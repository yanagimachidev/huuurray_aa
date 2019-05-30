package com.yanagimachidev.huuurraydevapp


import android.content.Context
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.AsyncTaskLoader
import android.util.Log
import org.json.JSONObject
import java.net.URL


// HrUser
data class HrUser(
    val username: String,
    val dispName: String,
    val sex: String,
    val birthday: String,
    val profile: String
)


// HrUserUpsertLoader
class HrUserUpsertLoader (context: Context, args: Bundle?) : AsyncTaskLoader<HrUser>(context) {

    // 変数定義
    private val LOG_TAG = HrUserUpsertLoader::class.java.simpleName // ログ用にクラス名を取得
    private var username = args?.getString("username") // ユーザー名
    private var dispName = args?.getString("dispName") // 表示名
    private var sex = args?.getString("sex") // 性別
    private var birthday  = args?.getString("birthday") // 誕生日
    private var profile = args?.getString("profile") // プロフィール


    // loadInBackground
    override fun loadInBackground(): HrUser? {
        // 送信Bodyデータを作成
        profile = profile?.replace("\r", "\\r")
        profile = profile?.replace("\n", "\\n")
        val sendData = String.format(
            "{\"username\": \"%s\"," +
            " \"disp_name\": \"%s\"," +
            " \"sex\": \"%s\"," +
            " \"birthday\": \"%s\"," +
            " \"profile\": \"%s\"}",
            username, dispName, sex, birthday, profile)
        Log.d("########## REQUEST CHECK", sendData)

        // 送信先を指定して接続用のインスタンスを作成
        val url = URL(context.getString(R.string.aws_ec2_endpoint) + "hruser/upsert/")

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
                val hrUser = parseHrUser(body)
                // SQLiteに書き込み
                var result: Boolean = false
                if (hrUser != null) {
                    val newDataString = mutableMapOf<String, String>()
                    val newDataInt = mutableMapOf<String, Int>()
                    val newDataDouble = mutableMapOf<String, Double>()
                    newDataString["username"] = hrUser.username
                    newDataString["disp_name"] = hrUser.dispName
                    newDataString["sex"] = hrUser.sex
                    newDataString["birthday"] = hrUser.birthday
                    newDataString["profile"] = hrUser.profile
                    result = upsertHrUser(context, newDataString, newDataInt, newDataDouble)
                } else {
                    Log.e(LOG_TAG, "Failed to Save User Data.")
                }
                if (result) {
                    return hrUser
                }
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


// parseHrUser
fun parseHrUser(body: String) : HrUser? {
    // レスポンスデータからHrUserをインスタンス化して戻す
    val parentJsonObj = JSONObject(body)
    if (parentJsonObj.getInt("statusCode") == 200) {
        val hrUser = HrUser(
            username = parentJsonObj.getString("username"),
            dispName = parentJsonObj.getString("disp_name"),
            sex = parentJsonObj.getString("sex"),
            birthday = parentJsonObj.getString("birthday"),
            profile = parentJsonObj.getString("profile")
        )
        return hrUser
    } else {
        return null
    }
}


// インターフェイス定義
interface HrUserUpsertLoaderInterface {
    fun HrUserUpsertLoaderOnLoadFinished(data: HrUser?)
}

// HrUserUpsertLoaderCallbacks
class HrUserUpsertLoaderCallbacks(
    private val context: Context,
    private val hrUserUpsertLoaderInterface: HrUserUpsertLoaderInterface
) : LoaderManager.LoaderCallbacks<HrUser> {

    // 変数定義
    private val LOG_TAG = HrUserUpsertLoaderCallbacks::class.java.simpleName // ログ用にクラス名を取得

    override fun onCreateLoader(id: Int, args: Bundle?): android.support.v4.content.Loader<HrUser> {
        return HrUserUpsertLoader(context, args)
    }

    override fun onLoadFinished(loader: android.support.v4.content.Loader<HrUser>, data: HrUser?) {
        hrUserUpsertLoaderInterface.HrUserUpsertLoaderOnLoadFinished(data)
    }

    override fun onLoaderReset(loader: android.support.v4.content.Loader<HrUser>) {
        // No Action
    }
}