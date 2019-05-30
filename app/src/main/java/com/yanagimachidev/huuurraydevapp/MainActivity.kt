package com.yanagimachidev.huuurraydevapp


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.support.design.widget.BottomNavigationView
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import kotlinx.android.synthetic.main.content_main.*
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*


// MainActivity
class MainActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener,
    RankingFragment.OnFragmentInteractionListener,
    RankingContentFragment.OnFragmentInteractionListener,
    MapFragment.OnFragmentInteractionListener,
    MapMarkerInfoDialogFragment.OnFragmentInteractionListener,
    PrFeedFragment.OnFragmentInteractionListener,
    FeedFragment.OnFragmentInteractionListener,
    ThanksFeedFragment.OnFragmentInteractionListener,
    AccountFragment.OnFragmentInteractionListener,
    NoAccountUserFragment.OnFragmentInteractionListener,
    MessageFragment.OnFragmentInteractionListener,
    MessageContentFragment.OnFragmentInteractionListener,
    ImageDisplayDialogFragment.OnFragmentInteractionListener,
    NiceCDLoaderInterface,
    MessageNiceCDLoaderInterface,
    MessageGetLoaderInterface {

    // 変数定義
    private val LOG_TAG = MainActivity::class.java.simpleName // ログ用にクラス名を取得
    private var status = 2 // ユーザーの認証状況＝0:SignIn, 1:NotConfirm, 2:NotSignUp
    private var forMapFlg = false // アカウント画面からマップフラグメントを起動した時用のフラグ
    private var username = "" // アカウントを保持していた場合のユーザー名
    private var messageUpdateAt = "" // メッセージ情報を最後にアップデートした日時
    private lateinit var fragmentTitle: TextView
    private lateinit var noOpenCountText: TextView

    // BottomNavigationのメニュー選択時のイベント
    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        Log.d(LOG_TAG, "listener setting status：" + status)
        when (item.itemId) {
            R.id.navigation_ranking -> {
                startFragment("rankingFragment", false, null)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_map -> {
                startFragment("mapFragment", false, null)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_search -> {
                startFragment("searchFragment", false, null)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_feed -> {
                startFragment("feedFragment", false, null)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_account -> {
                startFragment("accountFragment", true, null)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }


    // onCreate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // StatusBarを表示、透過する設定
        findViewById<View>(android.R.id.content).systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        // 未読メッセージ数を非表示にする
        noOpenCountText = findViewById<TextView>(R.id.no_open_count)
        noOpenCountText.setVisibility(View.GONE)

        // 現在のステータスをインテントから取得
        val intent = intent
        status = intent.getIntExtra("status", 2)

        // タイトルを取得
        fragmentTitle = findViewById<TextView>(R.id.app_bar_text)

        if (status == 1) {
            startVerificationActivity(this)
        }

        // forMapFlgを取得して、trueだった場合は情報を渡してMapフラグメントを表示
        forMapFlg = intent.getBooleanExtra("forMapFlg", false)
        if (forMapFlg) {
            val bundle = Bundle()
            bundle.putBoolean("forMapFlg", intent.getBooleanExtra("forMapFlg", true))
            bundle.putDouble("AccLat", intent.getDoubleExtra("AccLat", 35.6811323))
            bundle.putDouble("AccLng", intent.getDoubleExtra("AccLng", 139.7670182))
            fromAccStartMapFragment(bundle)
        } else {
            // デフォルト表示としてランキングフラグメントを表示
            startFragment("rankingFragment", false, null)
        }

        // ボトムナビゲーションボタンについての設定
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        // トグルナビゲーションボタンについての設定
        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)

        // ログインできている場合は
        if (status == 0) {
            // usernameを取得
            val pref = getSharedPreferences("aws_credentials", Context.MODE_PRIVATE)
            username = pref.getString("username", "")

            // メッセージデータを取得
            // message_updated_atを取得
            val acc_pref = getSharedPreferences("account_info", Context.MODE_PRIVATE)
            if (acc_pref.contains("message_updated_at")) {
                messageUpdateAt = acc_pref.getString("message_updated_at", "")
            }
            Log.d(LOG_TAG, "Last Message Updated At(Login At)" + messageUpdateAt)
            startMessageGetLoader()
        }
    }

    // onActivityResult
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // 投稿画面からの結果を処理
        if (requestCode == 17701 && resultCode == Activity.RESULT_OK && data != null) {
            val result =  data.extras["SavePostData"] as Boolean
            if (!result) {
                // データ更新失敗のダイアログを作成
                showAlertDialog(this, getString(R.string.error), getString(R.string.failed_to_post))
            }
            val fromType =  data.extras["SavePostDataFrom"] as String
            if (fromType == "Feed") {
                val fragment = supportFragmentManager.findFragmentByTag(
                    "feedFragment") as FeedFragment
                fragment.PrFeedDataReload()
            } else if (fromType == "Account") {
                val fragment = supportFragmentManager.findFragmentByTag(
                    "accountFragment") as AccountFragment
                fragment.dataReload()
            }
        }
        // ユーザーデータ編集画面からの結果を処理
        if (requestCode == 17703 && resultCode == Activity.RESULT_OK && data != null) {
            val result =  data.extras["SaveUserData"] as Boolean
            if (result) {
                val fragment = supportFragmentManager.findFragmentByTag(
                    "accountFragment") as AccountFragment
                fragment.update()
            } else {
                // データ更新失敗のダイアログを作成
                showAlertDialog(this, getString(R.string.error), getString(R.string.user_data_update_error))
            }
        }
        // アカウント画像編集画面からの結果を処理
        if (requestCode == 17704 && resultCode == Activity.RESULT_OK && data != null) {
            val result =  data.extras["SaveAccountImage"] as Boolean
            if (result) {
                val fragment = supportFragmentManager.findFragmentByTag(
                    "accountFragment") as AccountFragment
                fragment.update()
            } else {
                // データ更新失敗のダイアログを作成
                showAlertDialog(this, getString(R.string.error), getString(R.string.image_update_error))
            }
        }
        // Mapフラグメントのパーミッション関連AcitvityResultをキャッチ
        if (requestCode == 17707) {
            val fragment = supportFragmentManager.findFragmentByTag(
                "mapFragment") as MapFragment
            fragment.onActivityResult(requestCode, resultCode, data)
        }
        // 店舗情報編集画面からの結果を処理
        if (requestCode == 17710 && resultCode == Activity.RESULT_OK && data != null) {
            val result =  data.extras["SaveShopData"] as Boolean
            if (result) {
                val fragment = supportFragmentManager.findFragmentByTag(
                    "accountFragment") as AccountFragment
                fragment.update()
            } else {
                // データ更新失敗のダイアログを作成
                showAlertDialog(this, getString(R.string.error), getString(R.string.shop_update_error))
            }
        }
    }

    // onRequestPermissionsResult
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if(requestCode == 17709 && permissions.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val fragment = supportFragmentManager.findFragmentByTag(
                "mapFragment") as MapFragment
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    // onBackPressed
    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    // onNavigationItemSelected
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
        }

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }


    // RankingContentFragmentのonRankClicked
    override fun onRankClicked (username: String) {
        startAccountActivity(this, username, false)
    }


    // MapFragmentのInfoWindowClick
    override fun InfoWindowClick (snippet: String, lat: Double?, lng: Double?, lat1: Double, lng1: Double) {
        var checkInFlg = false
        if (lat != null && lng != null) {
            val distance = getDistance(lat, lng, lat1, lng1)
            Log.i(LOG_TAG, "Distance：" + distance[0].toString())
            if (distance[0] < CHECK_IN_OK_DISTANCE) {
                checkInFlg = true
            }
        }
        startAccountActivity(this, snippet, checkInFlg)
    }

    // MapFragmentのonMapInfoClicked
    override fun onMapInfoClicked(dialog: android.support.v7.app.AlertDialog, username: String, myLat: Double, myLng: Double, wpLat: Double, wpLng: Double) {
        var checkInFlg = false
        val distance = getDistance(myLat, myLng, wpLat, wpLng)
        Log.i(LOG_TAG, "Distance：" + distance[0].toString())
        if (distance[0] < CHECK_IN_OK_DISTANCE) {
            checkInFlg = true
        }
        startAccountActivity(this, username, checkInFlg)
        //dialog.dismiss()
    }

    // MapMarkerInfoDialogFragmentのonCloseClicked
    override fun onCloseClicked(dialog: AlertDialog) {
        dialog.dismiss()
    }


    // FeedFragmentのonAccountImageClicked
    override fun onAccountImageClicked(username: String) {
        startAccountActivity(this, username, false)
    }

    // FeedFragmentのonEditClicked
    override fun onEditClicked(id: Int, username: String, content: String, fromType: String) {
        startEditPostActivity(this, id, username, content, fromType)
    }

    // AccountFragmentとFeedFragmentのonNiceButtonClicked
    override fun onNiceButtonClicked(id: Int, username: String, favorite: Boolean) {
        startNiceCDLoaderLoader(id, username, favorite)
    }

    // AccountFragmentとFeedFragmentのonFavoriteCntClicked
    override fun onFavoriteCntClicked(id: Int) {
        startNiceListActivity(this, id)
    }

    // FeedFragmentのpostButtonOnClicked
    override fun postButtonOnClicked() {
        startSendPostActivity(this, "Feed")
    }

    // FeedFragmentのonMessageNiceButtonClicked
    override fun onMessageNiceButtonClicked(id: Int, username: String, favorite: Boolean) {
        startMessageNiceCDLoaderLoader(id, username, favorite)
    }

    // FeedFragmentのonMessageNiceCntClicked
    override fun onMessageNiceCntClicked(id: Int) {
        startMessageNiceListActivity(this, id)
    }


    // AccountFragmentのaccountEditButtonOnClick
    override fun accountEditButtonOnClick() {
        startEditAccountActivity(this)
    }

    // AccountFragmentのaccountEditButtonOnClick
    override  fun accountImageEditButtonOnClick (imageType: String) {
        startEditAccountImageActivity(this, imageType)
    }

    // AccountFragmentのwpEditButtonOnClick
    override fun wpEditButtonOnClick (ver: String) {
        startEditShopActivity(this, ver)
    }

    // AccountFragmentのwpMapBottunOnClick
    override fun wpMapBottunOnClick (bundle: Bundle) {
        fromAccStartMapFragment(bundle)
    }

    // AccountFragmentのwpUrlButtonOnClick
    override fun wpUrlButtonOnClick (url: String) {
        val ctIntent = CustomTabsIntent.Builder().build()
        ctIntent.launchUrl(this, Uri.parse(url))
    }

    // AccountFragmentのfollowButtonOnClick
    override fun followButtonOnClick(toUsername: String, fromUsername: String, flg: Boolean) {
        // No Action
    }

    // AccountFragmentのfollowCntOnClickk
    override fun followCntOnClick (username: String, flg: Boolean) {
        startFollowListActivity(this, username, flg)
    }

    // AccountFragmentのplayAdButtonOnClick
    override fun playAdButtonOnClick() {
        val context = this
        val intent = Intent(context, AdVampActivity::class.java)
        startActivity(intent)
    }


    // MessageContentFragmentのonClicked
    override fun onClicked(message: MessageDispData) {
        if (username == message.toUsername) {
            startAccountActivity(this, message.fromUsername!!, false)
        } else {
            startAccountActivity(this, message.toUsername!!, false)
        }
    }


    // NoAccountUserFragmentのaccountCreateButtonOnClick
    override fun accountCreateButtonOnClick() {
        startMySignUpActivity(this)
    }

    // NoAccountUserFragmentのaccountConfirmButtonOnClick
    override fun accountConfirmButtonOnClick() {
        startVerificationActivity(this)
    }


    // startNiceCDLoaderLoader
    private fun startNiceCDLoaderLoader(id: Int, username: String, favorite: Boolean) {
        // 引数に値を渡してローダーを起動
        val bundle = Bundle()
        bundle.putInt("id", id)
        bundle.putString("username", username)
        bundle.putBoolean("favorite", favorite)
        supportLoaderManager.restartLoader(13315, bundle, NiceCDLoaderCallbacks(this, this))
    }

    // いいね！に関する処理終了時の処理
    override fun NiceCDLoaderOnLoadFinished(data: Boolean?) {
        if (data != null && data) {
            Log.d(LOG_TAG, "SUCCESS TO NICE")
        }else {
            Log.d(LOG_TAG, "FAILED TO NICE")
        }
    }


    // startMessageNiceCDLoaderLoader
    private fun startMessageNiceCDLoaderLoader(id: Int, username: String, favorite: Boolean) {
        // 引数に値を渡してローダーを起動
        val bundle = Bundle()
        bundle.putInt("id", id)
        bundle.putString("username", username)
        bundle.putBoolean("favorite", favorite)
        supportLoaderManager.restartLoader(13315, bundle, MessageNiceCDLoaderCallbacks(this, this))
    }

    // いいね！に関する処理終了時の処理
    override fun MessageNiceCDLoaderOnLoadFinished(data: Boolean?) {
        if (data != null && data) {
            Log.d(LOG_TAG, "SUCCESS TO NICE")
        }else {
            Log.d(LOG_TAG, "FAILED TO NICE")
        }
    }


    // startMessageGetLoader
    private fun startMessageGetLoader() {
        // メッセージデータの更新分を取得
        Log.i(LOG_TAG, "Start Get User Data")
        val bundle = Bundle()
        bundle.putString("updated_at", messageUpdateAt)
        bundle.putString("username", username)
        supportLoaderManager.restartLoader(13319, bundle,
            MessageGetLoaderCallbacks(this, this))
    }

    // MessageGetLoaderOnLoadFinished
    override fun MessageGetLoaderOnLoadFinished(data: List<MessageData>?) {
        Log.i(LOG_TAG, "Finished Get Message Data")
        if (data != null) {
            Log.i(LOG_TAG, "Success to Get Message Data")
        } else {
            Log.e(LOG_TAG, "Failed to Get Message Data")
        }
    }


    // fromAccStartMapFragment
    private fun fromAccStartMapFragment(bundle: Bundle) {
        val mBtmView = findViewById<BottomNavigationView>(R.id.navigation)
        mBtmView.menu.findItem(R.id.navigation_map).setChecked(true)
        startFragment("mapFragment", false, bundle)
    }

    // startFragment
    private fun startFragment(tag: String, user: Boolean, bundle: Bundle?) {
        val fragmentMap = mutableMapOf<String, Any>()
        fragmentMap["rankingFragment"] = newRankingFragment()
        fragmentMap["mapFragment"] = newMapFragment(bundle)
        fragmentMap["searchFragment"] = newSearchFragment()
        fragmentMap["feedFragment"] = newFeedFragment()
        fragmentMap["accountFragment"] = newAccountFragment()
        //fragmentMap["messageFragment"] = newMessageFragment()
        val fragmentTitleMap = mutableMapOf<String, String>()
        fragmentTitleMap["rankingFragment"] = getString(R.string.title_ranking)
        fragmentTitleMap["mapFragment"] = getString(R.string.title_map)
        fragmentTitleMap["searchFragment"] = getString(R.string.title_search)
        fragmentTitleMap["feedFragment"] = getString(R.string.title_feed)
        fragmentTitleMap["accountFragment"] = getString(R.string.title_account)
        //fragmentTitleMap["messageFragment"] = getString(R.string.title_message)
        fragmentTitle.text = fragmentTitleMap[tag]
        if (user) {
            if (status == 0) {
                if (supportFragmentManager.findFragmentByTag(tag) == null){
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_fragment, fragmentMap[tag] as Fragment, tag)
                        .commit()
                }
            } else  {
                if (supportFragmentManager.findFragmentByTag("noAccountUserFragment") == null) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_fragment, newNoAccountUserFragment(), "noAccountUserFragment")
                        .commit()
                }
                if (status == 1) {
                    startVerificationActivity(this)
                }
            }
        } else {
            if (supportFragmentManager.findFragmentByTag(tag) == null){
                supportFragmentManager.beginTransaction()
                    .replace(R.id.main_fragment, fragmentMap[tag] as Fragment, tag)
                    .commit()
            }
        }
    }
}
