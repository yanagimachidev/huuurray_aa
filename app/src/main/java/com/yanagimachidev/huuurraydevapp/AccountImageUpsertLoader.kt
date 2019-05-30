package com.yanagimachidev.huuurraydevapp


import android.content.Context
import android.os.Bundle
import android.support.v4.app.LoaderManager
import android.support.v4.content.AsyncTaskLoader
import android.util.Base64
import android.util.Log
import org.json.JSONObject
import java.io.FileOutputStream
import java.net.URL


// AccountImage
data class AccountImage(
    val statusCode: Int,
    val username: String,
    val image: String
)


// AccountImageUpsertLoader
class AccountImageUpsertLoader (context: Context, args: Bundle?) : AsyncTaskLoader<AccountImage>(context) {

    // 変数定義
    private val LOG_TAG = AccountImageUpsertLoader::class.java.simpleName // ログ用にクラス名を取得
    private var username: String? = args?.getString("username") // ユーザー名
    private var imageType: String? = args?.getString("imageType") // 画像種別
    private var imageByteArray: ByteArray? = args?.getByteArray("image") // 画像


    // loadInBackground
    override fun loadInBackground(): AccountImage? {
        // 送信Bodyデータを作成
        var image = Base64.encodeToString(imageByteArray, Base64.DEFAULT)
        image = image?.replace("\r", "")
        image = image?.replace("\n", "")
        val sendData = String.format(
            "{\"username\": \"%s\"," +
            " \"imageType\": \"%s\"," +
            " \"image\": \"%s\"}",
            username, imageType, image)

        // 送信先を指定して接続用のインスタンスを作成
        val url = URL(context.getString(R.string.aws_ec2_endpoint) + "hruser/imgupsert/")

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
                val accountImage = parseAccountImage(body)
                // ローカルファイルとして画像を保存
                var result: Boolean = false
                if (accountImage != null) {
                    // アカウント画像を内部ストレージに保存
                    val fileOutputStream: FileOutputStream = context.openFileOutput(imageType + "_image", Context.MODE_PRIVATE)
                    fileOutputStream.write(imageByteArray)
                    fileOutputStream.close()
                    // 取得したS3上のファイルの名前を保存
                    val newDataString = mutableMapOf<String, String>()
                    val newDataInt = mutableMapOf<String, Int>()
                    val newDataDouble = mutableMapOf<String, Double>()
                    newDataString["username"] = accountImage.username
                    newDataString[imageType + "_image"] = accountImage.image
                    result = upsertHrUser(context, newDataString, newDataInt, newDataDouble)
                } else {
                    Log.e(LOG_TAG, "Failed to Save Image.")
                }
                if (result) {
                    return accountImage
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


// parseAccountImage
fun parseAccountImage(body: String) : AccountImage? {
    // レスポンスデータからAccountImageをインスタンス化して戻す
    val parentJsonObj = JSONObject(body)
    if (parentJsonObj.getInt("statusCode") == 200) {
        val accountImage = AccountImage(
            statusCode = parentJsonObj.getInt("statusCode"),
            username = parentJsonObj.getString("username"),
            image = parentJsonObj.getString("image")
        )
        return accountImage
    } else {
        return null
    }
}



// インターフェイス定義
interface AccountImageUpsertLoaderInterface {
    fun AccountImageUpsertLoaderOnLoadFinished(data: AccountImage?)
}

// AccountImageUpsertLoaderCallbacks
class AccountImageUpsertLoaderCallbacks(
    private val context: Context,
    private val accountImageUpsertLoaderInterface: AccountImageUpsertLoaderInterface
) : LoaderManager.LoaderCallbacks<AccountImage> {

    // 変数定義
    private val LOG_TAG = AccountImageUpsertLoaderCallbacks::class.java.simpleName // ログ用にクラス名を取得

    override fun onCreateLoader(id: Int, args: Bundle?): android.support.v4.content.Loader<AccountImage> {
        return AccountImageUpsertLoader(context, args)
    }

    override fun onLoadFinished(loader: android.support.v4.content.Loader<AccountImage>, data: AccountImage?) {
        accountImageUpsertLoaderInterface.AccountImageUpsertLoaderOnLoadFinished(data)
    }

    override fun onLoaderReset(loader: android.support.v4.content.Loader<AccountImage>) {
        // No Action
    }
}