package com.yanagimachidev.huuurraydevapp


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Spinner
import android.widget.TextView
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker


// MapFragment
class MapFragment : Fragment(),
    OnMapReadyCallback {

    // 変数定義
    private val LOG_TAG = MapFragment::class.java.simpleName // ログ用にクラス名を取得
    private lateinit var userData : List<Map<String, Any?>>
    private var isLocationGet: Boolean = false
    private var lat: Double? = null
    private var lng: Double? = null
    private var firstflg = true
    private var forMapFlg = false
    private var accLat: Double? = null
    private var accLng: Double? = null
    private lateinit var mMap : GoogleMap
    private var firstMerkerFlg = false
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private val getLocationRequest = LocationRequest()
        .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
        .setInterval(1000)
        .setNumUpdates(1)


    // リスナーを親へ渡すためのインターフェイス
    interface OnFragmentInteractionListener {
        fun InfoWindowClick(snippet: String, lat: Double?, lng: Double?, lat1: Double, lng1: Double)
    }
    private var onFragmentInteractionListener: OnFragmentInteractionListener? = null


    // onAttach
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is MapFragment.OnFragmentInteractionListener) {
            onFragmentInteractionListener = context
        } else {
            throw RuntimeException(context!!.toString() + getString(R.string.no_listener_error))
        }
    }

    // onCreateView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // ユーザーデータの取得
        userData = queryAllHrUser(activity as Context)

        // 引数から状態を取得
        if (arguments != null) {
            if (firstflg && arguments!!.containsKey("forMapFlg")) {
                forMapFlg = arguments!!.getBoolean("forMapFlg")
            }
            if (forMapFlg) {
                accLat = arguments!!.getDouble("AccLat")
                accLng = arguments!!.getDouble("AccLng")
                Log.d(LOG_TAG, "accLat:" + accLat.toString())
                Log.d(LOG_TAG, "accLng:" + accLng.toString())
            }
        }

        // Viewを取得
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        // Mapフラグメントを展開
        val mapFragment =  childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // 店舗御種別を選択リスト化してイベントを登録
        val wpCategoryEditText =  view.findViewById<Spinner>(R.id.wp_category)
        wpCategoryEditText.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (firstMerkerFlg) {
                    MarkersSet(parent!!.selectedItem.toString())
                    Log.d(LOG_TAG, "##########" + parent.selectedItem.toString())
                } else {
                    firstMerkerFlg = true
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                //No Action
            }
        }

        return view
    }

    // onStop
    override fun onStop() {
        super.onStop()
        fusedLocationClient?.removeLocationUpdates(locationCallback)
    }

    // onDetach
    override fun onDetach() {
        super.onDetach()
        onFragmentInteractionListener = null
        firstflg = false
        forMapFlg = false
    }


    // onMapReady
    override fun onMapReady(googleMap: GoogleMap) {
        // Mapが開いた時の位置情報をセット
        mMap = googleMap
        mMap.isIndoorEnabled = false
        mMap.setInfoWindowAdapter(MyInfoWindowAdapter(activity as Activity))
        // 一旦東京全域を表示
        val tokyo = LatLng(35.6811323, 139.7670182)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tokyo, FIRST_ZOOM_LEVEL))
        // マーカーをセット
        MarkersSet(getString(R.string.all_view))

        // マーカータップ時のイベントハンドラ登録
        mMap.setOnMarkerClickListener (object: GoogleMap.OnMarkerClickListener {
            override fun onMarkerClick(marker: Marker?): Boolean {
                checkLocationAvailable(activity!!,
                    getLocationRequest,
                    locationCallback,
                    mMap)
                if (marker != null) {
                    val flagmentManager = activity!!.supportFragmentManager
                    val dialogFragment = MapMarkerInfoDialogFragment()
                    val toDialogBundle = Bundle()
                    toDialogBundle.putString("snippet", marker.snippet)
                    if (lat != null && lng != null) {
                        toDialogBundle.putDouble("myLat", lat!!)
                        toDialogBundle.putDouble("myLng", lng!!)
                    } else {
                        toDialogBundle.putDouble("myLat", 0.0)
                        toDialogBundle.putDouble("myLng", 0.0)
                    }
                    dialogFragment.setArguments(toDialogBundle)
                    dialogFragment.show(flagmentManager, "myMapMarkerInfoDialog")
                } else {
                    Log.e(LOG_TAG, "マーカー情報が取得できませんでした。")
                }
                return true
            }
        })

        // インフォウィンドウのオンクリックイベント
        mMap.setOnInfoWindowClickListener (object: GoogleMap.OnInfoWindowClickListener {
            override fun onInfoWindowClick(marker: Marker?) {
                checkLocationAvailable(activity!!,
                    getLocationRequest,
                    locationCallback,
                    mMap)
                if (marker != null) {
                    val lat1 = marker.position.latitude
                    val lng1 = marker.position.longitude
                    val listener = context as? MapFragment.OnFragmentInteractionListener
                    listener?.InfoWindowClick(marker.snippet, lat, lng, lat1, lng1)
                } else {
                    Log.e(LOG_TAG, "マーカー情報が取得できませんでした。")
                }
            }
        })

        if (forMapFlg && accLat != null && accLng != null) {
            setLocation(mMap, LatLng(accLat!!, accLng!!), DEFAULT_ZOOM_LEVEL)
        }

        checkLocationAvailable(activity!!,
            getLocationRequest,
            locationCallback,
            mMap)
    }

    // onActivityResult
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 17707 && resultCode == AppCompatActivity.RESULT_OK) {
            Log.d("onActivityResult", "##### onActivityResult Start")
            getUpdateLocation(activity!!,
                getLocationRequest,
                locationCallback,
                mMap)
        } else {
            showAlertDialog(activity!!, getString(R.string.error), getString(R.string.not_get_location_error))
        }
    }

    // onRequestPermissionsResult
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == 17709 && permissions.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d("onRequestPermissionsResult", "##### onRequestPermissionsResult Start")
            getUpdateLocation(activity!!,
                getLocationRequest,
                locationCallback,
                mMap)
        } else {
            showAlertDialog(activity!!, getString(R.string.error), getString(R.string.not_get_location_error))
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
                                val myLatLng = LatLng(location.latitude, location.longitude)
                                if (!forMapFlg) {
                                    setLocation(mMap, myLatLng, DEFAULT_ZOOM_LEVEL)
                                } else {
                                    // No Action
                                }
                            } else {
                                showAlertDialog(activity!!, getString(R.string.error), getString(R.string.not_get_location_error))
                            }
                        }
                    }
                }
            }
        })
    }


    // wpCategory
    private fun MarkersSet (wpCategory: String) {
        // マップへのタッチイベントを無効化
        val uiSettings = mMap.uiSettings
        uiSettings.setAllGesturesEnabled(false)
        // マップ読み込み中のダイアログを作成
        val builder = AlertDialog.Builder(activity as Context)
            .setTitle(getString(R.string.map_wait))
            .setMessage(getString(R.string.wait))
        val alertDialog = builder.show();
        // マーカーをセット
        mMap.clear()
        var befWp1Lat = 0.0
        var befWp1Lng = 0.0
        var count = 0
        var befMarker: Marker? = null
        userData.forEach{
            if (it["wp1_lat"] != null && it["wp1_lng"] != null) {
                if (wpCategory != getString(R.string.all_view) && wpCategory != it["wp1_category"]) {
                    return@forEach
                }
                count++
                val wpLat = it["wp1_lat"] as Double
                val wpLng = it["wp1_lng"] as Double
                val latLng = LatLng(wpLat, wpLng)
                var title = it["username"] as String + "@" + it["wp1_name"] as String
                if (it["disp_name"] != "") {
                    title = it["disp_name"] as String + "@" + it["wp1_name"] as String
                }
                var snippet = it["username"] as String

                val distance = getDistance(wpLat, wpLng, befWp1Lat, befWp1Lng)
                if (distance[0] < 10 && befMarker != null) {
                    befMarker!!.remove()
                    title += "&!$" + befMarker!!.title
                    snippet += "&!$" + befMarker!!.snippet
                } else {
                    count = 1
                }
                befMarker = setLocationWithMarker(mMap, latLng, title, snippet, count, false, null)
                befWp1Lat = wpLat
                befWp1Lng = wpLng
            }
        }
        alertDialog.dismiss()
        // マップへのタッチイベントを有効化
        uiSettings.setAllGesturesEnabled(true)
    }


    // MyInfoWindowAdapter
    class MyInfoWindowAdapter (private val activity: Activity) : GoogleMap.InfoWindowAdapter {

        // 変数定義
        private val LOG_TAG = MyInfoWindowAdapter::class.java.simpleName // ログ用にクラス名を取得
        private val inflater = LayoutInflater.from(activity)
        private val view = inflater.inflate(R.layout.my_info_window_adapter, null)

        // getInfoWindow
        override fun getInfoWindow(marker: Marker): View? {
            return null
        }

        // getInfoContents
        override fun getInfoContents (marker: Marker) : View? {
            // タイトルをセット
            val title =  view.findViewById<TextView>(R.id.title)
            title.setText(marker.getTitle())
            return view
        }
    }
}


fun newMapFragment(bundle: Bundle?) : MapFragment {
    val mapFragment = MapFragment()
    if (bundle != null && bundle.containsKey("forMapFlg")) {
        val forMapFlg = bundle.getBoolean("forMapFlg")
        if (forMapFlg) {
            val mapBundle = Bundle()
            mapBundle.putBoolean("forMapFlg", forMapFlg)
            mapBundle.putDouble("AccLat", bundle.getDouble("AccLat"))
            mapBundle.putDouble("AccLng", bundle.getDouble("AccLng"))
            mapFragment.setArguments(mapBundle)
        }
    }
    return mapFragment
}
