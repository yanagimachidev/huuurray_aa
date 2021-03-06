package com.yanagimachidev.huuurraydevapp


import android.content.Context
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.AsyncTaskLoader
import android.util.Log
import java.net.URL


// MessageUpsertLoader
class MessageUpsertLoader (context: Context, args: Bundle?) : AsyncTaskLoader<Boolean>(context) {

    // 変数定義
    private val LOG_TAG = MessageUpsertLoader::class.java.simpleName // ログ用にクラス名を取得
    private var toUsername = args?.getString("toUsername") // 送信先ユーザー名
    private var fromUsername = args?.getString("fromUsername") // 送信元ユーザー名
    private var content = args?.getString("content") // 本文


    // loadInBackground
    override fun loadInBackground(): Boolean? {
        // 送信Bodyデータを作成
        content = content?.replace("\r", "\\r")
        content = content?.replace("\n", "\\n")
        val sendData = String.format(
            "{\"to_username\": \"%s\"," +
            " \"from_username\": \"%s\"," +
            " \"content\": \"%s\"}",
            toUsername, fromUsername, content)
        Log.d("########## REQUEST CHECK", sendData)

        // 送信先を指定して接続用のインスタンスを作成
        val url = URL(context.getString(R.string.aws_ec2_endpoint) + "message/upsert/")

        // 送信クライアントを作成して接続
        val response = MyOkhttpConnection(sendData, url)

        // レスポンスを処理
        if (response != null) {
            // ステータスコードが正常値の場合には、データを返す
            val statusCode = response.code()
            Log.i(LOG_TAG, "StatusCode" + statusCode)
            if (statusCode == 200) {
                val body = response.body()!!.string()
                Log.d("########## RESPONSE CHECK", body)
                val result = parseResult(body)
                return result
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
interface MessageUpsertLoaderInterface {
    fun MessageUpsertLoaderOnLoadFinished(data:  Boolean?)
}

// MessageUpsertLoaderCallbacks
class MessageUpsertLoaderCallbacks(
    private val context: Context,
    private val messageUpsertLoaderInterface: MessageUpsertLoaderInterface
) : LoaderManager.LoaderCallbacks<Boolean> {

    // 変数定義
    private val LOG_TAG = MessageGetLoaderCallbacks::class.java.simpleName // ログ用にクラス名を取得

    override fun onCreateLoader(id: Int, args: Bundle?):  android.support.v4.content.Loader<Boolean> {
        return MessageUpsertLoader(context, args)
    }

    override fun onLoadFinished(loader: android.support.v4.content.Loader<Boolean>, data: Boolean?) {
        messageUpsertLoaderInterface.MessageUpsertLoaderOnLoadFinished(data)
    }

    override fun onLoaderReset(loader: android.support.v4.content.Loader<Boolean>) {
        // No Action
    }
}