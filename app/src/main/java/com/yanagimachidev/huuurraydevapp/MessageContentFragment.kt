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


// MessageContentFragment
class MessageContentFragment : Fragment(),
    MessageGetLoaderInterface,
    MessageGetFromSQLiteLoaderInterface {

    // 変数定義
    private val LOG_TAG = MessageContentFragment::class.java.simpleName // ログ用にクラス名を取得
    private val scrollDisableListener = ScrollDisableListener()
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: MessageCellAdapter
    private lateinit var username: String
    private var datas = mutableListOf<MessageDispData>()
    private var position: Int? = null
    private var onPauseFlg = false
    private var loadMax = false


    // リスナーを親へ渡すためのインターフェイス
    interface OnFragmentInteractionListener {
        fun onClicked(message: MessageDispData)
    }
    private var onFragmentInteractionListener: OnFragmentInteractionListener? = null


    // onAttach
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is MessageContentFragment.OnFragmentInteractionListener) {
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

        // SharedPreferencesからusernameの取得
        val pref = activity!!.getSharedPreferences("aws_credentials", Context.MODE_PRIVATE)
        username = pref.getString("username", "")

        // SharedPreferencesから最終メッセージ取得日時を取得
        val acc_pref = activity!!.getSharedPreferences("account_info", Context.MODE_PRIVATE)
        var messageUpdatedAt = ""
        if (acc_pref.contains("message_updated_at")) {
            messageUpdatedAt = acc_pref.getString("message_updated_at", "")
        }

        // 引数から値を取得
        position = arguments?.getInt("position")

        // recyclerViewを設定
        recyclerView = view.findViewById<RecyclerView>(R.id.ranking)
        layoutManager = LinearLayoutManager(getActivity() as Context)
        recyclerView.layoutManager = layoutManager
        // 一旦空のAdapterをセット
        adapter = MessageCellAdapter(activity as Context, datas, position!!) {}
        Log.d(LOG_TAG, "#################" +  position)
        recyclerView.adapter = adapter
        // データ取得
        datas.clear()
        insertProgressBarOrEnd(-1)
        startMessageGetFromSQLiteLoader(position!!, 0, username)

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
            startMessageGetLoader(messageUpdatedAt, username)
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


    // startMessageGetFromSQLiteLoader
    private fun startMessageGetFromSQLiteLoader(position: Int, page: Int, username: String) {
        // 引数に値を渡してローダーを起動
        val bundle = Bundle()
        bundle.putInt("tfFlg", position)
        bundle.putInt("page", page)
        bundle.putString("username", username)
        loaderManager.restartLoader(13317 + position, bundle,
            MessageGetFromSQLiteLoaderCallbacks(activity as Context, this))
    }

    // MessageGetFromSQLiteLoaderOnLoadFinished
    override fun MessageGetFromSQLiteLoaderOnLoadFinished(data: List<MessageDispData>?) {
        if (onPauseFlg) {
            onPauseFlg = false
        } else {
            // ProgressBarの削除
            if (datas.isNotEmpty()) {
                val datasIndex = datas.size -1
                if (datas[datasIndex].id == -1) {
                    datas.removeAt(datasIndex)
                    adapter.notifyItemRangeRemoved(datasIndex, 1)
                }
            }

            if (datas.size == 0) {
                if (data != null) {
                    datas.addAll(data)
                }
                // アダプターのセット
                adapter = MessageCellAdapter(context!!, datas, position!!) { messageData->
                    val listener = context as? MessageContentFragment.OnFragmentInteractionListener
                    listener?.onClicked(messageData)
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


    // startMessageGetLoader
    private fun startMessageGetLoader(updated_at: String, username: String) {
        // 引数に値を渡してローダーを起動
        val bundle = Bundle()
        bundle.putString("updated_at", updated_at)
        bundle.putString("username", username)
        loaderManager.restartLoader(13319, bundle,
            MessageGetLoaderCallbacks(activity as Context, this))
    }

    // MessageGetLoaderOnLoadFinished
    override fun MessageGetLoaderOnLoadFinished(data: List<MessageData>?) {
        startMessageGetFromSQLiteLoader(position!!, 0, username)
    }


    // newEndlessScrollListener
    private fun newEndlessScrollListener(layoutManager: LinearLayoutManager) : EndlessScrollListener {
        val endlessScrollListener = object : EndlessScrollListener(layoutManager) {
            override fun onLoadMore(currentPage: Int) {
                if (!loadMax) {
                    // 追加データの取得
                    startMessageGetFromSQLiteLoader(position!!, currentPage, username)
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                refreshStatusUpdate(layoutManager, mSwipeRefreshLayout)
            }
        }
        return endlessScrollListener
    }

    // insertProgressBarOrEnd
    private fun insertProgressBarOrEnd(id: Int) {
        val positionStart = datas.size + 1
        val progressBarOrEnd = MessageDispData(
            id = id,
            toUsername = null,
            fromUsername = null,
            content = null,
            deleteFlg = null,
            createdAt = null,
            modifiedAt = null,
            dispName = null,
            accountImage = null,
            wpOn = null,
            wpName = null,
            wpUrl = null,
            wpLat = null,
            wpLng = null,
            clickType = null,
            position = null
        )
        datas.add(progressBarOrEnd)
        adapter.notifyItemRangeInserted(positionStart, 1)
    }
}


fun newMessageContentFragment() : MessageContentFragment {
    return MessageContentFragment()
}
