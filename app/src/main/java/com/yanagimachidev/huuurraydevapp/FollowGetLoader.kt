package com.yanagimachidev.huuurraydevapp


import android.content.Context
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.AsyncTaskLoader
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL


// FollowGetLoader
class FollowGetLoader (context: Context, args: Bundle?) : AsyncTaskLoader<List<AccountData>>(context) {

    // 変数定義
    private val LOG_TAG = FollowGetLoader::class.java.simpleName // ログ用にクラス名を取得
    private var username: String? = args?.getString("username") // ユーザー名
    private var flg: Boolean? = args?.getBoolean("flg") // フラグ
    private var page: Int? = args?.getInt("page") // ページ


    // loadInBackground
    override fun loadInBackground(): List<AccountData>? {
        // 送信Bodyデータを作成
        val sendData = String.format(
            "{\"username\": \"%s\"," +
            " \"flg\":  %s," +
            " \"page\":  %s}",
            username, flg, page)
        Log.d("########## REQUEST CHECK", sendData)

        // 送信先を指定して接続用のインスタンスを作成
        val url = URL(context.getString(R.string.aws_ec2_endpoint) + "hruser/getflw/")

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
                val flwUserList = parseFlwUser(body)
                return flwUserList
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

// parseFlwUser
fun parseFlwUser(body: String) : List<AccountData>? {
    // レスポンスデータからFlwUserをインスタンス化して戻す
    val parentJsonObjsOrg = JSONObject(body)
    if (parentJsonObjsOrg.getInt("statusCode") == 200) {
        val flwUserList = mutableListOf<AccountData>()
        val parentJsonObjs = JSONArray(parentJsonObjsOrg["data"].toString())
        for (i in 0 until parentJsonObjs.length()) {
            val parentJsonObj = parentJsonObjs.getJSONObject(i)
            val flwUser = AccountData(
                username = parentJsonObj.getString("username"),
                dispName = parentJsonObj.getString("disp_name"),
                accountImage = parentJsonObj.getString("account_image"),
                wpOn = parentJsonObj.getString("wp_on"),
                wpName = "",
                peFlg = 0
            )
            if (flwUser.wpOn != "0") {
                flwUser.wpName = parentJsonObj.getString("wp1_name")
            }
            flwUserList.add(flwUser)
        }
        if (!flwUserList.isNullOrEmpty()) {
            return flwUserList
        }
    }
    return null
}


// インターフェイス定義
interface FollowGetLoaderInterface {
    fun FollowGetLoaderOnLoadFinished(data: List<AccountData>?)
}

// FollowGetLoaderCallbacks
class FollowGetLoaderCallbacks(
    private val context: Context,
    private val followGetLoaderInterface: FollowGetLoaderInterface
) : LoaderManager.LoaderCallbacks<List<AccountData>> {
    // 変数定義
    private val LOG_TAG = FollowGetLoaderCallbacks::class.java.simpleName // ログ用にクラス名を取得

    override fun onCreateLoader(id: Int, args: Bundle?): android.support.v4.content.Loader<List<AccountData>> {
        return FollowGetLoader(context, args)
    }

    override fun onLoadFinished(loader: android.support.v4.content.Loader<List<AccountData>>, data: List<AccountData>?) {
        followGetLoaderInterface.FollowGetLoaderOnLoadFinished(data)
    }

    override fun onLoaderReset(loader: android.support.v4.content.Loader<List<AccountData>>) {
        // No Action
    }
}