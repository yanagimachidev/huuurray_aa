package com.yanagimachidev.huuurraydevapp

import android.app.Activity
import android.app.FragmentManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.support.v4.app.ActivityCompat.requestPermissions
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import okhttp3.*
import org.json.JSONObject
import java.io.BufferedInputStream
import java.net.URL


// AccountData
data class AccountData(
    var username: String,
    var dispName: String,
    var accountImage: String,
    var wpOn: String,
    var wpName: String,
    var peFlg: Int?
){
    // get
    fun get(col: String) : Any? {
        when (col) {
            "username" -> {
                return username
            }
            "disp_name" -> {
                return dispName
            }
            "account_image" -> {
                return accountImage
            }
            "wp_on" -> {
                return wpOn
            }
            "wp1_name" -> {
                return wpName
            }
            else -> {
                return peFlg
            }
        }
    }

    // set
    fun set(col: String, value: Any) {
        when (col) {
            "username" -> {
                username = value as String
            }
            "disp_name" -> {
                dispName = value as String
            }
            "account_image" -> {
                accountImage = value as String
            }
            "wp_on" -> {
                wpOn = value as String
            }
            "wp1_name" -> {
                wpName = value as String
            }
            else -> {
                peFlg = value as Int
            }
        }
    }
}

// parseResult(ステータスコードのみが返ってくる場合)
fun parseResult(body: String) : Boolean {
    // レスポンスデータからPostDataをインスタンス化して戻す
    val parentJsonObjsOrg = JSONObject(body)
    if (parentJsonObjsOrg.getInt("statusCode") == 200) {
        return true
    } else {
        return false
    }
}


// ボタンの連打禁止用
fun View.notPressTwice() {
    this.isEnabled = false
    this.postDelayed({
        this.isEnabled = true
    }, 3000L)
}

// OKhttpの実行
fun MyOkhttpConnection (sendData: String, url: URL) : Response? {
    try{
        val client = OkHttpClient()
        val mimeType = MediaType.parse("application/json; charset=utf-8")
        val requestBody = RequestBody.create(mimeType, sendData)
        val request = Request.Builder().url(url).post(requestBody).build()
        return client.newCall(request).execute()
    }catch (e: Exception) {
        return null
    }
}

// ローカルファイルから各画像をセットする関数
fun getAccountImage(activity: Activity, imageType: String) : Bitmap? {
    try {
        val bufferedInputStream = BufferedInputStream(activity.openFileInput(imageType + "_image"))
        val bitmap = BitmapFactory.decodeStream(bufferedInputStream)
        bufferedInputStream.close()
        return bitmap
    } catch (e: java.lang.Exception) {
        return null
    }
}

// S3画像をGlideで処理する関数
fun getS3ImageViaGlide(context: Context, imageType: String, imageName: String, imageView: ImageView) {
    Glide.with (context)
        .asBitmap()
        .load (context.getString(R.string.s3_endpoint) + imageType + "_image/" + imageName)
        .skipMemoryCache(true)
        .into (object : CustomViewTarget<ImageView, Bitmap>(imageView) {
            override fun onLoadFailed(errorDrawable: Drawable?) {
                Log.d("Glide", "onLoadFailed")
            }

            override fun onResourceCleared(placeholder: Drawable?) {
                Log.d("Glide", "onResourceCleared" + imageName)
            }

            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                Log.d("Glide", "onResourceReady:" + imageName)
                imageView.setImageBitmap(resource)
            }
        })
}


// ２地点間の距離を求める
fun getDistance (lat1: Double, lng1: Double, lat2: Double, lng2: Double) : FloatArray {
    val results = FloatArray(3)
    Location.distanceBetween(lat1, lng1, lat2, lng2, results)
    return results
}


// スクロール最下部でデータを追加でロードするためのクラス
abstract class EndlessScrollListener(private val mLinearLayoutManager: LinearLayoutManager) :
    RecyclerView.OnScrollListener() {

    // 変数定義
    private val LOG_TAG = EndlessScrollListener::class.java.simpleName // ログ用にクラス名を取得
    private var currentPage = 0
    private var firstVisibleItem = 0
    private var visibleItemCount = 0
    private var totalItemCount = 0
    private var previousTotal = 0
    private var loading = true
    private var visibleThreshold = 2

    override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        visibleItemCount = recyclerView!!.childCount
        totalItemCount = mLinearLayoutManager.itemCount
        firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition()
        /*
        Log.d(LOG_TAG, "###############totalItemCount=" + totalItemCount.toString())
        Log.d(LOG_TAG, "###############previousTotal=" + previousTotal.toString())
        Log.d(LOG_TAG, "###############visibleItemCount=" + visibleItemCount.toString())
        Log.d(LOG_TAG, "###############firstVisibleItem=" + firstVisibleItem.toString())
        */

        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false
                previousTotal = totalItemCount
            }
        }

        if (!loading && totalItemCount - visibleItemCount <= firstVisibleItem + visibleThreshold) {
            currentPage++

            onLoadMore(currentPage)

            loading = true
        }
    }

    abstract fun onLoadMore(currentPage: Int)
}


// スクロール最上部以外ではリフレッシュされないようにステータスを更新
fun refreshStatusUpdate(layoutManager: LinearLayoutManager, mSwipeRefreshLayout: SwipeRefreshLayout) {
    val firstPos =  layoutManager.findFirstCompletelyVisibleItemPosition()
    if (firstPos == 0) {
        mSwipeRefreshLayout.setEnabled(true)
    } else if (mSwipeRefreshLayout.isEnabled) {
        mSwipeRefreshLayout.setEnabled(false)
    }
}


// checkLocationAvailable
fun checkLocationAvailable(
    activity: Activity,
    getLocationRequest: LocationRequest,
    locationCallback: LocationCallback,
    mMap : GoogleMap
) {
    val checkRequest = LocationSettingsRequest.Builder()
        .addLocationRequest(getLocationRequest).build()
    val checkTask = LocationServices.getSettingsClient(activity).checkLocationSettings(checkRequest)
    checkTask.addOnCompleteListener { response ->
        try {
            Log.d("checkLocationAvailable", "##### checkLocationAvailable Start")
            response.getResult(ApiException::class.java)
            getUpdateLocation(activity, getLocationRequest, locationCallback, mMap)
        } catch (exception: ApiException) {
            if (exception.statusCode == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                val resolvable = exception as ResolvableApiException
                resolvable.startResolutionForResult(activity, 17707)
            } else {
                showAlertDialog(activity,
                    activity.getString(R.string.error), activity.getString(R.string.not_get_location_error))
            }
        }
    }
}


// 位置情報の更新
fun getUpdateLocation(
    activity: Activity,
    getLocationRequest: LocationRequest,
    locationCallback: LocationCallback,
    mMap: GoogleMap
) {
    if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity) != ConnectionResult.SUCCESS) {
        showAlertDialog(activity, activity.getString(R.string.error), activity.getString(R.string.not_get_location_error))
    }

    if(ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED) {
        // 位置情報の更新
        LocationServices.getFusedLocationProviderClient(activity).let { client ->
            client.requestLocationUpdates(getLocationRequest, locationCallback, null)
            /*
            .addOnCompleteListener { task ->
                if (task.result == null) {
                    Log.d(LOG_TAG, "########## Failed to Get Location.")
                }
            }
            */
            mMap.setMyLocationEnabled(true)
        }
    } else {
        requestPermissions(activity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 17709)
    }
}


// 最後に取得した位置情報を取得
/*
private fun receiveLocation() {
    if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
        showErrorMessage()
    }

    if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED) {
        // 位置情報を取得
        LocationServices.getFusedLocationProviderClient(this).let { client ->
            client.lastLocation.addOnCompleteListener(this) { task ->
                if (task.isSuccessful && task.result != null) {
                    setLocationWithMarker(LatLng(task.result!!.latitude, task.result!!.longitude))
                } else {
                    getUpdateLocation()
                }
            }
        }
    } else {
        requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 17709)
    }
}
*/


// マーカーを立てる
fun setLocationWithMarker(
    mMap: GoogleMap,
    latLng: LatLng,
    title: String,
    snippet: String,
    count: Int,
    zoomFlg: Boolean,
    zoomLevel: Float?
) : Marker {
    var icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
    if (count > 1) {
        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
    }
    val marker = mMap.addMarker(
        MarkerOptions()
            .position(latLng)
            .title(title)
            .snippet(snippet)
            .icon(icon)
    )
    if (zoomFlg && zoomLevel != null) {
        setLocation(mMap, latLng, zoomLevel)
    }
    return marker
}


// 現在地にズームする
fun setLocation(mMap: GoogleMap, latLng: LatLng, zoomLevel: Float) {
    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))
}

// ダイアログ表示
fun showAlertDialog(activity: Activity, title: String, Message: String) : AlertDialog {
    // ダイアログを作成/表示
    val builder = AlertDialog.Builder(activity)
        .setTitle(title)
        .setMessage(Message)
        .setNeutralButton(android.R.string.ok, null)
    return builder.show()
}

// ScrollDisableListener
class ScrollDisableListener : RecyclerView.OnItemTouchListener {
    override fun onInterceptTouchEvent(rv: RecyclerView?, e: MotionEvent?): Boolean {
        return true;
    }

    override fun onTouchEvent(rv: RecyclerView?, e: MotionEvent?) {
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
    }
}