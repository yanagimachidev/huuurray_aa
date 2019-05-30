package com.yanagimachidev.huuurraydevapp


import android.support.v4.content.AsyncTaskLoader
import android.support.v4.app.LoaderManager
import android.content.Context
import android.content.Loader
import android.os.Bundle
import android.util.Log
import java.net.URL


// PointUpsertLoader
class PointUpsertLoader (context: Context, args: Bundle?) : AsyncTaskLoader<Boolean>(context) {

    // 変数定義
    private val LOG_TAG = PostDataGetLoader::class.java.simpleName // ログ用にクラス名を取得
    private var toUsername: String? = args?.getString("toUsername") // Toユーザー名
    private var fromUsername: String? = args?.getString("fromUsername") // Fromユーザー名
    private var point: Int? = args?.getInt("point") // ポイント
    private var type: String? = args?.getString("type") // タイプ
    private var cache:Boolean? = null // キャッシュ


    // loadInBackground
    override fun loadInBackground(): Boolean {
        // 送信Bodyデータを作成
        val sendData = String.format(
            "{\"to_username\": \"%s\"," +
            " \"from_username\": \"%s\"," +
            " \"point\": %s," +
            " \"type\":  \"%s\"}",
            toUsername, fromUsername, point.toString(), type)
        Log.d("########## REQUEST CHECK", sendData)

        // 送信先を指定して接続用のインスタンスを作成
        val url = URL(context.getString(R.string.aws_ec2_endpoint) + "point/upsert/")

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
                var resultSqlite2 = false
                if (resultAws) {
                    // これまでに送ったSPを更新
                    val oldDataTo = queryHrUser(context, toUsername!!)
                    val newDataStringTo = mutableMapOf<String, String>()
                    val newDataIntTo = mutableMapOf<String, Int>()
                    val newDataDoubleTo = mutableMapOf<String, Double>()
                    newDataStringTo["username"] = toUsername!!
                    if (oldDataTo.containsKey("send_sp_to")){
                        newDataIntTo["send_sp_to"] = oldDataTo["send_sp_to"] as Int + point!!
                    } else {
                        newDataIntTo["send_sp_to"] = point!!
                    }
                    val resultSqlite1 = upsertHrUser(context, newDataStringTo, newDataIntTo, newDataDoubleTo)
                    if (resultSqlite1) {
                        if (type == "st") {
                            // 現在の利用可能SPを減算
                            val oldDataFrom = queryHrUser(context, fromUsername!!)
                            val newDataStringFrom = mutableMapOf<String, String>()
                            val newDataIntFrom = mutableMapOf<String, Int>()
                            val newDataDoubleFrom = mutableMapOf<String, Double>()
                            newDataStringFrom["username"] = fromUsername!!
                            newDataIntFrom["st_sp"] = oldDataFrom["st_sp"] as Int - point!!
                            resultSqlite2 = upsertHrUser(context, newDataStringFrom, newDataIntFrom, newDataDoubleFrom)
                        } else if (type == "ci") {
                            // 最終チェックイン日時を更新
                            val nowTime = System.currentTimeMillis().toString()
                            val newDataStringCi = mutableMapOf<String, String>()
                            val newDataIntCi = mutableMapOf<String, Int>()
                            val newDataDoubleCi = mutableMapOf<String, Double>()
                            newDataStringCi["username"] = toUsername!!
                            newDataStringCi["last_check_in"] = nowTime
                            resultSqlite2 = upsertHrUser(context, newDataStringCi, newDataIntCi, newDataDoubleCi)
                        } else if (type == "mg") {
                            resultSqlite2 = true
                        }
                    }
                } else {
                    Log.e(LOG_TAG, "Failed to Save Point Data.")
                }
                if (resultSqlite2) {
                    return true
                }
            }
        }
        return false
    }

    override fun deliverResult(data: Boolean?) {
        if (isReset) return
        cache = data
        super.deliverResult(data)
    }

    override fun onStartLoading() {
        if (cache != null) {
            deliverResult(cache)
        }
        if (takeContentChanged() || cache == null) {
            forceLoad()
        }
    }

    override fun onStopLoading() {
        cancelLoad()
    }

    override fun onReset() {
        super.onReset()
        onStopLoading()
        cache = null
    }
}


interface PointUpsertLoaderInterface {
    fun PointUpsertLoaderOnLoadFinished(data: Boolean?)
}

// PointUpsertLoaderCallbacks
class PointUpsertLoaderCallbacks(
    private val context: Context,
    private val pointUpsertLoaderInterface: PointUpsertLoaderInterface
) : LoaderManager.LoaderCallbacks<Boolean> {

    // 変数定義
    private val LOG_TAG = PointUpsertLoaderCallbacks::class.java.simpleName // ログ用にクラス名を取得

    override fun onCreateLoader(id: Int, args: Bundle?): android.support.v4.content.Loader<Boolean> {
        return PointUpsertLoader(context, args)
    }

    override fun onLoadFinished(loader: android.support.v4.content.Loader<Boolean>, data: Boolean?) {
        pointUpsertLoaderInterface.PointUpsertLoaderOnLoadFinished(data)
    }

    override fun onLoaderReset(loader: android.support.v4.content.Loader<Boolean>) {
        // No Action
    }
}
