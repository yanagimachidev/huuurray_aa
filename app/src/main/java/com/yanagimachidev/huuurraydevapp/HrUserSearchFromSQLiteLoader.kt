package com.yanagimachidev.huuurraydevapp


import android.support.v4.app.LoaderManager
import android.support.v4.content.AsyncTaskLoader
import android.content.Context
import android.os.Bundle


// HrUserSearchFromSQLiteLoader
class HrUserSearchFromSQLiteLoader (context: Context, args: Bundle?) : AsyncTaskLoader<List<AccountData>>(context) {

    // 変数定義
    private val LOG_TAG = HrUserSearchFromSQLiteLoader::class.java.simpleName // ログ用にクラス名を取得
    private var cache: List<AccountData>? = null // キャッシュ
    private var queryString: String? = args?.getString("queryString") // 検索文字列
    private var page : Int? = args?.getInt("page") // ページ


    // loadInBackground
    override fun loadInBackground(): List<AccountData>? {
        // SQLiteからデータを取得
        val datas = SearchHrUser(context, page!!, queryString!!)
        return datas
    }

    override fun deliverResult(data: List<AccountData>?) {
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
interface HrUserSearchFromSQLiteLoaderInterface {
    fun HrUserSearchFromSQLiteLoaderOnLoadFinished(data:  List<AccountData>?)
}

// HrUserSearchFromSQLiteLoaderCallbacks
class HrUserSearchFromSQLiteLoaderCallbacks(
    private val context: Context,
    private val hrUserSearchFromSQLiteLoaderInterface: HrUserSearchFromSQLiteLoaderInterface
) : LoaderManager.LoaderCallbacks<List<AccountData>> {

    // 変数定義
    private val LOG_TAG = MessageGetFromSQLiteLoaderCallbacks::class.java.simpleName // ログ用にクラス名を取得

    override fun onCreateLoader(id: Int, args: Bundle?):  android.support.v4.content.Loader<List<AccountData>> {
        return HrUserSearchFromSQLiteLoader(context, args)
    }

    override fun onLoadFinished(loader: android.support.v4.content.Loader<List<AccountData>>, data: List<AccountData>?) {
        hrUserSearchFromSQLiteLoaderInterface.HrUserSearchFromSQLiteLoaderOnLoadFinished(data)
    }

    override fun onLoaderReset(loader: android.support.v4.content.Loader<List<AccountData>>) {
        // No Action
    }
}