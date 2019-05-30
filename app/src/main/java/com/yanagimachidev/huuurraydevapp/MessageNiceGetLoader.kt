package com.yanagimachidev.huuurraydevapp


import android.content.Context
import android.os.Bundle
import android.support.v4.content.AsyncTaskLoader
import android.support.v4.app.LoaderManager
import android.util.Log
import java.net.URL


// MessageNiceGetLoader
class MessageNiceGetLoader (context: Context, args: Bundle?) : AsyncTaskLoader<List<AccountData>>(context) {

    // 変数定義
    private val LOG_TAG = MessageNiceGetLoader::class.java.simpleName // ログ用にクラス名を取得
    private var id: Int? = args?.getInt("id") // 投稿ID
    private var page: Int? = args?.getInt("page") // ページ


    // loadInBackground
    override fun loadInBackground(): List<AccountData>? {
        // 送信Bodyデータを作成
        val sendData = String.format(
            "{\"id\": %s," +
            " \"page\": %s}",
            id, page)
        Log.d("########## REQUEST CHECK", sendData)

        // 送信先を指定して接続用のインスタンスを作成
        val url = URL(context.getString(R.string.aws_ec2_endpoint) + "message/getnice/")

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
                val niceData = parseNiceData(body)
                return niceData
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


// インターフェイス定義
interface MessageNiceGetLoaderInterface {
    fun MessageNiceGetLoaderOnLoadFinished(data: List<AccountData>?)
}

// MessageNiceGetLoaderCallbacks
class MessageNiceGetLoaderCallbacks(
    private val context: Context,
    private val messageNiceGetLoaderInterface: MessageNiceGetLoaderInterface
) : LoaderManager.LoaderCallbacks<List<AccountData>> {
    // 変数定義
    private val LOG_TAG = MessageNiceGetLoaderCallbacks::class.java.simpleName // ログ用にクラス名を取得

    override fun onCreateLoader(id: Int, args: Bundle?): android.support.v4.content.Loader<List<AccountData>> {
        return MessageNiceGetLoader(context, args)
    }

    override fun onLoadFinished(loader: android.support.v4.content.Loader<List<AccountData>>, data: List<AccountData>?) {
        messageNiceGetLoaderInterface.MessageNiceGetLoaderOnLoadFinished(data)
    }

    override fun onLoaderReset(loader: android.support.v4.content.Loader<List<AccountData>>) {
        // No Action
    }
}