package com.yanagimachidev.huuurraydevapp


import android.content.Context
import android.icu.text.SimpleDateFormat
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log


const val DEFAULT_ST_SP = 20
const val CHECK_IN_OK_DISTANCE = 1000
const val USE_ST_SP = 10
const val CHECK_IN_SP = 50
const val SEND_MESSAGE_CHANCE = 50.0
const val SEND_MESSAGE_CHANCE_INT = 50
const val MESSAGE_SP = 20
const val DEFAULT_ZOOM_LEVEL = 18f
const val FIRST_ZOOM_LEVEL = 10f


// StartActivity
class StartActivity : AppCompatActivity(),
    HrUserGetLoaderInterface {

    // 変数定義
    private val LOG_TAG = StartActivity::class.java.simpleName // ログ用にクラス名を取得
    private var username = "" // アカウントを保持していた場合のユーザー名
    private var userUpdatedAt = "" // ユーザー情報を最後にアップデートした日時


    // onCreate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        // 最終ユーザーデータ更新日時を取得
        val acc_pref = getSharedPreferences("account_info", Context.MODE_PRIVATE)
        val acc_editor = acc_pref.edit()
        if (acc_pref.contains("updated_at")) {
            userUpdatedAt = acc_pref.getString("updated_at", "")
        }
        Log.d(LOG_TAG, "Last Updated At(Login At)" + userUpdatedAt)

        // アカウント作成済の場合（認証まで済んでいる）
        val pref = getSharedPreferences("aws_credentials", Context.MODE_PRIVATE)
        if (pref.contains("username") && pref.contains("password") && pref.contains("confirm")) {

            // usernameを取得
            username = pref.getString("username", "")

            // 現在の端末日付を取得
            val df = SimpleDateFormat("yyyy-MM-dd")
            val deviceToday = df.format(System.currentTimeMillis())
            Log.d(LOG_TAG, "Logined At(Today)" + deviceToday)

            // 最終ログイン時の端末日付を取得
            var lastLoginedAt: String = ""
            if (acc_pref.contains("logined_at")) {
                lastLoginedAt = acc_pref.getString("logined_at", "")
            }
            Log.d(LOG_TAG, "Logined At(Last)" + lastLoginedAt)

            // 現在の端末日付と最終ログイン時の端末日付を比較して異なる場合
            if (deviceToday != lastLoginedAt) {

                // 現在の通常ポイントを取得
                var hrUser = queryHrUser(this, username)

                // DBにデータが存在しない場合は、自分のレコードを作成
                var blankResult = true
                if (hrUser.isEmpty()) {
                    val blankDataString = mutableMapOf<String, String>()
                    val blankDataInt = mutableMapOf<String, Int>()
                    val blankDataDouble = mutableMapOf<String, Double>()
                    blankDataString["username"] = username
                    blankResult = upsertHrUser(this, blankDataString, blankDataInt, blankDataDouble)
                    hrUser = queryHrUser(this, username)
                }

                // 利用可能SPが標準以下の場合
                if (hrUser.isNotEmpty() && blankResult) {
                    val st_sp = hrUser["st_sp"] as Int
                    if (st_sp < DEFAULT_ST_SP) {
                        val newDataString = mutableMapOf<String, String>()
                        val newDataInt = mutableMapOf<String, Int>()
                        val newDataDouble = mutableMapOf<String, Double>()
                        newDataString["username"] = username
                        newDataInt["st_sp"] = DEFAULT_ST_SP
                        val result = upsertHrUser(this, newDataString, newDataInt, newDataDouble)
                        if (result) {
                            acc_editor.putString("logined_at", deviceToday)
                            acc_editor.apply()
                        }
                    } else {
                        acc_editor.putString("logined_at", deviceToday)
                        acc_editor.apply()
                    }
                } else {
                    Log.e(LOG_TAG, "Failed to Get Account Record")
                    showAlertDialog(this, getString(R.string.error), getString(R.string.failed_to_get_account))
                    finish()
                }
            }
        }
        startHrUserGetLoader()
    }


    // startHrUserGetLoader
    private fun startHrUserGetLoader () {
        // ユーザーデータの更新分を取得
        Log.i(LOG_TAG, "Start Get User Data")
        val bundle = Bundle()
        bundle.putString("updated_at", userUpdatedAt)
        supportLoaderManager.restartLoader(13305, bundle,
            HrUserGetLoaderCallbacks(this, this))
    }

    // HrUserGetLoaderOnLoadFinished
    override fun HrUserGetLoaderOnLoadFinished(data: List<HrUserAll>?) {
        Log.i(LOG_TAG, "Finished Get User Data")
        if (data != null) {
            Log.i(LOG_TAG, "Success to Get User Data")
        } else {
            Log.e(LOG_TAG, "Failed to Get User Data")
        }
        // メインアクティビティを起動
        val bundle = Bundle()
        val myAuth = MyAuth(this, bundle)
        myAuth.StatusCheck()
    }
}
