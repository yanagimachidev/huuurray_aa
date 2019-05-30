package com.yanagimachidev.huuurraydevapp


import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView


// SendPostActivity
class SendPostActivity : AppCompatActivity(),
    PostDataUpsertLoaderInterface {

    // 変数定義
    private val LOG_TAG = SendPostActivity::class.java.simpleName // ログ用にクラス名を取得
    private lateinit var contentEditText: TextView
    private lateinit var fromType: String


    // onCreate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        // Loaderの呼び出し元を取得
        fromType = intent.getStringExtra("fromType")

        // 本文の項目を初期化
        contentEditText = findViewById<EditText>(R.id.content)

        // usernameの取得
        val pref = getSharedPreferences("aws_credentials", Context.MODE_PRIVATE)
        val username = pref.getString("username", "")

        // 編集時の処理
        val id = intent.getIntExtra("id", 0)
        if (id != 0) {
            val usernamePost = intent.getStringExtra("username")
            val content = intent.getStringExtra("content")

            // 別ユーザーの投稿は編集できない
            if (username != usernamePost) {
                Log.e(LOG_TAG, "User Invalid")
                val intent = Intent()
                intent.putExtra("SavePostData", false)
                setResult(RESULT_OK, intent)
                finish()
            }

            // 本文の初期値をセット
            contentEditText.text = content
        }

        // 保存ボタン
        val save = findViewById<Button>(R.id.save)
        save.setOnClickListener {
            it.notPressTwice()
            // エラーフラグ
            var inValid = true

            // 入力値を取得
            val contentText = contentEditText.text.toString()

            // 本文の未入力エラー
            if (contentText.isEmpty()) {
                contentEditText.error = getString(R.string.no_content_error)
                inValid = false
            }

            // 本文の長さエラー
            if (contentText.length > 500 && inValid) {
                contentEditText.error = getString(R.string.too_long_post_error) +
                        contentText.length + getString(R.string.too_long_error_end)
                contentEditText.requestFocus()
                inValid = false
            }

            // エラー無しの場合
            if (inValid) {
                startPostDataUpsertLoader(id, username, contentText)
            }
        }
    }


    // startPostDataUpsertLoader
    private fun startPostDataUpsertLoader(id: Int, username: String,  contentText: String) {
        // 投稿
        val bundle = Bundle()
        bundle.putInt("id", id)
        bundle.putString("username", username)
        bundle.putString("content", contentText)
        supportLoaderManager.restartLoader(13306, bundle,
            PostDataUpsertLoaderCallbacks(this, this))
    }

    // PostDataUpsertLoaderOnLoadFinished
    override fun PostDataUpsertLoaderOnLoadFinished (data: Boolean?) {
        var result: Boolean = true
        if (data == null || data == false) {
            Log.e(LOG_TAG, "Failed to Save Post Data.")
            result = false
        }
        val intent = Intent()
        intent.putExtra("SavePostData", result)
        intent.putExtra("SavePostDataFrom", fromType)
        setResult(RESULT_OK, intent)
        finish()
    }
}
