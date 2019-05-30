package com.yanagimachidev.huuurraydevapp


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.SimpleItemAnimator
import android.widget.CompoundButton
import android.widget.Switch


// ThanksFeedFragment
class ThanksFeedFragment : Fragment(), MessageFeedGetLoaderInterface {

    // 変数定義
    private val LOG_TAG = ThanksFeedFragment::class.java.simpleName // ログ用にクラス名を取得
    private var username: String? = null
    private val scrollDisableListener = ScrollDisableListener()
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager:  LinearLayoutManager
    private lateinit var adapter: MessageFeedCellAdapter
    private var datas = mutableListOf<MessageFeedData>()
    private var onAccount = false
    private var favoriteStatus = false
    private var onPauseFlg = false
    private var loadMax = false


    // リスナーを親へ渡すためのインターフェイス
    interface OnFragmentInteractionListener {
        // アカウント画像押下時のイベント
        fun onAccountImageClicked(username: String)
        // イイね！ボタン押下時のイベント
        fun onMessageNiceButtonClicked(id: Int, username: String, favorite: Boolean)
        // イイね！の数押下時のイベント
        fun onMessageNiceCntClicked(id: Int)
    }
    private var onFragmentInteractionListener: OnFragmentInteractionListener? = null


    // onAttach
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is ThanksFeedFragment.OnFragmentInteractionListener) {
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
        // layoutファイルを指定
        val view = inflater.inflate(R.layout.fragment_thanks_feed, container, false)

        // 現在の状況をSharedPreferencesからusernameの取得から取得
        val pref = activity!!.getSharedPreferences("aws_credentials", Context.MODE_PRIVATE)
        val acc_pref = activity!!.getSharedPreferences("account_info", Context.MODE_PRIVATE)
        // usernameとパスワードを取得できていて認証済
        if (pref.contains("username") && pref.contains("password") && pref.contains("confirm")) {
            onAccount = true
            username = pref.getString("username", "")
            if (acc_pref.contains("thanks_favorite_switch")){
                favoriteStatus = acc_pref.getBoolean("thanks_favorite_switch", false)
            }
        }

        // recyclerViewを設定
        recyclerView = view.findViewById<RecyclerView>(R.id.feed)
        layoutManager = LinearLayoutManager(activity as Context)
        val simpleItemAnimator = recyclerView.itemAnimator as SimpleItemAnimator
        simpleItemAnimator.supportsChangeAnimations = false
        recyclerView.layoutManager = layoutManager
        // 一旦空のAdapterをセット
        adapter = MessageFeedCellAdapter(activity as Context, datas, onAccount) {}
        recyclerView.adapter = adapter
        // データの取得
        datas.clear()
        insertProgressBarOrEnd(-1)
        startMessageFeedGetLoader(username, 0, favoriteStatus)

        // スワイプリフレッシュに関する処理を登録
        mSwipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)
        mSwipeRefreshLayout.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            // スクロールを一旦禁止
            recyclerView.addOnItemTouchListener(scrollDisableListener)
            // スクロールリスナーを再登録
            recyclerView.addOnScrollListener(newEndlessScrollListener(layoutManager))
            loadMax = false
            // データの再取得
            datas.clear()
            startMessageFeedGetLoader(username, 0, favoriteStatus)
        })

        // お気に入りフィルタースイッチのイベント登録
        val favoriteSwitch = view.findViewById<Switch>(R.id.favorite_switch)
        if (!onAccount) {
            favoriteSwitch.setVisibility(View.GONE)
        } else {
            favoriteSwitch.setVisibility(View.VISIBLE)
            favoriteSwitch.isChecked = favoriteStatus
            if (favoriteStatus) {
                favoriteSwitch.text = getString(R.string.onry_favorite_feed)
            } else {
                favoriteSwitch.text = getString(R.string.all_account_feed)
            }
            favoriteSwitch.setOnCheckedChangeListener (object: CompoundButton.OnCheckedChangeListener {
                override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                    if (isChecked) {
                        favoriteSwitch.text = getString(R.string.onry_favorite_feed)
                    } else {
                        favoriteSwitch.text = getString(R.string.all_account_feed)
                    }
                    val editor = acc_pref.edit()
                    editor.putBoolean("thanks_favorite_switch", isChecked)
                    editor.apply()
                    favoriteStatus = isChecked
                    datas.clear()
                    startMessageFeedGetLoader(username, 0, favoriteStatus)
                }
            })
        }
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


    // startGetFeedLoader
    private fun startMessageFeedGetLoader (username: String?, page: Int, favoriteFlg: Boolean) {
        // データの読み込み
        val bundle = Bundle()
        bundle.putInt("page", page)
        bundle.putBoolean("favoriteFlg", favoriteFlg)
        if (username != null) {
            bundle.putString("username", username)
        }
        loaderManager.restartLoader(13322, bundle,
            MessageFeedGetLoaderCallbacks(activity as Context, this))
    }

    // PostDataGetLoaderOnLoadFinished
    override fun MessageFeedGetLoaderOnLoadFinished(data: List<MessageFeedData>?) {
        if (onPauseFlg){
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
                adapter = MessageFeedCellAdapter(activity as Context, datas, onAccount) { massageData ->
                    if (massageData.clickType == "niceButton") {
                        val position = massageData.position!!
                        // cellを更新
                        if (massageData.favorite) {
                            massageData.favorite = false
                        } else {
                            massageData.favorite = true
                        }
                        datas.set(position, massageData)
                        adapter.notifyItemChanged(position)
                        val favorite = massageData.favorite
                        if (favorite) {
                            massageData.favoriteCnt++
                        } else {
                            massageData.favoriteCnt--
                        }
                        val listener = context as? ThanksFeedFragment.OnFragmentInteractionListener
                        listener?.onMessageNiceButtonClicked(massageData.id, username!!, favorite)
                    } else if (massageData.clickType == "favoriteCnt") {
                        val listener = context as? ThanksFeedFragment.OnFragmentInteractionListener
                        listener?.onMessageNiceCntClicked(massageData.id)
                    } else if (massageData.clickType == "fromAccountImage") {
                        val listener = context as? ThanksFeedFragment.OnFragmentInteractionListener
                        listener?.onAccountImageClicked(massageData.fromUsername)
                    } else {
                        val listener = context as? ThanksFeedFragment.OnFragmentInteractionListener
                        listener?.onAccountImageClicked(massageData.toUsername)
                    }
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
                    // データの取得を開始
                    startMessageFeedGetLoader(username, currentPage, favoriteStatus)
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
        val progressBarOrEnd = MessageFeedData(
            id = id,
            content = "",
            createdAt = "",
            modifiedAt ="",
            toUsername = "",
            toDispName = "",
            toAccountImage = "",
            toWpOn = "",
            toWpName = "",
            fromUsername = "",
            fromDispName = "",
            fromAccountImage = "",
            fromWpOn = "",
            fromWpName = "",
            favorite = false,
            favoriteCnt = 0,
            clickType = "",
            position = null
        )
        datas.add(progressBarOrEnd)
        adapter.notifyItemRangeInserted(positionStart, 1)
    }

    // データ再取得用
    fun dataReload() {
        // データの取得
        loadMax = false
        onPauseFlg = false
        datas.clear()
        startMessageFeedGetLoader(username, 0,  favoriteStatus)
    }
}


fun newThanksFeedFragment() : ThanksFeedFragment {
    return ThanksFeedFragment()
}
