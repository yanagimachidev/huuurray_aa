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


// MessageData
data class MessageDispData(
    var id: Int?,
    var toUsername: String?,
    var fromUsername: String?,
    var content: String?,
    var deleteFlg: String?,
    var createdAt: String?,
    var modifiedAt: String?,
    var dispName: String?,
    var accountImage: String?,
    var wpOn: String?,
    var wpName: String?,
    var wpUrl: String?,
    var wpLat: Double?,
    var wpLng: Double?,
    var clickType: String?,
    var position: Int?
){
    // get
    fun get(col: String) : Any? {
        when (col) {
            "to_username" -> {
                return toUsername
            }
            "from_username" -> {
                return fromUsername
            }
            "content" -> {
                return content
            }
            "delete_flg" -> {
                return deleteFlg
            }
            "created_at" -> {
                return createdAt
            }
            "modified_at" -> {
                return modifiedAt
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
            "wp_name" -> {
                return wpName
            }
            "wp_url" -> {
                return wpUrl
            }
            "wp_lat" -> {
                return wpLat
            }
            "wp_lng" -> {
                return wpLng
            }
            else -> {
                return id
            }
        }
    }

    // set
    fun set(col: String, value: Any) {
        when (col) {
            "to_username" -> {
                toUsername = value as String
            }
            "from_username" -> {
                fromUsername = value as String
            }
            "content" -> {
                content = value as String
            }
            "delete_flg" -> {
                deleteFlg = value as String
            }
            "created_at" -> {
                createdAt = value as String
            }
            "modified_at" -> {
                modifiedAt = value as String
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
            "wp_name" -> {
                wpName = value as String
            }
            "wp_url" -> {
                wpUrl = value as String
            }
            "wp_lat" -> {
                wpLat = value as Double
            }
            "wp_lng" -> {
                wpLng = value as Double
            }
            else -> {
                id = value as Int
            }
        }
    }
}


// MessageData
data class MessageData(
    var id: Int?,
    var toUsername: String?,
    var fromUsername: String?,
    var content: String?,
    var deleteFlg: String?,
    var createdAt: String?,
    var modifiedAt: String?
){
    // get
    fun get(col: String) : Any? {
        when (col) {
            "to_username" -> {
                return toUsername
            }
            "from_username" -> {
                return fromUsername
            }
            "content" -> {
                return content
            }
            "delete_flg" -> {
                return deleteFlg
            }
            "created_at" -> {
                return createdAt
            }
            "modified_at" -> {
                return modifiedAt
            }
            else -> {
                return id
            }
        }
    }

    // set
    fun set(col: String, value: Any) {
        when (col) {
            "to_username" -> {
                toUsername = value as String
            }
            "from_username" -> {
                fromUsername = value as String
            }
            "content" -> {
                content = value as String
            }
            "delete_flg" -> {
                deleteFlg = value as String
            }
            "created_at" -> {
                createdAt = value as String
            }
            "modified_at" -> {
                modifiedAt = value as String
            }
            else -> {
                id = value as Int
            }
        }
    }
}


// MessageGetLoader
class MessageGetLoader (context: Context, args: Bundle?) : AsyncTaskLoader<List<MessageData>>(context) {

    // 変数定義
    private val LOG_TAG = MessageGetLoader::class.java.simpleName // ログ用にクラス名を取得
    private var cache: List<MessageData>? = null // キャッシュ
    private var updatedAt: String? = args?.getString("updated_at") // 最終取得日時
    private var username: String? = args?.getString("username") // ユーザー名


    // loadInBackground
    override fun loadInBackground(): List<MessageData>? {
        // 送信Bodyデータを作成
        val sendData = String.format(
            "{\"updatedAt\": \"%s\"," +
            " \"username\": \"%s\"}",
            updatedAt, username)
        Log.d("########## REQUEST CHECK", sendData)

        // 送信先を指定して接続用のインスタンスを作成
        val url = URL(context.getString(R.string.aws_ec2_endpoint) + "message/getmessage/")

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
                val messageDataList = parseMessageDataList(body)
                var result: Boolean = false
                if (messageDataList != null) {
                    messageDataList.forEach { data ->
                        val newDataString = mutableMapOf<String, String?>()
                        val newDataInt = mutableMapOf<String, Int?>()
                        newDataInt["id"] = data.id
                        newDataString["to_username"] = data.toUsername
                        newDataString["from_username"] = data.fromUsername
                        newDataString["content"] = data.content
                        newDataString["delete_flg"] = data.deleteFlg
                        newDataString["created_at"] = data.createdAt
                        newDataString["modified_at"] = data.modifiedAt
                        if (updatedAt == "") {
                            newDataString["open_flg"] = "1"
                        }
                        result = upsertHrMessage(context, newDataString, newDataInt)
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
                    editor.putString("message_updated_at", gmtNowTime)
                    editor.apply()
                } else {
                    Log.e(LOG_TAG, "Failed to Save Message Data.")
                }
                return messageDataList
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
    override fun deliverResult(data: List<MessageData>?) {
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


// parseMessageDataList
fun parseMessageDataList(body: String) : List<MessageData>? {
    // レスポンスデータからHrUserAllをインスタンス化して戻す
    val parentJsonObjsOrg = JSONObject(body)
    if (parentJsonObjsOrg.getInt("statusCode") == 200) {
        val messageDataList = mutableListOf<MessageData>()
        val parentJsonObjs = JSONArray(parentJsonObjsOrg["data"].toString())
        for (i in 0 until parentJsonObjs.length()) {
            val parentJsonObj = parentJsonObjs.getJSONObject(i)
            val messageData = MessageData(
                id = parentJsonObj.getInt("id"),
                toUsername = parentJsonObj.getString("to_username_id"),
                fromUsername = parentJsonObj.getString("from_username_id"),
                content = parentJsonObj.getString("content"),
                deleteFlg = parentJsonObj.getString("delete_flg"),
                createdAt = parentJsonObj.getString("created_at"),
                modifiedAt = parentJsonObj.getString("modified_at")
            )
            messageDataList.add(messageData)
        }
        if (!messageDataList.isNullOrEmpty()) {
            return messageDataList
        }
    }
    return null
}

// インターフェイス定義
interface MessageGetLoaderInterface {
    fun MessageGetLoaderOnLoadFinished(data:  List<MessageData>?)
}

// MessageGetLoaderCallbacks
class MessageGetLoaderCallbacks(
    private val context: Context,
    private val messageGetLoaderInterface: MessageGetLoaderInterface
) : LoaderManager.LoaderCallbacks<List<MessageData>> {

    // 変数定義
    private val LOG_TAG = MessageGetLoaderCallbacks::class.java.simpleName // ログ用にクラス名を取得

    override fun onCreateLoader(id: Int, args: Bundle?):  android.support.v4.content.Loader<List<MessageData>> {
        return MessageGetLoader(context, args)
    }

    override fun onLoadFinished(loader: android.support.v4.content.Loader<List<MessageData>>, data: List<MessageData>?) {
        messageGetLoaderInterface.MessageGetLoaderOnLoadFinished(data)
    }

    override fun onLoaderReset(loader: android.support.v4.content.Loader<List<MessageData>>) {
        // No Action
    }
}