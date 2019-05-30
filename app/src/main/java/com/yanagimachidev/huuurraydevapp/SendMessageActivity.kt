package com.yanagimachidev.huuurraydevapp


import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView


// SendMessageActivity
class SendMessageActivity : AppCompatActivity(),
    MessageUpsertLoaderInterface,
    PointUpsertLoaderInterface,
    AfterGifAlertDialogFragment.OnFragmentInteractionListener{

    // 変数定義
    private val LOG_TAG = SendMessageActivity::class.java.simpleName // ログ用にクラス名を取得
    private lateinit var toUsername: String
    private lateinit var fromUsername: String
    private lateinit var contentText: String


    // onCreate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_message)

        // StatusBarを表示、透過する設定
        findViewById<View>(android.R.id.content).systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        // チャンス見出しの値をセット
        val chanceEditText =  findViewById<TextView>(R.id.chance)
        val chanceText = SEND_MESSAGE_CHANCE_INT.toString() + getString(R.string.chance_h1)
        chanceEditText.text = chanceText

        // チャンスメッセージの値をセット
        val chanceMessageEditText =  findViewById<TextView>(R.id.chance_message)
        val chanceMessageText = getString(R.string.chance_message) +
                MESSAGE_SP.toString() + getString(R.string.chance_message_end)
        chanceMessageEditText.text = chanceMessageText

        // fromUsernameの取得
        val pref = getSharedPreferences("aws_credentials", Context.MODE_PRIVATE)
        fromUsername = pref.getString("username", "")

        // toUsernameの取得
        toUsername = intent.getStringExtra("toUsername")

        // 送信ボタン
        val save = findViewById<Button>(R.id.save)
        save.setOnClickListener {
            it.notPressTwice()
            // エラーフラグ
            var inValid = true

            // 本文の入力値のセット
            val contentEditText = findViewById<EditText>(R.id.content)
            contentText = contentEditText.text.toString()

            // 本文の未入力エラー
            if (contentText.isEmpty()) {
                contentEditText.error = getString(R.string.no_content_error)
                inValid = false
            }

            // 本文の長さエラー
            if (contentText.length > 200 && inValid) {
                contentEditText.error = getString(R.string.too_long_message_error) +
                        contentText.length + getString(R.string.too_long_error_end)
                contentEditText.requestFocus()
                inValid = false
            }

            // エラー無しの場合
            if (inValid) {
                // ダイアログを表示
                val flagmentManager = supportFragmentManager
                val dialogFragment = AfterGifAlertDialogFragment()
                val toDialogBundle = Bundle()
                toDialogBundle.putString("title", getString(R.string.send_message_confirm))
                toDialogBundle.putString("message", getString(R.string.send_message_alert))
                toDialogBundle.putString("yes", getString(R.string.send))
                toDialogBundle.putString("no", getString(R.string.cancel))
                toDialogBundle.putString("toUsername", toUsername)
                toDialogBundle.putString("fromUsername", fromUsername)
                toDialogBundle.putInt("point", MESSAGE_SP)
                toDialogBundle.putString("type", "mg")
                dialogFragment.setArguments(toDialogBundle)
                dialogFragment.show(flagmentManager, "myAlertDialog")
            }
        }

        // キャンセルボタン
        val cancel = findViewById<Button>(R.id.cancel)
        cancel.setOnClickListener {
            finish()
        }
    }


    // yesButtonOnClick
    override fun yesButtonOnClick(type: Int, args: Bundle?) {
        startMessageUpsertLoader()
        startPointUpsertLoader()
    }

    // noButtonOnClick
    override fun noButtonOnClick(type: Int) {
        // No Action
    }


    // startMessageUpsertLoader
    private fun startMessageUpsertLoader() {
        // 引数に値を渡してローダーを起動
        val bundle = Bundle()
        bundle.putString("toUsername", toUsername)
        bundle.putString("fromUsername", fromUsername)
        bundle.putString("content", contentText)
        supportLoaderManager.restartLoader(13320, bundle,
            MessageUpsertLoaderCallbacks(this, this))
    }

    // MessageUpsertLoaderOnLoadFinished
    override fun MessageUpsertLoaderOnLoadFinished(data: Boolean?) {
        if (data == null || data == false) {
            Log.e(LOG_TAG, "Failed to Save Message Data.")
        } else {
            Log.i(LOG_TAG, "Success to Save Message Data.")
        }
    }


    // startPointUpsertLoader
    private fun startPointUpsertLoader() {
        // 引数に値を渡してローダーを起動
        val bundle = Bundle()
        bundle.putString("toUsername", toUsername)
        bundle.putString("fromUsername", fromUsername)
        bundle.putString("type", "mg")
        bundle.putInt("point", MESSAGE_SP)
        supportLoaderManager.restartLoader(13308, bundle,
            PointUpsertLoaderCallbacks(this, this))
    }

    // PointUpsertLoaderOnLoadFinished
    override fun PointUpsertLoaderOnLoadFinished(data: Boolean?) {
        if(data == null || data == false) {
            Log.e(LOG_TAG, "Failed to Point Upsert")
        } else {
            val intent = Intent()
            intent.putExtra("SavePointData", data)
            setResult(RESULT_OK, intent)
            Log.i(LOG_TAG, "Success to Point Upsert")
        }
    }
}
