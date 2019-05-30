package com.yanagimachidev.huuurraydevapp


import android.content.Context
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.AsyncTaskLoader
import android.util.Log
import org.json.JSONObject
import java.net.URL


// Shop
data class Shop(
    val statusCode: Int,
    val username: String,
    val wpName: String,
    val wpCategory: String,
    val wpUrl: String,
    val wpLat: Double,
    val wpLng: Double,
    val wpLatSort: Double,
    val wpLngSort: Double,
    val wpLatlngSort: Double
)


// ShopUpsertLoader
class ShopUpsertLoader (context: Context, args: Bundle?) : AsyncTaskLoader<Shop>(context) {

    // 変数定義
    private val LOG_TAG = ShopUpsertLoader::class.java.simpleName // ログ用にクラス名を取得
    private val wpVer: String? = args?.getString("ver")
    private val username: String? = args?.getString("username")
    private var wpName: String? = args?.getString("wpName") // 店舗名
    private var wpCategory: String? = args?.getString("wpCategory") // 店舗種別
    private var wpUrl: String? = args?.getString("wpUrl") // 店舗URL
    private var wpLat: Double? = args?.getDouble("wpLat") // 店舗緯度
    private var wpLng: Double? = args?.getDouble("wpLng") // 店舗経度


    // loadInBackground
    override fun loadInBackground(): Shop? {
        lateinit var sendData: String
        // 送信Bodyデータを作成
        if (wpName == "") {
            sendData = String.format(
                "{\"username\": \"%s\"," +
                " \"wp_on\": \"%s\"," +
                " \"wp" + wpVer + "_image\": \"%s\"," +
                " \"wp" + wpVer + "_name\": \"%s\"," +
                " \"wp" + wpVer + "_category\": \"%s\"," +
                " \"wp" + wpVer + "_url\": \"%s\"," +
                " \"wp" + wpVer + "_lat\": %s," +
                " \"wp" + wpVer + "_lng\": %s," +
                " \"wp" + wpVer + "_lat_sort\": %s," +
                " \"wp" + wpVer + "_lng_sort\": %s," +
                " \"wp" + wpVer + "_latlng_sort\": %s}",
                username, "0", "", "", "", "", 0.0, 0.0, 0.0, 0.0, 0.0)
        } else {
            val wpLatlngSort = Math.pow(wpLat!!, 2.0) +  Math.pow(wpLng!!, 2.0)
            sendData = String.format(
                "{\"username\": \"%s\"," +
                " \"wp_on\": \"%s\"," +
                " \"wp" + wpVer + "_name\": \"%s\"," +
                " \"wp" + wpVer + "_category\": \"%s\"," +
                " \"wp" + wpVer + "_url\": \"%s\"," +
                " \"wp" + wpVer + "_lat\": %s," +
                " \"wp" + wpVer + "_lng\": %s," +
                " \"wp" + wpVer + "_lat_sort\": %s," +
                " \"wp" + wpVer + "_lng_sort\": %s," +
                " \"wp" + wpVer + "_latlng_sort\": %s}",
                username, wpVer, wpName, wpCategory, wpUrl, wpLat, wpLng, wpLat, wpLng, wpLatlngSort)
        }

        Log.d("########## REQUEST CHECK", sendData)

        // 送信先を指定して接続用のインスタンスを作成
        val url = URL(context.getString(R.string.aws_ec2_endpoint) + "hruser/upsert/")

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
                val shop = parseShop(body, wpVer)
                // SQLiteに書き込み
                var result: Boolean = false
                if (shop != null) {
                    // SQLの更新
                    val newDataString = mutableMapOf<String, String>()
                    val newDataInt = mutableMapOf<String, Int>()
                    val newDataDouble = mutableMapOf<String, Double>()
                    newDataString["username"] = shop.username
                    newDataString["wp_on"] = wpVer!!
                    newDataString["wp" + wpVer + "_name"] = shop.wpName
                    newDataString["wp" + wpVer + "_category"] = shop.wpCategory
                    newDataString["wp" + wpVer + "_url"] = shop.wpUrl
                    newDataDouble["wp" + wpVer + "_lat"] = shop.wpLat
                    newDataDouble["wp"  + wpVer + "_lng"] = shop.wpLng
                    newDataDouble["wp" + wpVer + "_lat_sort"] = shop.wpLat
                    newDataDouble["wp"  + wpVer + "_lng_sort"] = shop.wpLng
                    newDataDouble["wp"  + wpVer + "_latlng_sort"] = shop.wpLatlngSort
                    // 削除時は画像も削除
                    if (shop.wpName == "") {
                        // ローカルの画像を削除
                        context.deleteFile("wp" + wpVer + "_image")
                        newDataString["wp_on"] = "0"
                    }
                    result = upsertHrUser(context, newDataString, newDataInt, newDataDouble)
                } else {
                    Log.e(LOG_TAG, "Failed to Save Shop Data.")
                }
                if (result) {
                    return shop
                }
            }
        }
        return null
    }

    override fun onStartLoading() {
        forceLoad()
    }

    override fun onStopLoading() {
        cancelLoad()
    }

    override fun onReset() {
        super.onReset()
        onStopLoading()
    }
}


// parseShop
fun parseShop(body: String, ver: String?) : Shop? {
    // レスポンスデータからShopをインスタンス化して戻す
    val parentJsonObj = JSONObject(body)
    if (parentJsonObj.getInt("statusCode") == 200) {
        val shop = Shop(
            statusCode = parentJsonObj.getInt("statusCode"),
            username = parentJsonObj.getString("username"),
            wpName = parentJsonObj.getString("wp" + ver + "_name"),
            wpCategory = parentJsonObj.getString("wp" + ver + "_category"),
            wpUrl = parentJsonObj.getString("wp" + ver + "_url"),
            wpLat = parentJsonObj.getDouble("wp" + ver + "_lat"),
            wpLng = parentJsonObj.getDouble("wp" + ver + "_lng"),
            wpLatSort = parentJsonObj.getDouble("wp" + ver + "_lat_sort"),
            wpLngSort = parentJsonObj.getDouble("wp" + ver + "_lng_sort"),
            wpLatlngSort = parentJsonObj.getDouble("wp" + ver + "_latlng_sort")
        )
        return shop
    } else {
        return null
    }
}


// インターフェイス定義
interface ShopUpsertLoaderInterface {
    fun ShopUpsertLoaderOnLoadFinished(data: Shop?)
}

// ShopUpsertLoaderCallbacks
class ShopUpsertLoaderCallbacks(
    private val context: Context,
    private val shopUpsertLoaderInterface: ShopUpsertLoaderInterface
) : LoaderManager.LoaderCallbacks<Shop> {

    // 変数定義
    private val LOG_TAG = ShopUpsertLoaderCallbacks::class.java.simpleName // ログ用にクラス名を取得

    override fun onCreateLoader(id: Int, args: Bundle?): android.support.v4.content.Loader<Shop> {
        return ShopUpsertLoader(context, args)
    }

    override fun onLoadFinished(loader: android.support.v4.content.Loader<Shop>, data: Shop?) {
        shopUpsertLoaderInterface.ShopUpsertLoaderOnLoadFinished(data)
    }

    override fun onLoaderReset(loader: android.support.v4.content.Loader<Shop>) {
        // No Action
    }
}