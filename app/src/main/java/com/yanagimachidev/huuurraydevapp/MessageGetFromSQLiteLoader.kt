package com.yanagimachidev.huuurraydevapp


import android.support.v4.app.LoaderManager
import android.support.v4.content.AsyncTaskLoader
import android.content.Context
import android.os.Bundle


// MessageGetLoader
class MessageGetFromSQLiteLoader (context: Context, args: Bundle?) : AsyncTaskLoader<List<MessageDispData>>(context) {

    // 変数定義
    private val LOG_TAG = MessageGetFromSQLiteLoader::class.java.simpleName // ログ用にクラス名を取得
    private var cache: List<MessageDispData>? = null // キャッシュ
    private var username: String? = args?.getString("username") // ユーザー名
    private var tfFlg : Int? = args?.getInt("tfFlg") // 受信メッセージフラグ
    private var page : Int? = args?.getInt("page") // ページ


    // loadInBackground
    override fun loadInBackground(): List<MessageDispData>? {
        // SQLiteからデータを取得
        val datas = queryAllMessage(context, tfFlg!!, page!!, username!!)
        return datas
    }

    override fun deliverResult(data: List<MessageDispData>?) {
        if (isReset) return
        cache = data
        super.deliverResult(data)
    }

    override fun onStartLoading() {
        forceLoad()
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

// インターフェイス定義
interface MessageGetFromSQLiteLoaderInterface {
    fun MessageGetFromSQLiteLoaderOnLoadFinished(data:  List<MessageDispData>?)
}

// MessageGetFromSQLiteLoaderCallbacks
class MessageGetFromSQLiteLoaderCallbacks(
    private val context: Context,
    private val messageGetFromSQLiteLoaderInterface: MessageGetFromSQLiteLoaderInterface
) : LoaderManager.LoaderCallbacks<List<MessageDispData>> {

    // 変数定義
    private val LOG_TAG = MessageGetFromSQLiteLoaderCallbacks::class.java.simpleName // ログ用にクラス名を取得

    override fun onCreateLoader(id: Int, args: Bundle?):  android.support.v4.content.Loader<List<MessageDispData>> {
        return MessageGetFromSQLiteLoader(context, args)
    }

    override fun onLoadFinished(loader: android.support.v4.content.Loader<List<MessageDispData>>, data: List<MessageDispData>?) {
        messageGetFromSQLiteLoaderInterface.MessageGetFromSQLiteLoaderOnLoadFinished(data)
    }

    override fun onLoaderReset(loader: android.support.v4.content.Loader<List<MessageDispData>>) {
        // No Action
    }
}