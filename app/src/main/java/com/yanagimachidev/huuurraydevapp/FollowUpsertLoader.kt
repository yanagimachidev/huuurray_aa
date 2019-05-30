package com.yanagimachidev.huuurraydevapp


import android.content.Context
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.AsyncTaskLoader
import android.util.Log
import java.net.URL


// FollowUpsertLoader
class FollowUpsertLoader (context: Context, args: Bundle?) : AsyncTaskLoader<Boolean>(context) {

    // 変数定義
    private val LOG_TAG = FollowUpsertLoader::class.java.simpleName // ログ用にクラス名を取得
    private var toUsername: String? = args?.getString("toUsername") // Toユーザー名
    private var fromUsername: String? = args?.getString("fromUsername") // Fromユーザー名
    private var flg: Boolean? = args?.getBoolean("flg") // フラグ


    // loadInBackground
    override fun loadInBackground(): Boolean {
        // 送信Bodyデータを作成
        val sendData = String.format(
            "{\"to_username\": \"%s\"," +
            " \"from_username\": \"%s\"," +
            " \"flg\":  %s}",
            toUsername, fromUsername, flg)
        Log.d("########## REQUEST CHECK", sendData)

        // 送信先を指定して接続用のインスタンスを作成
        val url = URL(context.getString(R.string.aws_ec2_endpoint) + "hruser/follow/")

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
                var resultSqlite = false
                if (resultAws) {
                    // ユーザーのお気に入り登録状況を更新
                    var status = "0"
                    if (flg!!) {
                        status = "1"
                    }
                    val newDataString = mutableMapOf<String, String>()
                    val newDataInt = mutableMapOf<String, Int?>()
                    val newDataDouble = mutableMapOf<String, Double?>()
                    newDataString["username"] = toUsername!!
                    newDataString["favorite_user"] = status
                    resultSqlite = upsertHrUser(context, newDataString, newDataInt, newDataDouble)
                } else {
                    Log.e(LOG_TAG, "Failed to Save Follow Status")
                }
                if (resultSqlite) {
                    return true
                }
            }
        }

        return false
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
interface FollowUpsertLoaderInterface {
    fun FollowUpsertLoaderOnLoadFinished(data: Boolean?)
}

// FollowUpsertLoaderCallbacks
class FollowUpsertLoaderCallbacks(
    private val context: Context,
    private val followUpsertLoaderInterface: FollowUpsertLoaderInterface
) : LoaderManager.LoaderCallbacks<Boolean> {
    // 変数定義
    private val LOG_TAG = FollowUpsertLoaderCallbacks::class.java.simpleName // ログ用にクラス名を取得

    override fun onCreateLoader(id: Int, args: Bundle?): android.support.v4.content.Loader<Boolean> {
        return FollowUpsertLoader(context, args)
    }

    override fun onLoadFinished(loader: android.support.v4.content.Loader<Boolean>, data: Boolean?) {
        if (data != null) {
            followUpsertLoaderInterface.FollowUpsertLoaderOnLoadFinished(data)
        } else {
            Log.i(LOG_TAG, "NO POST DATA")
        }
    }

    override fun onLoaderReset(loader: android.support.v4.content.Loader<Boolean>) {
        // No Action
    }
}