package com.yanagimachidev.huuurraydevapp


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


// SearchFragment
class SearchFragment : Fragment(),
    HrUserSearchFromSQLiteLoaderInterface {

    // 変数定義
    private val LOG_TAG = SearchFragment::class.java.simpleName // ログ用にクラス名を取得
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: SearchCellAdapter
    private var datas = mutableListOf<AccountData>()
    private var queryString = ""
    private var onPauseFlg = false
    private val onQueryTextListener = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            return false
        }

        override fun onQueryTextChange(query: String?): Boolean {
            searchView.isIconified = false
            if (query != null && query!= "") {
                // データの取得
                datas.clear()
                //insertProgressBarOrEnd(-1)
                queryString = query
                startHrUserSearchFromSQLiteLoader(0, query)
            } else {
                datas.clear()
                adapter.notifyDataSetChanged()
            }
            return false
        }
    }


    // onCreateView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Viewを指定
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        searchView = view.findViewById<SearchView>(R.id.search)

        // searchViewにイベントリスナーを登録
        searchView.setOnQueryTextListener(onQueryTextListener)
        searchView.isIconified = false

        searchView.setOnClickListener { searchView.isIconified = false }

        // recyclerViewを設定
        recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view)
        layoutManager = LinearLayoutManager(activity as Context)
        recyclerView.layoutManager = layoutManager
        // 一旦空のAdapterをセット
        adapter = SearchCellAdapter(activity as Context, datas) {}
        recyclerView.adapter = adapter

        return view
    }

    // onPause
    override fun onPause() {
        super.onPause()
        onPauseFlg = true
    }


    // startHrUserSearchFromSQLiteLoader
    private fun startHrUserSearchFromSQLiteLoader (page: Int, queryString: String) {
        // 引数に値を渡してローダーを起動
        val bundle = Bundle()
        bundle.putInt("page", page)
        bundle.putString("queryString", queryString)
        loaderManager.restartLoader(13321, bundle,
            HrUserSearchFromSQLiteLoaderCallbacks(activity as Context, this))
    }

    // HrUserSearchFromSQLiteLoaderOnLoadFinished
    override fun HrUserSearchFromSQLiteLoaderOnLoadFinished(data: List<AccountData>?) {
        if (onPauseFlg) {
            onPauseFlg = false
        } else {
            if (datas.size == 0) {
                if (data != null) {
                    datas.addAll(data)
                }
                // アダプターのセット
                adapter = SearchCellAdapter(activity as Context, datas) {AccountData->
                    startAccountActivity(activity as Context, AccountData.username, false)
                }
                recyclerView.adapter = adapter
                // スクロールリスナーを登録
                recyclerView.addOnScrollListener(newEndlessScrollListener(layoutManager))
            } else {
                if (data != null) {
                    val positionStart = datas.size + 1
                    datas.addAll(data)
                    adapter.notifyItemRangeInserted(positionStart, data.size)
                }
            }
        }
    }


    // newEndlessScrollListener
    private fun newEndlessScrollListener(layoutManager: LinearLayoutManager) : EndlessScrollListener {
        val endlessScrollListener = object : EndlessScrollListener(layoutManager) {
            override fun onLoadMore(currentPage: Int) {
                startHrUserSearchFromSQLiteLoader(currentPage, queryString)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                // No Action
            }
        }
        return endlessScrollListener
    }

    // insertProgressBarOrEnd
    private fun insertProgressBarOrEnd(flg: Int) {
        val positionStart = datas.size + 1
        val progressBarOrEnd = AccountData(
            username = "",
            dispName = "",
            accountImage = "",
            wpOn = "",
            wpName = "",
            peFlg = flg
        )
        datas.add(progressBarOrEnd)
        adapter.notifyItemRangeInserted(positionStart, 1)
    }
}

fun newSearchFragment() : SearchFragment{
    return SearchFragment()
}
