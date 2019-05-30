package com.yanagimachidev.huuurraydevapp


import android.content.Context
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.AsyncTaskLoader
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL


// RankData
data class RankData(
    val username: String,
    val dispName: String,
    val accountImage: String,
    val profile: String,
    val point: Int,
    val wpOn: String,
    var wpName: String,
    var wpCategory: String
)


// RankingGetLoader
class RankingGetLoader (context: Context, args: Bundle?) : AsyncTaskLoader<List<RankData>>(context) {

    // 変数定義
    private val LOG_TAG = RankingGetLoader::class.java.simpleName // ログ用にクラス名を取得
    private var cache: List<RankData>? = null // キャッシュ
    private val position =  args?.getInt("position") // ポジション
    private var page: Int? = args?.getInt("page") // ページ


    override fun loadInBackground(): List<RankData>? {
        // 送信Bodyデータを作成
        val sendData = String.format(
            "{\"position\": %s," +
            " \"page\": %s}",
            position, page)
        Log.d("########## REQUEST CHECK", sendData)

        // 送信先を指定して接続用のインスタンスを作成
        val url = URL(context.getString(R.string.aws_ec2_endpoint) + "hruser/ranking/")

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
                val rankData = parseRankDataList(body)
                return rankData
            }
        }
        return null
    }


    override fun deliverResult(data: List<RankData>?) {
        if (isReset) return
        cache = data
        super.deliverResult(data)
    }

    override fun onStartLoading() {
        //forceLoad()
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


// parseRankDataList
fun parseRankDataList(body: String) : List<RankData>? {
    // レスポンスデータからPostDataをインスタンス化して戻す
    val parentJsonObjsOrg = JSONObject(body)
    if (parentJsonObjsOrg.getInt("statusCode") == 200) {
        val rankDataList = mutableListOf<RankData>()
        val parentJsonObjs = JSONArray(parentJsonObjsOrg["data"].toString())
        for (i in 0 until parentJsonObjs.length()) {
            val parentJsonObj = parentJsonObjs.getJSONObject(i)
            val rankData = RankData(
                username = parentJsonObj.getString("username"),
                dispName = parentJsonObj.getString("disp_name"),
                accountImage = parentJsonObj.getString("account_image"),
                profile = parentJsonObj.getString("profile"),
                point = parentJsonObj.getInt("s_point"),
                wpOn = parentJsonObj.getString("wp_on"),
                wpName = "",
                wpCategory = ""
            )
            if (rankData.wpOn != "0") {
                rankData.wpName = parentJsonObj.getString("wp1_name")
                rankData.wpCategory = parentJsonObj.getString("wp1_category")
            }
            rankDataList.add(rankData)
        }
        if (!rankDataList.isNullOrEmpty()) {
            return rankDataList
        }
    }
    return null
}


// インターフェイス定義
interface RankingGetLoaderInterface {
    fun RankingGetLoaderOnLoadFinished(data: List<RankData>?)
}

// RankingGetLoaderCallbacks
class RankingGetLoaderCallbacks(
    private val context: Context,
    private val rankingGetLoaderInterface: RankingGetLoaderInterface
) : LoaderManager.LoaderCallbacks<List<RankData>> {

    // 変数定義
    private val LOG_TAG = RankingGetLoaderCallbacks::class.java.simpleName // ログ用にクラス名を取得

    override fun onCreateLoader(id: Int, args: Bundle?): android.support.v4.content.Loader<List<RankData>> {
        return RankingGetLoader(context, args)
    }

    override fun onLoadFinished(loader: android.support.v4.content.Loader<List<RankData>>, data: List<RankData>?) {
        rankingGetLoaderInterface.RankingGetLoaderOnLoadFinished(data)
    }

    override fun onLoaderReset(loader: android.support.v4.content.Loader<List<RankData>>) {
        // No Action
    }
}