package com.yanagimachidev.huuurraydevapp


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.customtabs.CustomTabsIntent
import android.util.Log
import android.view.View


// AccountActivity
class AccountActivity() :
    AppCompatActivity(),
    PrFeedFragment.OnFragmentInteractionListener,
    AccountFragment.OnFragmentInteractionListener,
    AfterGifAlertDialogFragment.OnFragmentInteractionListener,
    ImageDisplayDialogFragment.OnFragmentInteractionListener,
    FollowUpsertLoaderInterface,
    NiceCDLoaderInterface,
    PointUpsertLoaderInterface {

    // 変数定義
    private val LOG_TAG = AccountActivity::class.java.simpleName // ログ用にクラス名を取得


    // onCreate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_for_fragment_blank)

        // StatusBarを表示、透過する設定
        findViewById<View>(android.R.id.content).systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        // フラグメントをセット
        if (supportFragmentManager.findFragmentByTag("accountFragment") == null){
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_fragment, newAccountFragment(), "accountFragment")
                .commit()
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
        }
        // メッセージ送信画面からの結果を処理
        if (requestCode == 17702 && resultCode == Activity.RESULT_OK && data != null) {
            val result =  data.extras["SavePointData"] as Boolean
            if (result) {
                val fragment = supportFragmentManager.findFragmentByTag(
                    "accountFragment") as AccountFragment
                fragment.update()
            }
        }
    }


    // AccountFragmentのaccountEditButtonOnClick
    override fun accountEditButtonOnClick() {
        // No action
    }

    // AccountFragmentのaccountImageEditButtonOnClick
    override fun accountImageEditButtonOnClick(imageType: String) {
        // No action
    }

    // AccountFragmentのwpEditButtonOnClick
    override fun wpEditButtonOnClick(ver: String) {
        // No action
    }

    // AccountFragmentとFeedFragmentのonEditClicked
    override fun onEditClicked(id: Int, username: String, content: String, fromType: String) {
        startEditPostActivity(this, id, username, content, fromType)
    }

    // AccountFragmentのfollowButtonOnClick
    override fun followButtonOnClick(toUsername: String, fromUsername: String, flg: Boolean) {
        startFolloeUpsertLoader(toUsername, fromUsername, flg)
    }

    // AccountFragmentのfollowCntOnClick
    override fun followCntOnClick (username: String, flg: Boolean) {
        startFollowListActivity(this, username, flg)
    }

    // AccountFragmentのwpMapBottunOnClick
    override fun wpMapBottunOnClick (bundle: Bundle) {
        val myAuth = MyAuth(this, bundle)
        myAuth.StatusCheck()
    }

    // AccountFragmentのwpUrlButtonOnClick
    override fun wpUrlButtonOnClick (url: String) {
        val ctIntent = CustomTabsIntent.Builder().build()
        ctIntent.launchUrl(this, Uri.parse(url))
    }

    // AfterGifAlertDialogFragmentのyesButtonOnClick
    override fun yesButtonOnClick (type: Int, args: Bundle?) {
        startPointUpsertLoader(args)
    }

    // AfterGifAlertDialogFragmentのnoButtonOnClick
    override fun noButtonOnClick(type: Int) {
        val fragment = supportFragmentManager.findFragmentByTag(
            "accountFragment") as AccountFragment
        fragment.dialogCancel()
    }

    // AccountFragmentのpostButtonOnClick
    override fun postButtonOnClicked() {
        // No Action
    }

    // AccountFragmentのonAccountImageClicked
    override fun onAccountImageClicked(username: String) {
        // No Action
    }

    // AccountFragmentのonFavoriteCntClicked
    override fun onFavoriteCntClicked(id: Int) {
        startNiceListActivity(this, id)
    }

    // AccountFragmentのonNiceButtonClicked
    override fun onNiceButtonClicked(id: Int, username: String, favorite: Boolean) {
        startNiceCDLoaderLoader(id, username, favorite)
    }

    // playAdButtonOnClick
    override fun playAdButtonOnClick() {
        // No Action
    }


    // startPointUpsertLoader
    private fun startPointUpsertLoader(args: Bundle?) {
        // 引数に値を渡してローダーを起動
        val bundle = Bundle()
        bundle.putString("toUsername", args?.getString("toUsername"))
        bundle.putString("fromUsername", args?.getString("fromUsername"))
        bundle.putString("type", args?.getString("type"))
        bundle.putInt("point", args!!.getInt("point"))
        supportLoaderManager.restartLoader(13308, bundle,
            PointUpsertLoaderCallbacks(this, this))
    }

    // PointUpsertLoaderOnLoadFinished
    override fun PointUpsertLoaderOnLoadFinished(data: Boolean?) {
        if(data != null) {
            if (data){
                Log.d(LOG_TAG, "Success to Point Upsert")
                val fragment = supportFragmentManager.findFragmentByTag(
                    "accountFragment") as AccountFragment
                fragment.update()
                return
            }
        }
        Log.e(LOG_TAG, "Failed to Point Upsert")
        return
    }


    // startFolloeUpsertLoader
    private fun startFolloeUpsertLoader(toUsername: String, fromUsername: String, flg: Boolean) {
        // 引数に値を渡してローダーを起動
        val bundle = Bundle()
        bundle.putString("toUsername", toUsername)
        bundle.putString("fromUsername", fromUsername)
        bundle.putBoolean("flg", flg)
        supportLoaderManager.restartLoader(13314, bundle,
            FollowUpsertLoaderCallbacks(this, this))
    }

    // FollowUpsertLoaderOnLoadFinished
    override fun FollowUpsertLoaderOnLoadFinished(data: Boolean?) {
        if (data != null && data) {
            val fragment = supportFragmentManager.findFragmentByTag(
                "accountFragment") as AccountFragment
            fragment.update()
        }
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
}
