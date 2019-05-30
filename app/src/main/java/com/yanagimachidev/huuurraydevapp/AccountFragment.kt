package com.yanagimachidev.huuurraydevapp


import android.app.Activity
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.content.res.AppCompatResources.getDrawable
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView


// AccountFragment
class AccountFragment : Fragment(),
    PointDataGetLoaderInterface,
    PostDataGetLoaderInterface {

    // 変数定義
    private val LOG_TAG = AccountFragment::class.java.simpleName // ログ用にクラス名を取得
    private var onMe: Boolean = false // 自分のアカウントかの判定
    private var onAccount: Boolean = false // アカウントを持っているかの判定
    private var username: String = "" // 対象ユーザー
    private var psUsername: String = "@@@@@" // SharedPreferencesから取得したUsername
    private var sp: Int? = null // これまでにもらったSP
    private var sendSp: Int? = null // これまでに送ったSP
    private var sendSpTo: Int? = null // これまで対象ユーザーに送ったSP
    private var favorite: Int? = null // お気に入り登録者数
    private var favoriteTo: Int? = null // お気に入り登録数
    private var stSp: Int? = null // 利用可能SP
    private var checkInFlg = false // チェックインフラグ
    private var lastCheckInValue: Long? = null // 最終チェックイン日時
    private var onPauseFlg = false
    private var onDialog = false
    private var loadMax = false

    // 投稿関連
    private val scrollDisableListener = ScrollDisableListener()
    private lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PostCellAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private var datas = mutableListOf<PostData>()
    
    // 表示項目（共通）
    private lateinit var usernameText: TextView
    private lateinit var dispNameText: TextView
    private lateinit var profileText: TextView
    private lateinit var accountImage: ImageView
    private lateinit var backImage: ImageView
    private lateinit var toSpText: TextView
    private lateinit var fromSpText: TextView
    private lateinit var wp1NameText: TextView
    private lateinit var wp1MapBottun: ImageButton
    private lateinit var wp1SiteBottun: ImageButton
    private lateinit var wp1CategoryText: TextView
    private lateinit var wp1Image: ImageView
    
    // 表示項目（Myアカウント用） 
    private lateinit var accountEditButton : ImageButton
    private lateinit var accountImageEditButton: ImageButton
    private lateinit var backImageEditButton: ImageButton
    private lateinit var wp1EditButton: ImageButton
    private lateinit var wp1ImageEditButton: ImageButton

    // 表示項目（Myアカウント以外）
    private lateinit var followButton: Button
    private lateinit var underGrayBackImage: ImageView
    private lateinit var underWhiteBack1Image: ImageView
    private lateinit var sendSpToText: TextView
    private lateinit var lastCheckIn: TextView
    private lateinit var spText: TextView
    private lateinit var spSpText: TextView
    private lateinit var favoriteText: TextView
    private lateinit var favoriteToText: TextView
    private lateinit var checkinTitle: TextView
    private lateinit var checkinButton: FloatingActionButton
    private lateinit var todayCheckIn: TextView
    private lateinit var supportTitle: TextView
    private lateinit var supportButton: FloatingActionButton

    private lateinit var playAdButton: FloatingActionButton


    // リスナーを親へ渡すためのインターフェイス
    interface OnFragmentInteractionListener {
        // アカウント情報編集ボタン押下時のイベント
        fun accountEditButtonOnClick()
        // 画像編集ボタン押下時のイベント
        fun accountImageEditButtonOnClick(imageType: String)
        // 店舗情報編集ボタン押下時のイベント
        fun wpEditButtonOnClick(ver: String)
        // 店舗マップ１押下時のイベント
        fun wpMapBottunOnClick(bundle: Bundle)
        // 店舗URL１押下時のイベント
        fun wpUrlButtonOnClick(url: String)
        // お気に入り登録ボタン押下時のイベント
        fun followButtonOnClick(toUsername: String, fromUsername: String, flg: Boolean)
        // お気に入り登録者数押下時のイベント
        fun followCntOnClick(username: String, flg: Boolean)
        // 編集押下時のイベント
        fun onEditClicked(id: Int, username: String, content: String, fromType: String)
        // いいね！ボタン押下時のイベント
        fun onNiceButtonClicked(id: Int, username: String, favorite: Boolean)
        // イイね！の数押下時のイベント
        fun onFavoriteCntClicked(id: Int)
        // 動画再生ボタン押下時のイベント
        fun playAdButtonOnClick()
    }
    private var onFragmentInteractionListener: OnFragmentInteractionListener? = null


    // onAttach
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is AccountFragment.OnFragmentInteractionListener) {
            onFragmentInteractionListener = context
        } else {
            throw RuntimeException(context!!.toString() + getString(R.string.no_listener_error))
        }
    }

    // onCreateView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // layoutファイルを指定
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        // intentからusernameを取得
        val intent = activity!!.intent
        val intUsername = intent.getStringExtra("username")
        checkInFlg = intent.getBooleanExtra("checkInFlg", false)

        // SharedPreferencesからusernameの取得
        val pref = getActivity()!!.getSharedPreferences("aws_credentials", Context.MODE_PRIVATE)
        // usernameとパスワードを取得できていて認証済
        if (pref.contains("username") && pref.contains("password") && pref.contains("confirm")) {
            psUsername = pref.getString("username", "")
            onAccount = true
        }

        // usernameからMyアカウントかを判定
        if (intUsername == null || intUsername == psUsername) {
            username = psUsername
            onMe = true
        } else {
            username = intUsername
        }

        // recyclerViewを設定
        recyclerView = view.findViewById<RecyclerView>(R.id.feed)
        layoutManager = LinearLayoutManager(activity as Context)
        val simpleItemAnimator = recyclerView.itemAnimator as SimpleItemAnimator
        simpleItemAnimator.supportsChangeAnimations = false
        recyclerView.layoutManager = layoutManager
        // 一旦空のAdapterをセット
        adapter = PostCellAdapter(activity as Context, datas, onAccount) {}
        recyclerView.adapter = adapter
        // プログレスバーの表示
        datas.clear()
        insertProgressBarOrEnd(-1)

        // スワイプ更新に関する登録
        mSwipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)
        mSwipeRefreshLayout.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            // スクロールを一旦禁止
            recyclerView.addOnItemTouchListener(scrollDisableListener)
            // スクロールリスナーを再登録
            recyclerView.addOnScrollListener(newEndlessScrollListener(layoutManager))
            loadMax = false
            datas.clear()
            update()
        })

        // 各項目をインスタンス化（標準項目）
        dispNameText = view.findViewById<TextView>(R.id.disp_name) // 表示名
        profileText = view.findViewById<TextView>(R.id.profile) // プロフィール
        accountImage = view.findViewById<ImageView>(R.id.avatar_image) // アカウント画像
        backImage = view.findViewById<ImageView>(R.id.back_image) // 背景画像
        toSpText = view.findViewById<TextView>(R.id.rept) // もらったSP
        fromSpText = view.findViewById<TextView>(R.id.sept) // 送ったSP
        favoriteText = view.findViewById<TextView>(R.id.follower) // お気に入り登録者数
        favoriteToText = view.findViewById<TextView>(R.id.follow) // お気に入り登録数
        wp1NameText = view.findViewById<TextView>(R.id.wp1_name) // 店舗名１
        wp1MapBottun = view.findViewById<ImageButton>(R.id.wp1_map) // 店舗１マップボタン
        wp1SiteBottun = view.findViewById<ImageButton>(R.id.wp1_site) // 店舗１サイトボタン
        wp1CategoryText = view.findViewById<TextView>(R.id.wp1_category) // 店舗１種別
        wp1Image = view.findViewById<ImageView>(R.id.wp1_image) // 店舗画像１

        // 各項目をインスタンス化（Myアカウント）
        accountEditButton = view.findViewById<ImageButton>(R.id.profile_edit) // アカウント情報編集ボタン
        accountImageEditButton = view.findViewById<ImageButton>(R.id.avatar_image_edit) // アカウント画像編集ボタン
        backImageEditButton = view.findViewById<ImageButton>(R.id.back_image_edit) // 背景画像編集ボタン
        wp1EditButton = view.findViewById<ImageButton>(R.id.wp1_edit) // 店舗１編集ボタン
        wp1ImageEditButton = view.findViewById<ImageButton>(R.id.wp1_image_edit) // 店舗画像１編集ボタン

        // 各項目をインスタンス化（Myアカウント以外）
        followButton =  view.findViewById<Button>(R.id.follow_button) // お気に入り登録ボタン
        underGrayBackImage = view.findViewById<ImageView>(R.id.under_gray_back) // 下部グレイ背景
        underWhiteBack1Image = view.findViewById<ImageView>(R.id.under_white_back1) // 下部白背景１
        lastCheckIn = view.findViewById<TextView>(R.id.last_check_in) // 最終チェックイン日時
        sendSpToText = view.findViewById<TextView>(R.id.sent_this_user) // これまでに送ったSP
        spText = view.findViewById<TextView>(R.id.sp) // 利用可能SP
        spSpText = view.findViewById<TextView>(R.id.sp_sp) // 利用可能コイン
        checkinTitle = view.findViewById<TextView>(R.id.check_in_title) // チェックイン！
        checkinButton = view.findViewById<FloatingActionButton>(R.id.check_in) // チェックインボタン
        todayCheckIn = view.findViewById<TextView>(R.id.today_check_in) // 本日のチェックイン
        supportTitle = view.findViewById<TextView>(R.id.support_title) // サポート！
        supportButton = view.findViewById<FloatingActionButton>(R.id.support) // サポートボタン

        playAdButton = view.findViewById<FloatingActionButton>(R.id.play_ad)

        // Myアカウント以外用の項目を非表示
        if (onMe || !onAccount) {
            followButton.setVisibility(View.GONE)
            underGrayBackImage.setVisibility(View.GONE)
            underWhiteBack1Image.setVisibility(View.GONE)
            sendSpToText.setVisibility(View.GONE)
            lastCheckIn.setVisibility(View.GONE)
            spText.setVisibility(View.GONE)
            spSpText.setVisibility(View.GONE)
            checkinTitle.setVisibility(View.GONE)
            supportTitle.setVisibility(View.GONE)
        }

        // ユーザー名のセット
        usernameText = view.findViewById<TextView>(R.id.username)
        val conUsernameText = "ID:" + username
        usernameText.text = conUsernameText

        // 各項目の値を更新
        update()

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


    // startPointGetLoader
    private fun startPointGetLoader (username: String, psUsername: String, dbSendSpTo: Int?) {
        // 引数に値を渡してローダーを起動
        val bundle = Bundle()
        bundle.putString("toUsername", username)
        bundle.putString("fromUsername", psUsername)
        var sendSpTo = 0
        if (dbSendSpTo != null) {
            sendSpTo = dbSendSpTo
        }
        bundle.putInt("sendSpTo", sendSpTo)
        loaderManager.restartLoader(13309, bundle, PointDataGetLoaderCallbacks(activity as Context, this))
    }

    // PointDataGetLoaderOnLoadFinished
    override fun PointDataGetLoaderOnLoadFinished(data: PointData?) {
        if (data != null) {
            sp = data.sp
            sendSp = data.sendSp
            sendSpTo = data.sendSpTo
            favorite = data.favorite
            favoriteTo = data.favoriteTo
            pointUpdate()
        } else {
            // ポイント不足エーダイアログを作成
            showAlertDialog(activity!!,
                getString(R.string.error), getString(R.string.failed_to_point_get))
        }
    }


    // startGetFeedLoader
    private fun startGetFeedLoader (username: String?, page: Int, feedFlg: Boolean, favoriteFlg: Boolean) {
        // データの読み込み
        val bundle = Bundle()
        bundle.putInt("page", page)
        bundle.putBoolean("feedFlg", feedFlg)
        bundle.putBoolean("favoriteFlg", favoriteFlg)
        if (username != null) {
            bundle.putString("username", username)
        }
        loaderManager.restartLoader(13307, bundle,
            PostDataGetLoaderCallbacks(activity as Context, this))
    }

    // PostDataGetLoaderOnLoadFinished
    override fun PostDataGetLoaderOnLoadFinished(data: List<PostData>?) {
        if (onPauseFlg || onDialog) {
            onPauseFlg = false
            onDialog = false
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
                adapter = PostCellAdapter(activity!! as Context, datas, onAccount) { postData ->
                    if (postData.clickType == "niceButton") {
                        val position = postData.position!!
                        // cellを更新
                        postData.favorite = !postData.favorite
                        datas.set(position, postData)
                        adapter.notifyItemChanged(position)
                        val favorite = postData.favorite
                        if (favorite) {
                            postData.favoriteCnt++
                        } else {
                            postData.favoriteCnt--
                        }
                        val listener = context as? AccountFragment.OnFragmentInteractionListener
                        listener?.onNiceButtonClicked(postData.id, username, favorite)
                    } else if (postData.clickType == "favoriteCnt") {
                        val listener = context as? AccountFragment.OnFragmentInteractionListener
                        listener?.onFavoriteCntClicked(postData.id)
                    } else if (postData.clickType == "edit") {
                        val listener = context as? AccountFragment.OnFragmentInteractionListener
                        listener?.onEditClicked(postData.id, postData.username, postData.content, "Account")
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
                if (onMe) {
                    insertProgressBarOrEnd(-3)
                } else {
                    insertProgressBarOrEnd(-2)
                }
            }
        }
        recyclerView.removeOnItemTouchListener(scrollDisableListener)
        mSwipeRefreshLayout.setRefreshing(false)
    }


    // 項目の更新
    fun update() {
        // ユーザーデータの取得
        val data = queryHrUser(activity as Context, username)
        var myData: Map<String, Any?> = mapOf()
        if (onAccount) {
            myData = queryHrUser(activity as Context, psUsername)
        }

        // ポイントデータの取得
        var sendSpTo = 0
        if (data.isNotEmpty()) {
            sendSpTo = data["send_sp_to"] as Int
        }
        // 引数に値を渡してローダーを起動
        startPointGetLoader(username, psUsername, sendSpTo)

        // Feedデータの取得
        if (onPauseFlg || onDialog) {
            onPauseFlg = false
            onDialog = false
        } else {
            datas.clear()
            startGetFeedLoader(username, 0, false, false)
        }

        // チェックイン日時のセット
        if (data.isEmpty() || data["last_check_in"] == "") {
            lastCheckInValue = null
        } else {
            val lastCheckInString = data["last_check_in"] as String
            lastCheckInValue = lastCheckInString.toLong()
        }

        // 表示名の更新
        if (data.isEmpty() || data["disp_name"] == "") {
            dispNameText.text = getString(R.string.no_disp_name)
        } else {
            dispNameText.text = data["disp_name"]  as String
        }

        // プロフィール文の更新
        if (data.isEmpty() || data["profile"] == "") {
            profileText.text = getString(R.string.no_profile)
        } else {
            profileText.text = data["profile"] as String
        }

        // アカウント画像の更新
        if (onMe) {
            val accBitmap = getAccountImage(activity as Activity, "account")
            if (accBitmap != null) {
                accountImage.setImageBitmap(accBitmap)
                accountImage.setOnClickListener{
                    onDialog = true
                    displayImageDialog("account", data["account_image"] as String)
                }
            }
        } else {
            if (data.isNotEmpty() && data["account_image"] != "") {
                getS3ImageViaGlide(activity as Context, "account", data["account_image"] as String, accountImage)
                accountImage.setOnClickListener{
                    onDialog = true
                    displayImageDialog("account", data["account_image"] as String)
                }
            }
        }

        // 背景画像の更新
        if (onMe) {
            val backBitmap = getAccountImage(activity as Activity, "back")
            if (backBitmap != null) {
                backImage.setImageBitmap(backBitmap)
            }
        } else {
            if (data.isNotEmpty() && data["back_image"] != "") {
                getS3ImageViaGlide(getActivity() as Context, "back", data["back_image"] as String, backImage)
            }
        }

        // 店舗名１の更新
        if (data.isEmpty() || data["wp1_name"] == "") {
            wp1NameText.text = getString(R.string.no_shop_data)
        } else {
            wp1NameText.text = data["wp1_name"] as String
        }

        // 店舗マップ１の更新
        if (data.isEmpty() || data["wp1_lat"] == null || data["wp1_lng"] == null
            || data["wp1_lat"] == 0.0 || data["wp1_lng"] == 0.0
        ) {
            wp1MapBottun.setVisibility(View.GONE)
        } else {
            wp1MapBottun.setVisibility(View.VISIBLE)
            wp1MapBottun.setOnClickListener{
                it.notPressTwice()
                val wp1MapBundle = Bundle()
                wp1MapBundle.putBoolean("forMapFlg", true)
                wp1MapBundle.putDouble("AccLat", data["wp1_lat"] as Double)
                wp1MapBundle.putDouble("AccLng", data["wp1_lng"] as Double)
                val listener = context as? AccountFragment.OnFragmentInteractionListener
                listener?.wpMapBottunOnClick(wp1MapBundle)
            }
        }

        // 店舗URL１の更新
        if (data.isEmpty() || data["wp1_url"] == "") {
            wp1SiteBottun.setVisibility(View.INVISIBLE)
        } else {
            wp1SiteBottun.setVisibility(View.VISIBLE)
            wp1SiteBottun.setOnClickListener{
                it.notPressTwice()
                val listener = context as? AccountFragment.OnFragmentInteractionListener
                listener?.wpUrlButtonOnClick(data["wp1_url"] as String)
            }
        }

        // 店舗名１の更新
        if (data.isEmpty() || data["wp1_category"] == "") {
            wp1CategoryText.text = ""
        } else {
            wp1CategoryText.text = data["wp1_category"] as String
        }

        // 店舗画像の更新
        if (onMe) {
            val wp1Bitmap = getAccountImage(activity as Activity,"wp1")
            if (wp1Bitmap != null) {
                wp1Image.setImageBitmap(wp1Bitmap)
                wp1Image.setOnClickListener{
                    onDialog = true
                    displayImageDialog("wp1", data["wp1_image"] as String)
                }
            } else {
                wp1Image.setImageDrawable(getDrawable(activity as Context, R.drawable.ic_store_white_24dp))
            }
        } else {
            if (data.isNotEmpty() && data["wp1_image"] != "") {
                getS3ImageViaGlide(activity as Context, "wp1", data["wp1_image"] as String, wp1Image)
                wp1Image.setOnClickListener{
                    onDialog = true
                    displayImageDialog("wp1", data["wp1_image"] as String)
                }
            }
        }

        // Myアカウント以外用の項目の値を更新
        if (!onMe && onAccount) {
            // 利用可能SPの更新
            if (myData.isEmpty() || myData["st_sp"] == null) {
                spText.text = getString(R.string.can_use_sp_0)
                stSp = 0
            } else {
                val conSpText =  getString(R.string.can_use_sp) + String.format("%,d", myData["st_sp"])
                spText.text = conSpText
                stSp = myData["st_sp"] as Int
            }
        }


        // アカウント情報編集ボタンへのイベント登録
        if (onMe) {
            accountEditButton.setOnClickListener {
                it.notPressTwice()
                val listener = context as? AccountFragment.OnFragmentInteractionListener
                listener?.accountEditButtonOnClick()
            }
        } else {
            accountEditButton.setVisibility(View.GONE)
        }

        // アカウント画像編集ボタンへのイベント登録
        if (onMe) {
            accountImageEditButton.setOnClickListener{
                it.notPressTwice()
                val listener = context as? AccountFragment.OnFragmentInteractionListener
                listener?.accountImageEditButtonOnClick("account")
            }
        } else {
            accountImageEditButton.setVisibility(View.GONE)
        }

        // 背景画像編集ボタンへのイベント登録
        if (onMe) {
            backImageEditButton.setOnClickListener{
                it.notPressTwice()
                val listener = context as? AccountFragment.OnFragmentInteractionListener
                listener?.accountImageEditButtonOnClick("back")
            }
        } else {
            backImageEditButton.setVisibility(View.GONE)
        }

        // 店舗1編集ボタンへのイベント登録
        if (onMe) {
            wp1EditButton.setOnClickListener{
                it.notPressTwice()
                val listener = context as? AccountFragment.OnFragmentInteractionListener
                listener?.wpEditButtonOnClick("1")
            }
        } else {
            wp1EditButton.setVisibility(View.GONE)
        }

        // 店舗画像１編集ボタンへのイベント登録
        if (onMe) {
            wp1ImageEditButton.setOnClickListener{
                it.notPressTwice()
                val listener = context as? AccountFragment.OnFragmentInteractionListener
                listener?.accountImageEditButtonOnClick("wp1")
            }
        } else {
            wp1ImageEditButton.setVisibility(View.GONE)
        }

        // お気に入り登録/解除ボタン
        if (onMe || !onAccount) {
            followButton.setVisibility(View.GONE)
        } else {
            var favorite = false
            var favoriteStatus = getString(R.string.follow_off)
            if (data["favorite_user"] == "0") {
                favorite = true
                favoriteStatus = getString(R.string.follow_on)
            }
            followButton.text = favoriteStatus
            followButton.setOnClickListener {
                it.notPressTwice()
                val listener = context as? AccountFragment.OnFragmentInteractionListener
                listener?.followButtonOnClick(username, psUsername, favorite)
            }
        }

        // お気に入り登録者数の表示
        favoriteText.setOnClickListener {
            it.notPressTwice()
            val listener = context as? AccountFragment.OnFragmentInteractionListener
            listener?.followCntOnClick(username, true)
        }

        // お気に入り登録数の表示
        favoriteToText.setOnClickListener {
            it.notPressTwice()
            val listener = context as? AccountFragment.OnFragmentInteractionListener
            listener?.followCntOnClick(username, false)
        }

        // チェックインボタンへのイベント登録
        if (onMe || !checkInFlg || !onAccount) {
            checkinTitle.setVisibility(View.GONE)
            checkinButton.setVisibility(View.GONE)
            todayCheckIn.setVisibility(View.GONE)
        } else {
            val df = SimpleDateFormat("yyyy-MM-dd")
            var lastCheckInFormat = ""
            if (lastCheckInValue != null) {
                //df.setTimeZone(TimeZone.getTimeZone("gmt"))
                lastCheckInFormat = df.format(lastCheckInValue)
            }
            val nowDate = df.format(System.currentTimeMillis())
            if (lastCheckInFormat != nowDate) {
                todayCheckIn.setVisibility(View.GONE)
                checkinButton.setOnClickListener {
                    onDialog = true
                    if (activity!!.supportFragmentManager.findFragmentByTag("myAlertDialog") == null) {
                        val flagmentManager = activity!!.supportFragmentManager
                        val dialogFragment = AfterGifAlertDialogFragment()
                        val toDialogBundle = Bundle()
                        toDialogBundle.putString("title", getString(R.string.check_in_confirm))
                        toDialogBundle.putString("message", getString(R.string.check_in_alert))
                        toDialogBundle.putString("yes", getString(R.string.check_in_execute))
                        toDialogBundle.putString("no", getString(R.string.cancel))
                        toDialogBundle.putString("toUsername", username)
                        toDialogBundle.putString("fromUsername", psUsername)
                        toDialogBundle.putInt("point", CHECK_IN_SP)
                        toDialogBundle.putInt("sendSpTo", sendSpTo)
                        toDialogBundle.putString("type", "ci")
                        dialogFragment.setArguments(toDialogBundle)
                        dialogFragment.show(flagmentManager, "myAlertDialog")
                    }
                }
            } else {
                todayCheckIn.setVisibility(View.VISIBLE)
                checkinButton.isEnabled = false
            }
        }

        // SPボタンのイベント登録
        if (onMe || !onAccount) {
            supportButton.setVisibility(View.GONE)
        } else {
            if (stSp!! >= USE_ST_SP) {
                supportButton.setOnClickListener {
                    onDialog = true
                    if (activity!!.supportFragmentManager.findFragmentByTag("myAlertDialog") == null) {
                        val flagmentManager = activity!!.supportFragmentManager
                        val dialogFragment = AfterGifAlertDialogFragment()
                        val toDialogBundle = Bundle()
                        toDialogBundle.putString("title", getString(R.string.support_confirm))
                        toDialogBundle.putString("message", getString(R.string.spwo) + USE_ST_SP + getString(R.string.consumption))
                        toDialogBundle.putString("yes", getString(R.string.execute_support))
                        toDialogBundle.putString("no", getString(R.string.cancel))
                        toDialogBundle.putString("toUsername", username)
                        toDialogBundle.putString("fromUsername", psUsername)
                        toDialogBundle.putInt("point", USE_ST_SP)
                        toDialogBundle.putInt("sendSpTo", sendSpTo)
                        toDialogBundle.putString("type", "st")
                        dialogFragment.setArguments(toDialogBundle)
                        dialogFragment.show(flagmentManager, "AfterGifAlertDialog")
                    }
                }
            } else {
                supportButton.setOnClickListener {
                    showAlertDialog(activity!!, getString(R.string.error), getString(R.string.sp_lack))
                }
            }
        }

        playAdButton.setOnClickListener {
            it.notPressTwice()
            val listener = context as? AccountFragment.OnFragmentInteractionListener
            listener?.playAdButtonOnClick()
        }
    }

    // ポイント/お気に入り項目の更新
    fun pointUpdate() {
        // ポイント情報をセット
        toSpText.text = String.format("%,d", sp)
        fromSpText.text = String.format("%,d", sendSp)
        favoriteText.text = String.format("%,d", favorite)
        favoriteToText.text = String.format("%,d", favoriteTo)
        if (!onMe && onAccount) {
            // 対象ユーザーに送ったSPをセット
            val conSendSpText = getString(R.string.send_to_sp_text) + String.format("%,d", sendSpTo)
            sendSpToText.text = conSendSpText

            // 最終チェックイン日時をセット
            if (lastCheckInValue == null) {
                lastCheckIn.text = getString(R.string.last_check_in)
            } else {
                val df = SimpleDateFormat("yyyy/MM/dd")
                //df.setTimeZone(TimeZone.getTimeZone("gmt"))
                val lastCheckInFormat = df.format(lastCheckInValue)
                val conLastCheckIn =getString(R.string.last_check_in) + lastCheckInFormat
                lastCheckIn.text = conLastCheckIn
            }
        }
    }

    // データ再取得用
    fun dataReload() {
        // データの取得
        loadMax = false
        onPauseFlg = false
        datas.clear()
        startGetFeedLoader(username, 0, false, false)
    }

    // dialogCancel
    fun dialogCancel () {
        onDialog = false
    }

    // displayImageDialog
    private fun displayImageDialog (imageType: String, imageName: String) {
        if (activity!!.supportFragmentManager.findFragmentByTag("myImageDialog") == null) {
            val flagmentManager = activity!!.supportFragmentManager
            val dialogFragment = ImageDisplayDialogFragment()
            val toDialogBundle = Bundle()
            toDialogBundle.putString("imageType", imageType)
            toDialogBundle.putString("imageName", imageName)
            dialogFragment.setArguments(toDialogBundle)
            dialogFragment.show(flagmentManager, "myImageDialog")
        }
    }

    // newEndlessScrollListener
    private fun newEndlessScrollListener(layoutManager: LinearLayoutManager) : EndlessScrollListener {
        val endlessScrollListener = object : EndlessScrollListener(layoutManager) {
            override fun onLoadMore(currentPage: Int) {
                if (!loadMax) {
                    startGetFeedLoader(username, currentPage, false, false)
                }
            }
        }
        return endlessScrollListener
    }

    // insertProgressBarOrEnd
    private fun insertProgressBarOrEnd(id: Int) {
        val positionStart = datas.size + 1
        val progressBarOrEnd = PostData(
            id = id,
            content = "",
            createdAt = "",
            modifiedAt ="",
            username = "",
            dispName = "",
            accountImage = "",
            favorite = false,
            favoriteCnt = 0,
            wpOn = "",
            wpName = "",
            clickType = null,
            position = null
        )
        datas.add(progressBarOrEnd)
        adapter.notifyItemRangeInserted(positionStart, 1)
    }
}


fun newAccountFragment() : AccountFragment {
    return AccountFragment()
}
