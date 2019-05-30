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


// MessageFeedData
data class MessageFeedData(
    var id: Int,
    var content: String,
    var createdAt: String,
    var modifiedAt: String,
    var toUsername: String,
    var toDispName: String,
    var toAccountImage: String,
    var toWpOn: String,
    var toWpName: String,
    var fromUsername: String,
    var fromDispName: String,
    var fromAccountImage: String,
    var fromWpOn: String,
    var fromWpName: String,
    var favorite: Boolean,
    var favoriteCnt: Int,
    var clickType: String,
    var position: Int?
)


// MessageGetLoader
class MessageFeedGetLoader (context: Context, args: Bundle?) : AsyncTaskLoader<List<MessageFeedData>>(context) {

    // 変数定義
    private val LOG_TAG = MessageGetLoader::class.java.simpleName // ログ用にクラス名を取得
    private var cache: List<MessageFeedData>? = null // キャッシュ
    private var username: String? = args?.getString("username") // ユーザー名
    private var page: Int? = args?.getInt("page") // ページ
    private var favoriteFlg: Boolean? = args?.getBoolean("favoriteFlg") // お気に入りフィルターフラグ

    // loadInBackground
    override fun loadInBackground(): List<MessageFeedData>? {
        // 送信Bodyデータを作成
        val sendData = String.format(
            "{\"username\": \"%s\"," +
            " \"favoriteFlg\": %s," +
            " \"page\": %s}",
            username, favoriteFlg, page)
        Log.d("########## REQUEST CHECK", sendData)

        // 送信先を指定して接続用のインスタンスを作成
        val url = URL(context.getString(R.string.aws_ec2_endpoint) + "message/getmessagefeed/")

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
                val MessageFeedDataList = parseMessageFeedDataList(body)
                return MessageFeedDataList
            }
        }
        return null
    }


    // onStartLoading
    override fun onStartLoading() {
        forceLoad()
        if (cache != null) {
            deliverResult(cache)
        }
        if (takeContentChanged() || cache == null) {
            forceLoad()
        }
    }
    // deliverResult
    override fun deliverResult(data: List<MessageFeedData>?) {
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


// parseMessageFeedDataList
fun parseMessageFeedDataList(body: String) : List<MessageFeedData>? {
    // レスポンスデータからHrUserAllをインスタンス化して戻す
    val parentJsonObjsOrg = JSONObject(body)
    if (parentJsonObjsOrg.getInt("statusCode") == 200) {
        val MessageFeedDataList = mutableListOf<MessageFeedData>()
        val parentJsonObjs = JSONArray(parentJsonObjsOrg["data"].toString())
        for (i in 0 until parentJsonObjs.length()) {
            val parentJsonObj = parentJsonObjs.getJSONObject(i)
            val messageFeedData = MessageFeedData(
                id = parentJsonObj.getInt("id"),
                content = parentJsonObj.getString("content"),
                createdAt = parentJsonObj.getString("created_at"),
                modifiedAt = parentJsonObj.getString("modified_at"),
                toUsername = parentJsonObj.getString("to_username"),
                toDispName = parentJsonObj.getString("to_disp_name"),
                toAccountImage = parentJsonObj.getString("to_account_image"),
                toWpOn = parentJsonObj.getString("to_wp_on"),
                toWpName = parentJsonObj.getString("to_wp1_name"),
                fromUsername = parentJsonObj.getString("from_username"),
                fromDispName = parentJsonObj.getString("from_disp_name"),
                fromAccountImage = parentJsonObj.getString("from_account_image"),
                fromWpOn = parentJsonObj.getString("from_wp_on"),
                fromWpName = parentJsonObj.getString("from_wp1_name"),
                favorite = parentJsonObj.getBoolean("favorite"),
                favoriteCnt = parentJsonObj.getInt("favorite_cnt"),
                clickType = "",
                position = null
            )
            MessageFeedDataList.add(messageFeedData)
        }
        if (!MessageFeedDataList.isNullOrEmpty()) {
            return MessageFeedDataList
        }
    }
    return null
}

// インターフェイス定義
interface MessageFeedGetLoaderInterface {
    fun MessageFeedGetLoaderOnLoadFinished(data:  List<MessageFeedData>?)
}

// MessageFeedGetLoaderCallbacks
class MessageFeedGetLoaderCallbacks(
    private val context: Context,
    private val messageFeedGetLoaderInterface: MessageFeedGetLoaderInterface
) : LoaderManager.LoaderCallbacks<List<MessageFeedData>> {

    // 変数定義
    private val LOG_TAG = MessageFeedGetLoaderCallbacks::class.java.simpleName // ログ用にクラス名を取得

    override fun onCreateLoader(id: Int, args: Bundle?):  android.support.v4.content.Loader<List<MessageFeedData>> {
        return MessageFeedGetLoader(context, args)
    }

    override fun onLoadFinished(loader: android.support.v4.content.Loader<List<MessageFeedData>>, data: List<MessageFeedData>?) {
        messageFeedGetLoaderInterface.MessageFeedGetLoaderOnLoadFinished(data)
    }

    override fun onLoaderReset(loader: android.support.v4.content.Loader<List<MessageFeedData>>) {
        // No Action
    }
}