package com.yanagimachidev.huuurraydevapp

import android.content.Context
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.AsyncTaskLoader
import android.util.Log
import org.json.JSONObject
import java.net.URL


// PointData
data class PointData(
    val toUsername: String,
    val fromUsername: String,
    val sp: Int,
    val sendSp: Int,
    val sendSpTo: Int,
    val favorite: Int,
    val favoriteTo: Int
)


// PointDataGetLoader
class PointDataGetLoader (context: Context, args: Bundle?) : AsyncTaskLoader<PointData>(context) {

    // 変数定義
    private val LOG_TAG = PointDataGetLoader::class.java.simpleName // ログ用にクラス名を取得
    private var toUsername: String? = args?.getString("toUsername") // Toユーザー名
    private var fromUsername: String? = args?.getString("fromUsername") // Fromユーザー名
    private var sendSpTo: Int? = args?.getInt("sendSpTo") // Toユーザーに送ったSP


    // loadInBackground
    override fun loadInBackground(): PointData? {
        // 送信Bodyデータを作成
        val sendData = String.format(
            "{\"to_username\": \"%s\"," +
            " \"from_username\": \"%s\"," +
            " \"send_sp_to\": %s}",
            toUsername, fromUsername, sendSpTo)
        Log.d("########## REQUEST CHECK", sendData)

        // 送信先を指定して接続用のインスタンスを作成
        val url = URL(context.getString(R.string.aws_ec2_endpoint) + "point/getpoint/")

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
                val pointData = parsePointData(body)
                return pointData
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


// parsePointData
fun parsePointData(body: String) : PointData? {
    // レスポンスデータからPostDataをインスタンス化して戻す
    val parentJsonObj = JSONObject(body)
    if (parentJsonObj.getInt("statusCode") == 200) {
        val pointData = PointData(
            toUsername = parentJsonObj.getString("to_username"),
            fromUsername = parentJsonObj.getString("from_username"),
            sp = parentJsonObj.getInt("sp"),
            sendSp = parentJsonObj.getInt("send_sp"),
            sendSpTo = parentJsonObj.getInt("send_sp_to"),
            favorite = parentJsonObj.getInt("favorite"),
            favoriteTo = parentJsonObj.getInt("favorite_to")
        )
        return pointData
    }
    return null
}


// インターフェイス定義
interface PointDataGetLoaderInterface {
    fun PointDataGetLoaderOnLoadFinished(data: PointData?)
}

// PointDataGetLoaderCallbacks
class PointDataGetLoaderCallbacks(
    private val context: Context,
    private val pointDataGetLoaderInterface: PointDataGetLoaderInterface
) : LoaderManager.LoaderCallbacks<PointData> {

    // 変数定義
    private val LOG_TAG = PostDataGetLoaderCallbacks::class.java.simpleName // ログ用にクラス名を取得

    override fun onCreateLoader(id: Int, args: Bundle?): android.support.v4.content.Loader<PointData> {
        return PointDataGetLoader(context, args)
    }

    override fun onLoadFinished(loader: android.support.v4.content.Loader<PointData>, data: PointData?) {
        pointDataGetLoaderInterface.PointDataGetLoaderOnLoadFinished(data)
    }

    override fun onLoaderReset(loader: android.support.v4.content.Loader<PointData>) {
        // No Action
    }
}