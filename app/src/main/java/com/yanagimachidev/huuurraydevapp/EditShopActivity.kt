package com.yanagimachidev.huuurraydevapp


import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.location.LocationRequest


// EditShopActivity
class EditShopActivity :
    AppCompatActivity(),
    OnMapReadyCallback,
    ShopUpsertLoaderInterface,
    NoGifAlertDialogFragment.OnFragmentInteractionListener {

    // 変数定義
    private val LOG_TAG = EditShopActivity::class.java.simpleName // ログ用にクラス名を取得
    private var ver: String? = null // 何店舗目か
    private var isLocationGet: Boolean = false // 位置情報が取得できているか
    private lateinit var username: String // ユーザー名
    private lateinit var wpNameEditText: TextView // 店舗名
    private lateinit var wpCategoryEditText: Spinner // 店舗種別
    private lateinit var wpURLEditText: TextView // 店舗URL
    private var lat: Double? = null // 緯度
    private var lng: Double? = null // 経度
    private lateinit var mMap : GoogleMap // Mapインスタンス
    private var fusedLocationClient: FusedLocationProviderClient? = null // FusedLocationProviderClientインスタンス
    // リクエスト設定（位置情報を1回だけ取得）
    private val getLocationRequest = LocationRequest()
        .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
        .setInterval(1000)
        .setNumUpdates(1)


    // onCreate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_shop)

        // StatusBarを表示、透過する設定
        findViewById<View>(android.R.id.content).systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        // 店舗位置情報取得のためのマップを展開
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // ユーザーデータの取得
        val pref = getSharedPreferences("aws_credentials", Context.MODE_PRIVATE)
        username = pref.getString("username", "")
        val data = queryHrUser(this, username)

        // 何店舗目かを取得
        ver = intent.getStringExtra("ver")

        // 緯度の取得
        if (!(data.isEmpty() || data["wp" + ver +"_lat"] == null)) {
            lat = data["wp" + ver + "_lat"] as Double
        }

        // 緯度の取得経度
        if (!(data.isEmpty() || data["wp" + ver + "_lng"] == null)) {
            lng = data["wp" + ver + "_lng"] as Double
        }

        // 店舗名のセット
        wpNameEditText = findViewById<EditText>(R.id.wp_name)
        if (!(data.isEmpty() || data["wp" + ver + "_name"] == "")) {
            wpNameEditText.text = data["wp" + ver + "_name"] as String
        }

        // 店舗種別のセット
        wpCategoryEditText =  findViewById<Spinner>(R.id.wp_category)
        if (!(data.isEmpty() || data["wp" + ver + "_category"] == "")) {
            val items = resources.getStringArray(R.array.wp_category)
            for (i in 0 .. items.size - 1) {
                wpCategoryEditText.setSelection(i)
                if (wpCategoryEditText.selectedItem == data["wp" + ver + "_category"] as String) {
                    break
                }
            }
        }

        // URLのセット
        wpURLEditText = findViewById<EditText>(R.id.wp_url)
        if (!(data.isEmpty() || data["wp" + ver + "_url"] == "")) {
            wpURLEditText.text = data["wp" + ver + "_url"] as String
        }

        // 位置情報の再取得用ボタン
        val updateBottun = findViewById<Button>(R.id.update)
        updateBottun.setOnClickListener {
            it.notPressTwice()
            mMap.clear()
            lat = null
            lng = null
            isLocationGet = false
            checkLocationAvailable(this,
                getLocationRequest,
                locationCallback,
                mMap)
        }

        // 保存用ボタン
        val saveBottun = findViewById<Button>(R.id.save)
        saveBottun.setOnClickListener {
            it.notPressTwice()
            // エラーフラグ
            var inValid = true

            // 店舗名の入力値を取得
            val wpNameText = wpNameEditText.text.toString()

            // 店舗名の未入力エラー
            if (wpNameText.isEmpty()) {
                wpNameEditText.error = getString(R.string.no_wp_name_error)
                wpNameEditText.requestFocus()
                inValid = false
            }

            // 店舗名の長さエラー
            if (wpNameText.length > 30 && inValid) {
                wpNameEditText.error = getString(R.string.too_long_wp_name_error) +
                        wpNameText.length + getString(R.string.too_long_error_end)
                wpNameEditText.requestFocus()
                inValid = false
            }

            // 店舗種別の入力値を取得
            val wpCategoryText = wpCategoryEditText.selectedItem as String

            // URLの入力値を取得
            val wpURLText = wpURLEditText.text.toString()

            //  URLの長さエラー
            if (wpURLText.length > 200 && inValid) {
                wpURLEditText.error = getString(R.string.too_long_wp_url_error) +
                        wpURLText.length + getString(R.string.too_long_error_end)
                wpURLEditText.requestFocus()
                inValid = false
            }

            // 位置情報が未取得エラー
            if (lat == null || lng == null) {
                showAlertDialog(this,
                    getString(R.string.error), getString(R.string.not_get_location_error))
                inValid = false
            }

            // 引数に値を渡してローダーを起動
            if (inValid) {
                val bundle = Bundle()
                bundle.putString("ver", ver)
                bundle.putString("username", username)
                bundle.putString("wpName", wpNameText)
                bundle.putString("wpCategory", wpCategoryText)
                bundle.putString("wpUrl", wpURLText)
                bundle.putDouble("wpLat", lat!!)
                bundle.putDouble("wpLng", lng!!)
                startShopUpsertLoader(bundle)
            }
        }

        // 削除用ボタン
        val deleteButton = findViewById<Button>(R.id.delete)
        deleteButton.setOnClickListener{
            val flagmentManager = supportFragmentManager
            val dialogFragment = NoGifAlertDialogFragment()
            val toDialogBundle = Bundle()
            toDialogBundle.putString("title", getString(R.string.confirm))
            toDialogBundle.putString("message", getString(R.string.delete_confirm))
            toDialogBundle.putString("yes", getString(R.string.wp_delete))
            toDialogBundle.putString("no", getString(R.string.cancel))
            dialogFragment.setArguments(toDialogBundle)
            dialogFragment.show(flagmentManager, "NoGifAlertDialog")
        }
    }

    // onStop
    override fun onStop() {
        super.onStop()
        fusedLocationClient?.removeLocationUpdates(locationCallback)
    }


    // onMapReady
    override fun onMapReady(googleMap: GoogleMap) {
        // Mapが開いた時の位置情報をセット
        mMap = googleMap
        mMap.isIndoorEnabled = false
        // 前回の位置情報をセット
        if (lat != null && lat != 0.0  && lng != null && lng != 0.0) {
            val latitude = lat!!
            val longitude = lng!!
            setLocationWithMarker(mMap,
                LatLng(latitude, longitude),
                getString(R.string.wp_location),
                "",
                1,
                true,
                DEFAULT_ZOOM_LEVEL)
        } else {
            // 前回情報がない場合はパーミッション関連処理開始
            val tokyo = LatLng(35.6811323, 139.7670182)
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tokyo, FIRST_ZOOM_LEVEL))
            checkLocationAvailable(this,
                getLocationRequest,
                locationCallback,
                mMap)
        }
    }

    // onActivityResult
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 17707 && resultCode == RESULT_OK) {
            getUpdateLocation(this,
                getLocationRequest,
                locationCallback,
                mMap)
        } else {
            showAlertDialog(this,
                getString(R.string.error), getString(R.string.not_get_location_error))
        }
    }

    // onRequestPermissionsResult
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == 17709 && permissions.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getUpdateLocation(this,
                getLocationRequest,
                locationCallback,
                mMap)
        } else {
            showAlertDialog(this,
                getString(R.string.error), getString(R.string.not_get_location_error))
        }
    }

    // 位置情報更新後に処理位置情報を再取得
    private val locationCallback: LocationCallback by lazy {
        (object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                if (locationResult?.lastLocation == null ) {
                    Log.e(LOG_TAG, "Failed to Get Location.")
                } else {
                    if (!isLocationGet) {
                        locationResult.lastLocation.let { location ->
                            isLocationGet = true
                            lat = location.latitude
                            lng = location.longitude
                            if (lat != null && lng != null) {
                                val merker = setLocationWithMarker(mMap,
                                    LatLng(location.latitude, location.longitude),
                                    getString(R.string.wp_location),
                                    "",
                                    1,
                                    true,
                                    DEFAULT_ZOOM_LEVEL)
                            } else {
                                val dialog = showAlertDialog(this@EditShopActivity,
                                    getString(R.string.error),  getString(R.string.not_get_location_error))
                            }
                        }
                    }
                }
            }
        })
    }


    // noButtonOnClick
    override fun noButtonOnClick(type: Int) {
        // No Action
    }

    // yesButtonOnClick
    override fun yesButtonOnClick(type: Int, args: Bundle?) {
        val bundle = Bundle()
        bundle.putString("ver", ver)
        bundle.putString("username", username)
        bundle.putString("wpName", "")
        bundle.putString("wpUrl", "")
        bundle.putDouble("wpLat", 0.0)
        bundle.putDouble("wpLng", 0.0)
        startShopUpsertLoader(bundle)
    }


    private fun startShopUpsertLoader (bundle: Bundle) {
        supportLoaderManager.restartLoader(13304, bundle,
            ShopUpsertLoaderCallbacks(this, this))
    }

    // ShopUpsertLoaderOnLoadFinished
    override fun ShopUpsertLoaderOnLoadFinished(data: Shop?) {
        var result = false
        if (data != null) {
            result = true
        }
        val intent = Intent()
        intent.putExtra("SaveShopData", result)
        setResult(RESULT_OK, intent)
        finish()
    }
}
