package com.yanagimachidev.huuurraydevapp


import android.support.v4.app.LoaderManager
import android.support.v4.content.AsyncTaskLoader
import android.content.Context
import android.os.Bundle
import android.util.Log
import java.net.URL


// PostDataUpsertLoader
class PostDataUpsertLoader (context: Context, args: Bundle?) : AsyncTaskLoader<Boolean>(context) {

    // 変数定義
    private val LOG_TAG = PostDataUpsertLoader::class.java.simpleName // ログ用にクラス名を取得
    private var id = args?.getInt("id") // ID
    private var username = args?.getString("username") // ユーザー名
    private var content = args?.getString("content") // 本文


    // loadInBackground
    override fun loadInBackground(): Boolean? {
        // 送信Bodyデータを作成
        content = content?.replace("\r", "\\r")
        content = content?.replace("\n", "\\n")
        lateinit var sendData: String
        if (id != 0) {
            sendData = String.format(
                "{\"id\": %s," +
                " \"username\": \"%s\"," +
                " \"content\": \"%s\"}",
                id, username, content)
        } else {
            sendData = String.format(
                "{\"username\": \"%s\"," +
                " \"content\": \"%s\"}",
                username, content)
        }
        Log.d("########## REQUEST CHECK", sendData)

        // 送信先を指定して接続用のインスタンスを作成
        val url = URL(context.getString(R.string.aws_ec2_endpoint) + "postdata/upsert/")

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


    // onStartLoading
    override fun onStartLoading() {
        forceLoad()
    }
    // onStopLoading
    override fun onStopLoading() {
        cancelLoad()
    }
    // onReset
    override fun onReset() {
        super.onReset()
        onStopLoading()
    }
}


// インターフェイス定義
interface PostDataUpsertLoaderInterface {
    fun PostDataUpsertLoaderOnLoadFinished(data: Boolean?)
}

// PostDataUpsertLoaderCallbacks
class PostDataUpsertLoaderCallbacks(
    private val context: Context,
    private val postDataUpsertLoaderInterface: PostDataUpsertLoaderInterface
) : LoaderManager.LoaderCallbacks<Boolean> {

    // 変数定義
    private val LOG_TAG = PostDataGetLoaderCallbacks::class.java.simpleName // ログ用にクラス名を取得

    override fun onCreateLoader(id: Int, args: Bundle?): android.support.v4.content.Loader<Boolean> {
        return PostDataUpsertLoader(context, args)
    }

    override fun onLoadFinished(loader: android.support.v4.content.Loader<Boolean>, data: Boolean?) {
        postDataUpsertLoaderInterface.PostDataUpsertLoaderOnLoadFinished(data)
    }

    override fun onLoaderReset(loader: android.support.v4.content.Loader<Boolean>) {
        // No Action
    }
}
