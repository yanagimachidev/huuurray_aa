package com.yanagimachidev.huuurraydevapp


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


// RankingContentFragment
class RankingContentFragment : Fragment(),
    RankingGetLoaderInterface {

    // 変数定義
    private val LOG_TAG = RankingContentFragment::class.java.simpleName // ログ用にクラス名を取得
    private val scrollDisableListener = ScrollDisableListener()
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: RankingCellAdapter
    private var datas = mutableListOf<RankData>()
    private var position: Int? = null
    private var onPauseFlg = false
    private var loadMax = false


    // リスナーを親へ渡すためのインターフェイス
    interface OnFragmentInteractionListener {
        fun onRankClicked(username: String)
    }
    private var onFragmentInteractionListener: OnFragmentInteractionListener? = null


    // onAttach
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is RankingContentFragment.OnFragmentInteractionListener) {
            onFragmentInteractionListener = context
        } else {
            throw RuntimeException(context!!.toString() + getString(R.string.no_listener_error))
        }
    }

    // onCreateView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ranking_content, container, false)

        // 引数から値を取得
        position = arguments?.getInt("position")

        // recyclerViewを設定
        recyclerView = view.findViewById<RecyclerView>(R.id.ranking)
        layoutManager = LinearLayoutManager(getActivity() as Context)
        recyclerView.layoutManager = layoutManager
        // 一旦空のAdapterをセット
        adapter = RankingCellAdapter(activity as Context, datas) {}
        recyclerView.adapter = adapter
        // データ取得
        datas.clear()
        insertProgressBarOrEnd(-1)
        startRankingGetLoader(position!!, 0)

        // リフレッシュ時にもデータを再取得
        mSwipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)
        mSwipeRefreshLayout.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            // スクロールを一旦禁止
            recyclerView.addOnItemTouchListener(scrollDisableListener)
            // スクロールリスナーを再登録
            recyclerView.addOnScrollListener(newEndlessScrollListener(layoutManager))
            loadMax = false
            // 引数に値を渡してローダーを起動
            datas.clear()
            startRankingGetLoader(position!!, 0)
        })

        return view
    }

    // onPause
    override fun onPause() {
        super.onPause()
        onPauseFlg = true
    }

    // onDetach
    override fun onDetach() {
        super.onDetach()
        onFragmentInteractionListener = null
    }


    // startRankingGetLoader
    private fun startRankingGetLoader(position: Int, page: Int) {
        // 引数に値を渡してローダーを起動
        val bundle = Bundle()
        bundle.putInt("position", position)
        bundle.putInt("page", page)
        loaderManager.restartLoader(13310 + position, bundle,
            RankingGetLoaderCallbacks(activity as Context, this))
    }

    // RankingGetLoaderOnLoadFinished
    override fun RankingGetLoaderOnLoadFinished (data: List<RankData>?) {
        if (onPauseFlg) {
            onPauseFlg = false
        } else {
            // ProgressBarの削除
            if (datas.isNotEmpty()) {
                val datasIndex = datas.size -1
                if (datas[datasIndex].point == -1) {
                    datas.removeAt(datasIndex)
                    adapter.notifyItemRangeRemoved(datasIndex, 1)
                }
            }

            if (datas.size == 0) {
                if (data != null) {
                    datas.addAll(data)
                }
                // アダプターのセット
                adapter = RankingCellAdapter(context!!, datas) { rankData ->
                    val listener = context as? RankingContentFragment.OnFragmentInteractionListener
                    listener?.onRankClicked(rankData.username)
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
            } else if (!loadMax) {
                loadMax = true
                insertProgressBarOrEnd(-2)
            }
        }
        recyclerView.removeOnItemTouchListener(scrollDisableListener)
        mSwipeRefreshLayout.setRefreshing(false)
    }


    // newEndlessScrollListener
    private fun newEndlessScrollListener(layoutManager: LinearLayoutManager) : EndlessScrollListener {
        val endlessScrollListener = object : EndlessScrollListener(layoutManager) {
            override fun onLoadMore(currentPage: Int) {
                if (!loadMax) {
                    // 追加データの取得
                    startRankingGetLoader(position!!, currentPage )
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                refreshStatusUpdate(layoutManager, mSwipeRefreshLayout)
            }
        }
        return endlessScrollListener
    }

    // insertProgressBarOrEnd
    private fun insertProgressBarOrEnd(point: Int) {
        val positionStart = datas.size + 1
        val progressBarOrEnd = RankData(
            username = "",
            dispName = "",
            accountImage = "",
            profile = "",
            point = point,
            wpOn = "",
            wpName = "",
            wpCategory = ""
        )
        datas.add(progressBarOrEnd)
        adapter.notifyItemRangeInserted(positionStart, 1)
    }
}


fun newRankingContentFragment() : RankingContentFragment {
    return RankingContentFragment()
}
