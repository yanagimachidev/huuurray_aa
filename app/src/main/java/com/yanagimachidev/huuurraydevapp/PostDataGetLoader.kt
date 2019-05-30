package com.yanagimachidev.huuurraydevapp


import android.content.Context
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.AsyncTaskLoader
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL


// PostData
data class PostData(
    val id: Int,
    val content: String,
    val createdAt: String,
    val modifiedAt: String,
    val username: String,
    val dispName: String,
    val accountImage: String,
    var favorite: Boolean,
    var favoriteCnt: Int,
    val wpOn: String,
    var wpName: String,
    var clickType: String?,
    var position: Int?
)


// PostDataGetLoader
class PostDataGetLoader (context: Context, args: Bundle?) : AsyncTaskLoader<List<PostData>>(context) {

    // 変数定義
    private val LOG_TAG = PostDataGetLoader::class.java.simpleName // ログ用にクラス名を取得
    private var cache: List<PostData>? = null // キャッシュ
    private var username: String? = args?.getString("username") // ユーザー名
    private var page: Int? = args?.getInt("page") // ページ
    private var feedFlg: Boolean? = args?.getBoolean("feedFlg") // フィードフラグ
    private var favoriteFlg: Boolean? = args?.getBoolean("favoriteFlg") // お気に入りフィルターフラグ


    // loadInBackground
    override fun loadInBackground(): List<PostData>? {
        // 送信Bodyデータを作成
        val sendData = String.format(
            "{\"username\": \"%s\"," +
            " \"feedFlg\": %s," +
            " \"favoriteFlg\": %s," +
            " \"page\": %s}",
            username, feedFlg, favoriteFlg, page)
        Log.d("########## REQUEST CHECK", sendData)

        // 送信先を指定して接続用のインスタンスを作成
        val url = URL(context.getString(R.string.aws_ec2_endpoint) + "postdata/getpost/")

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
                val postData = parsePostDataList(body)
                return postData
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
    //deliverResult
    override fun deliverResult(data: List<PostData>?) {
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


// parsePostDataList
fun parsePostDataList(body: String) : List<PostData>? {
    // レスポンスデータからPostDataをインスタンス化して戻す
    val parentJsonObjsOrg = JSONObject(body)
    if (parentJsonObjsOrg.getInt("statusCode") == 200) {
        val postDataList = mutableListOf<PostData>()
        val parentJsonObjs = JSONArray(parentJsonObjsOrg["data"].toString())
        for (i in 0 until parentJsonObjs.length()) {
            val parentJsonObj = parentJsonObjs.getJSONObject(i)
            val postData = PostData(
                id = parentJsonObj.getInt("id"),
                content = parentJsonObj.getString("content"),
                createdAt = parentJsonObj.getString("created_at"),
                modifiedAt = parentJsonObj.getString("modified_at"),
                username = parentJsonObj.getString("username"),
                dispName = parentJsonObj.getString("disp_name"),
                accountImage = parentJsonObj.getString("account_image"),
                favorite = parentJsonObj.getBoolean("favorite"),
                favoriteCnt = parentJsonObj.getInt("favorite_cnt"),
                wpOn = parentJsonObj.getString("wp_on"),
                wpName = "",
                clickType = null,
                position = null
            )
            if (postData.wpOn != "0") {
                postData.wpName = parentJsonObj.getString("wp1_name")
            }
            postDataList.add(postData)
        }
        if (!postDataList.isNullOrEmpty()) {
            return postDataList
        }
    }
    return null
}


// インターフェイス定義
interface PostDataGetLoaderInterface {
    fun PostDataGetLoaderOnLoadFinished(data: List<PostData>?)
}

// PostDataGetLoaderCallbacks
class PostDataGetLoaderCallbacks(
    private val context: Context,
    private val postDataGetLoaderInterface: PostDataGetLoaderInterface
) : LoaderManager.LoaderCallbacks<List<PostData>> {

    // 変数定義
    private val LOG_TAG = PostDataGetLoaderCallbacks::class.java.simpleName // ログ用にクラス名を取得

    override fun onCreateLoader(id: Int, args: Bundle?): android.support.v4.content.Loader<List<PostData>> {
        return PostDataGetLoader(context, args)
    }

    override fun onLoadFinished(loader: android.support.v4.content.Loader<List<PostData>>, data: List<PostData>?) {
        postDataGetLoaderInterface.PostDataGetLoaderOnLoadFinished(data)
    }

    override fun onLoaderReset(loader: android.support.v4.content.Loader<List<PostData>>) {
        // No Action
    }
}
