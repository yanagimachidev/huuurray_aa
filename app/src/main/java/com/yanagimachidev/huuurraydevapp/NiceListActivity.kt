package com.yanagimachidev.huuurraydevapp


import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View


// NiceListActivity
class NiceListActivity : AppCompatActivity(),
    NiceGetLoaderInterface {

    // 変数定義
    private val LOG_TAG = NiceListActivity::class.java.simpleName // ログ用にクラス名を取得
    private val scrollDisableListener = ScrollDisableListener()
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: NiceCellAdapter
    private var datas = mutableListOf<AccountData>()
    private var onPauseFlg = false
    private var id = 0


    // onCreate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nice_list)

        // StatusBarを表示、透過する設定
        findViewById<View>(android.R.id.content).systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        // intentの取得
        id = intent.getIntExtra("id", 0)

        // recyclerViewを設定
        recyclerView = findViewById<RecyclerView>(R.id.nice_list)
        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        // 一旦空のAdapterをセット
        adapter = NiceCellAdapter(this, datas) {}
        recyclerView.adapter = adapter

        // データの取得
        datas.clear()
        insertProgressBarOrEnd(-1)
        startNiceGetLoader(id, 0)

        // リフレッシュ時にもデータを再取得
        mSwipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)
        mSwipeRefreshLayout.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            // スクロールを一旦禁止
            recyclerView.addOnItemTouchListener(scrollDisableListener)
            // スクロールリスナーを再登録
            recyclerView.addOnScrollListener(newEndlessScrollListener(layoutManager))
            // 引数に値を渡してローダーを起動
            datas.clear()
            startNiceGetLoader(id, 0)
        })
    }

    // onPause
    override fun onPause() {
        super.onPause()
        onPauseFlg = true
    }


    // startNiceGetLoader
    private fun startNiceGetLoader(id: Int, page: Int) {
        // 引数に値を渡してローダーを起動
        val bundle = Bundle()
        bundle.putInt("id", id)
        bundle.putInt("page", page)
        supportLoaderManager.restartLoader(13316, bundle,
            NiceGetLoaderCallbacks(this, this))
    }

    // NiceGetLoaderOnLoadFinished
    override fun NiceGetLoaderOnLoadFinished(data: List<AccountData>?) {
        if (onPauseFlg) {
            onPauseFlg = false
        } else {
            // ProgressBarの削除
            if (datas.isNotEmpty()) {
                val datasIndex = datas.size -1
                if (datas[datasIndex].peFlg == -1) {
                    datas.removeAt(datasIndex)
                    adapter.notifyItemRangeRemoved(datasIndex, 1)
                }
            }

            if (datas.size == 0) {
                if (data != null) {
                    datas.addAll(data)
                }
                // アダプターのセット
                adapter = NiceCellAdapter(this, datas) {NiceData->
                    startAccountActivity(this, NiceData.username, false)
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

            // 終端を判定して追加
            if (data != null && data.isNotEmpty()) {
                insertProgressBarOrEnd(-1)
            }
        }
        recyclerView.removeOnItemTouchListener(scrollDisableListener)
        mSwipeRefreshLayout.setRefreshing(false)
    }


    // newEndlessScrollListener
    private fun newEndlessScrollListener(layoutManager: LinearLayoutManager) : EndlessScrollListener {
        val endlessScrollListener = object : EndlessScrollListener(layoutManager) {
            override fun onLoadMore(currentPage: Int) {
                startNiceGetLoader(id, currentPage)
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                refreshStatusUpdate(layoutManager, mSwipeRefreshLayout)
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
