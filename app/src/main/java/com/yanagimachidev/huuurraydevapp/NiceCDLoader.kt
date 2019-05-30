package com.yanagimachidev.huuurraydevapp


import android.support.v4.app.LoaderManager
import android.support.v4.content.AsyncTaskLoader
import android.content.Context
import android.os.Bundle
import android.util.Log
import java.net.URL


// NiceCDLoader
class NiceCDLoader (context: Context, args: Bundle?) : AsyncTaskLoader<Boolean>(context) {

    // 変数定義
    private val LOG_TAG = NiceCDLoader::class.java.simpleName // ログ用にクラス名を取得
    private var id: Int? = args?.getInt("id") // 投稿ID
    private var username: String? = args?.getString("username") // ユーザー名
    private var favorite: Boolean? = args?.getBoolean("favorite") // フラグ


    // loadInBackground
    override fun loadInBackground(): Boolean {
        // 送信Bodyデータを作成
        val sendData = String.format(
            "{\"id\": %s," +
            " \"username\": \"%s\"," +
            " \"favorite\":  %s}",
            id, username, favorite)
        Log.d("########## REQUEST CHECK", sendData)

        // 送信先を指定して接続用のインスタンスを作成
        val url = URL(context.getString(R.string.aws_ec2_endpoint) + "postdata/nice/")

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
                val resultAws = parseResult(body)
                if (resultAws) {
                    return true
                }
            }
        }
        return false
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
interface NiceCDLoaderInterface {
    fun NiceCDLoaderOnLoadFinished(data: Boolean?)
}

// NiceCDLoaderCallbacks
class NiceCDLoaderCallbacks(
    private val context: Context,
    private val niceCDLoaderInterface: NiceCDLoaderInterface
) : LoaderManager.LoaderCallbacks<Boolean> {

    // 変数定義
    private val LOG_TAG = NiceCDLoaderCallbacks::class.java.simpleName // ログ用にクラス名を取得

    override fun onCreateLoader(id: Int, args: Bundle?): NiceCDLoader {
        return NiceCDLoader(context, args)
    }

    override fun onLoadFinished(loader: android.support.v4.content.Loader<Boolean>, data: Boolean?) {
        niceCDLoaderInterface.NiceCDLoaderOnLoadFinished(data)
    }

    override fun onLoaderReset(loader: android.support.v4.content.Loader<Boolean>) {
        // No Action
    }
}