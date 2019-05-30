package com.yanagimachidev.huuurraydevapp


import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.amazonaws.mobile.auth.core.internal.util.ViewHelper
import com.amazonaws.mobile.auth.userpools.CognitoUserPoolsSignInProvider
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.cognitoidentityprovider.*
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler
import java.util.regex.Pattern
import android.text.style.UnderlineSpan
import android.text.SpannableString


// MySignUpActivity
class MySignUpActivity : AppCompatActivity() {

    // 変数定義
    private val LOG_TAG = MySignUpActivity::class.java.simpleName // ログ用にクラス名を取得
    private var username: String? = null // ユーザー名
    private var password: String? = null // パスワード
    private var email: String? = null // メールアドレス
    private var mUserPool: CognitoUserPool? = null // ユーザープール


    // onCreate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_sign_up)
        val context = this

        // StatusBarを表示、透過する設定
        findViewById<View>(android.R.id.content).systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        // 利用規約について
        val serviceTermsText = findViewById<TextView>(R.id.service_terms_text)
        var conServiceTermsText = "上記の" + getString(R.string.create_account) + "ボタンを押すことにより\n"
        val len = conServiceTermsText.length
        conServiceTermsText += "利用規約に同意したことになります"
        val spanStr = SpannableString(conServiceTermsText)
        spanStr.setSpan(UnderlineSpan(), len, len + 4, 0)
        serviceTermsText.text = spanStr

        // ユーザープールのインスタンス化
        val awsConfiguration = AWSConfiguration(this)
        mUserPool = CognitoUserPool(context, awsConfiguration)

        // サインアップボタン
        val sign_up = findViewById<Button>(R.id.sign_up)
        sign_up.setOnClickListener{
            it.notPressTwice()
            // エラーフラグ
            var inValid = true

            // ユーザー名の入力値を取得
            val usernameEditText = findViewById<EditText>(R.id.username)
            val usernameText = usernameEditText.text.toString()

            // メールアドレスの入力値を取得
            val emailEditText = findViewById<EditText>(R.id.email)
            val emailText = emailEditText.text.toString()

            // パスワードの入力値を取得
            val passwordEditText = findViewById<EditText>(R.id.password)
            val passwordText = passwordEditText.text.toString()

            // パスワード確認の入力値の取得
            val passwordConfirmEditText = findViewById<EditText>(R.id.password_confirm)
            val passwordConfirmText = passwordConfirmEditText.text.toString()

            // ユーザ名の未入力エラー
            if (usernameText.isEmpty()) {
                usernameEditText.error = getString(R.string.username_no_value)
                usernameEditText.requestFocus()
                inValid = false
            }

            // ユーザ名の長さエラー
            if (usernameText.length > 20 && inValid) {
                usernameEditText.error = getString(R.string.too_long_username_error)
                usernameEditText.requestFocus()
                inValid = false
            }

            // ユーザー名の入力規則エラー
            val pattern = Pattern.compile("^[0-9a-zA-Z]+$")
            val matcher = pattern.matcher(usernameText)
            if (!matcher.matches() && inValid) {
                usernameEditText.error = getString(R.string.invalid_username)
                usernameEditText.requestFocus()
                inValid = false
            }

            // メールアドレスの未入力エラー
            if (emailText.isEmpty() && inValid) {
                emailEditText.error = getString(R.string.email_no_value)
                emailEditText.requestFocus()
                inValid = false
            }

            // パスワードの未入力エラー
            if (passwordText.isEmpty() && inValid) {
                passwordEditText.error = getString(R.string.password_no_value)
                passwordEditText.requestFocus()
                inValid = false
            }

            // パスワードの長さエラー
            if ((passwordText.length < 8 || passwordText.length > 16)  && inValid) {
                passwordEditText.error = getString(R.string.password_length_error)
                passwordEditText.requestFocus()
                inValid = false
            }

            // パスワード確認エラー
            if (passwordText != passwordConfirmText && inValid) {
                passwordConfirmEditText.error = getString(R.string.password_diff_error)
                passwordConfirmEditText.requestFocus()
                inValid = false
            }

            // エラー無しの場合
            if (inValid) {
                Log.i(LOG_TAG, "Start Sign Up.")

                // 取得した値をセット
                username = usernameText
                password = passwordText
                email = emailText
                val userAttributes = CognitoUserAttributes()
                userAttributes.addAttribute(CognitoUserPoolsSignInProvider.AttributeKeys.EMAIL_ADDRESS, email)

                // サインアップ中のダイアログを作成
                val builder = AlertDialog.Builder(this)
                    .setTitle(getString(R.string.sign_up_wait))
                    .setMessage(getString(R.string.wait))
                val alertDialog = builder.show();


                mUserPool!!.signUpInBackground(username, password, userAttributes, null,
                    object : SignUpHandler {
                        override fun onSuccess(
                            user: CognitoUser,
                            signUpConfirmationState: Boolean,
                            cognitoUserCodeDeliveryDetails: CognitoUserCodeDeliveryDetails
                        ) {
                            alertDialog.dismiss()
                            // SharedPreferencesに書き込み
                            val pref = getSharedPreferences("aws_credentials", Context.MODE_PRIVATE)
                            val editor = pref.edit()
                            editor.putString("username", username)
                            editor.putString("password", password)
                            editor.apply()
                            // 認証コード入力アクティビティスタート
                            startVerificationActivityFromAuth()
                        }

                        override fun onFailure(exception: Exception) {
                            alertDialog.dismiss()
                            showError(
                                if (exception.localizedMessage != null)
                                    getErrorMessageFromException(exception)
                                else
                                    ""
                            )
                        }
                    }
                )
            }
        }
    }


    // startVerificationActivity
    private fun startVerificationActivityFromAuth() {
        val context = this
        val intent = Intent(context, MySignUpConfirmActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    // showError
    protected fun showError(msg: String) {
        // エラーダイアログを表示
        ViewHelper.showDialog(
            this,
            getString(R.string.error),
            getString(R.string.sign_up_error) + "\n" + msg
        )
    }

    // getErrorMessageFromException
    fun getErrorMessageFromException(exception: Exception): String {
        // エラーメッセージを切り取る
        val exception_prefix = "(Service"
        val message = exception.localizedMessage ?: exception.message
        if (message != null){
            val index = message.indexOf(exception_prefix)
            var returnMessage = message
             if (index != -1) {
                 returnMessage = message.substring(0, index)
            }
            if (returnMessage.contains(getString(R.string.user_already_exists_error_English))) {
                returnMessage = getString(R.string.user_already_exists_error_japanese)
            }
            return returnMessage!!
        }
        return ""
    }
}
