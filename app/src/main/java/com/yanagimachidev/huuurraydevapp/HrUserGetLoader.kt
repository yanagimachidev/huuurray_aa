package com.yanagimachidev.huuurraydevapp


import android.support.v4.app.LoaderManager
import android.support.v4.content.AsyncTaskLoader
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.os.Bundle
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.util.*


// HrUser
data class HrUserAll(
    val username: String,
    val dispName: String,
    val sex: String,
    val birthday: String,
    val profile: String,
    val accountImage: String,
    val backImage: String,
    val wpOn: String,
    var wp1Name: String,
    var wp1Category: String,
    var wp1Url: String,
    var wp1Lat: Double?,
    var wp1Lng: Double?,
    var wp1LatSort: Double?,
    var wp1LngSort: Double?,
    var wp1LatlngSort: Double?,
    var wp1Image: String,
    val deleteFlg: String
)


// HrUserGetLoader
class HrUserGetLoader (context: Context, args: Bundle?) : AsyncTaskLoader<List<HrUserAll>>(context) {

    // 変数定義
    private val LOG_TAG = HrUserUpsertLoader::class.java.simpleName // ログ用にクラス名を取得
    private var cache: List<HrUserAll>? = null // キャッシュ
    private var updatedAt: String? = args?.getString("updated_at") // 最終取得日時


    // loadInBackground
    override fun loadInBackground(): List<HrUserAll>? {
        // 送信Bodyデータを作成
        val sendData = String.format("{\"updatedAt\": \"%s\"}", updatedAt)
        Log.d("########## REQUEST CHECK", sendData)

        // 送信先を指定して接続用のインスタンスを作成
        val url = URL(context.getString(R.string.aws_ec2_endpoint) + "hruser/getuser/")

        // 送信クライアントを作成して接続
        val response = MyOkhttpConnection(sendData, url)

        // レスポンスを処理
        if (response != null) {
            // ステータスコードが正常値の場合には、データを返す
            val statusCode = response.code()
            Log.i(LOG_TAG, "StatusCode" + statusCode)
            if (statusCode == 200){
                val body = response.body()!!.string()
                Log.d("########## RESPONSE CHECK", body)
                val hrUserList = parseHrUserList(body)
                var result: Boolean = false
                if (hrUserList != null) {
                    hrUserList.forEach { data ->
                        val newDataString = mutableMapOf<String, String>()
                        val newDataInt = mutableMapOf<String, Int?>()
                        val newDataDouble = mutableMapOf<String, Double?>()
                        newDataString["username"] = data.username
                        newDataString["disp_name"] = data.dispName
                        newDataString["sex"] = data.sex
                        newDataString["birthday"] = data.birthday
                        newDataString["profile"] = data.profile
                        newDataString["account_image"] = data.accountImage
                        newDataString["back_image"] = data.backImage
                        newDataString["wp_on"] = data.wpOn
                        newDataString["wp1_name"] = data.wp1Name
                        newDataString["wp1_category"] = data.wp1Category
                        newDataString["wp1_url"] = data.wp1Url
                        newDataDouble["wp1_lat"] = data.wp1Lat
                        newDataDouble["wp1_lng"] = data.wp1Lng
                        newDataDouble["wp1_lat_sort"] = data.wp1LatSort
                        newDataDouble["wp1_lng_sort"] = data.wp1LngSort
                        newDataDouble["wp1_latlng_sort"] = data.wp1LatlngSort
                        newDataString["wp1_image"] = data.wp1Image
                        newDataString["delete_flg"] = data.deleteFlg
                        result = upsertHrUser(context, newDataString, newDataInt, newDataDouble)
                        Log.d(LOG_TAG, "#####username" + data.username)
                        Log.d(LOG_TAG, "#####result" + result)
                    }
                } else {
                    result = true
                    Log.e(LOG_TAG, "No Update Data.")
                }
                if (result) {
                    // 現在UTC時刻を取得
                    val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    df.setTimeZone(TimeZone.getTimeZone("gmt"))
                    val gmtNowTime = df.format(Date())
                    Log.d(LOG_TAG, "Updated At. (UTC)" + gmtNowTime)
                    // SharedPreferencesに書き込み
                    val acc_pref = context.getSharedPreferences("account_info", Context.MODE_PRIVATE)
                    val editor = acc_pref.edit()
                    editor.putString("updated_at", gmtNowTime)
                    editor.apply()
                } else {
                    Log.e(LOG_TAG, "Failed to Save Users Data.")
                }
                return hrUserList
            }
        }
        return null
    }


    // onStartLoading
    override fun onStartLoading() {
        //forceLoad()
        if (cache != null) {
            deliverResult(cache)
        }
        if (takeContentChanged() || cache == null) {
            forceLoad()
        }
    }
    // deliverResult
    override fun deliverResult(data: List<HrUserAll>?) {
        if (isReset) return
        cache = data
        super.deliverResult(data)
    }
    // onStopLoading
    override fun onStopLoading() {
        cancelLoad()
    }
    // onReset
    override fun onReset() {
        super.onReset()
        onStopLoading()
        cache = null
    }
}


// parseHrUser
fun parseHrUserList(body: String) : List<HrUserAll>? {
    // レスポンスデータからHrUserAllをインスタンス化して戻す
    val parentJsonObjsOrg = JSONObject(body)
    if (parentJsonObjsOrg.getInt("statusCode") == 200) {
        val hrUserList = mutableListOf<HrUserAll>()
        val parentJsonObjs = JSONArray(parentJsonObjsOrg["data"].toString())
        for (i in 0 until parentJsonObjs.length()) {
            val parentJsonObj = parentJsonObjs.getJSONObject(i)
            val hrUser = HrUserAll(
                username = parentJsonObj.getString("username"),
                dispName = parentJsonObj.getString("disp_name"),
                sex = parentJsonObj.getString("sex"),
                birthday = parentJsonObj.getString("birthday"),
                profile = parentJsonObj.getString("profile"),
                accountImage = parentJsonObj.getString("account_image"),
                backImage = parentJsonObj.getString("back_image"),
                wpOn = parentJsonObj.getString("wp_on"),
                wp1Name = "",
                wp1Category = "",
                wp1Url = "",
                wp1Lat = null,
                wp1Lng = null,
                wp1LatSort = null,
                wp1LngSort = null,
                wp1LatlngSort = null,
                wp1Image = "",
                deleteFlg = parentJsonObj.getString("delete_flg")
            )
            if (hrUser.wpOn != "0") {
                hrUser.wp1Name = parentJsonObj.getString("wp1_name")
                hrUser.wp1Category = parentJsonObj.getString("wp1_category")
                hrUser.wp1Url = parentJsonObj.getString("wp1_url")
                hrUser.wp1Lat = parentJsonObj.getDouble("wp1_lat")
                hrUser.wp1Lng = parentJsonObj.getDouble("wp1_lng")
                hrUser.wp1LatSort = parentJsonObj.getDouble("wp1_lat_sort")
                hrUser.wp1LngSort = parentJsonObj.getDouble("wp1_lng_sort")
                hrUser.wp1LatlngSort = parentJsonObj.getDouble("wp1_latlng_sort")
                hrUser.wp1Image = parentJsonObj.getString("wp1_image")
            }
            hrUserList.add(hrUser)
        }
        if (!hrUserList.isNullOrEmpty()) {
            return hrUserList
        }
    }
    return null
}


// インターフェイス定義
interface HrUserGetLoaderInterface {
    fun HrUserGetLoaderOnLoadFinished(data: List<HrUserAll>?)
}

// HrUserGetLoaderCallbacks
class HrUserGetLoaderCallbacks(
    private val context: Context,
    private val hrUserGetLoaderInterface: HrUserGetLoaderInterface
) : LoaderManager.LoaderCallbacks<List<HrUserAll>> {

    // 変数定義
    private val LOG_TAG = HrUserGetLoaderCallbacks::class.java.simpleName // ログ用にクラス名を取得

    override fun onCreateLoader(id: Int, args: Bundle?): android.support.v4.content.Loader<List<HrUserAll>> {
        return HrUserGetLoader(context, args)
    }

    override fun onLoadFinished(loader: android.support.v4.content.Loader<List<HrUserAll>>, data: List<HrUserAll>?) {
        hrUserGetLoaderInterface.HrUserGetLoaderOnLoadFinished(data)
    }

    override fun onLoaderReset(loader: android.support.v4.content.Loader<List<HrUserAll>>) {
        // No Action
    }
}