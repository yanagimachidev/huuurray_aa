package com.yanagimachidev.huuurraydevapp


import android.content.Context
import android.icu.text.SimpleDateFormat
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.VerificationHandler
import java.lang.Exception
import java.util.regex.Pattern


// MySignUpConfirmActivity
class MySignUpConfirmActivity : AppCompatActivity(),
    HrUserUpsertLoaderInterface {

    // 変数定義
    private val LOG_TAG = MySignUpConfirmActivity::class.java.simpleName // ログ用にクラス名を取得
    private lateinit var username: String
    private lateinit var alertDialog: AlertDialog


    // onCreate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_sign_up_confirm)

        // ユーザープールのインスタンス化
        val awsConfiguration = AWSConfiguration(this)
        val mUserPool = CognitoUserPool(this, awsConfiguration)

        // 項目の初期設定
        val pref = getSharedPreferences("aws_credentials", Context.MODE_PRIVATE)
        username = pref.getString("username", "")
        val cognitoUser = mUserPool.getUser(username)
        val verificationCodeEditText = findViewById<EditText>(R.id.verification_code)
        verificationCodeEditText.requestFocus()

        // 確認コード送信ボタン
        val verification = findViewById<Button>(R.id.verification)
        verification.setOnClickListener {
            it.notPressTwice()
            // エラーフラグ
            var inValid = true

            // 確認コードの入力値を取得
            val verificationCodeText = verificationCodeEditText.text.toString()

            // 確認コードの未入力エラー
            if (verificationCodeText.isEmpty()) {
                verificationCodeEditText.error = getString(R.string.verification_code_no_value)
                inValid = false
            }

            // 確認コードの長さエラー
            if (verificationCodeText.length != 6 && inValid) {
                verificationCodeEditText.error = getString(R.string.verification_code_invalid)
                verificationCodeEditText.requestFocus()
                inValid = false
            }

            // ユーザー名の入力規則エラー
            val pattern = Pattern.compile("^[0-9]+$")
            val matcher = pattern.matcher(verificationCodeText)
            if (!matcher.matches() && inValid) {
                verificationCodeEditText.error = getString(R.string.verification_code_invalid)
                verificationCodeEditText.requestFocus()
                inValid = false
            }
            
            // エラー無しの場合
            if (inValid) {
                Log.i(LOG_TAG, "Verification Start.")

                // コンファーム中のダイアログを作成
                val builder = AlertDialog.Builder(this)
                    .setTitle(getString(R.string.confirming))
                    .setMessage(getString(R.string.wait))
                alertDialog = builder.show();

                cognitoUser.confirmSignUpInBackground(verificationCodeText, true,
                    object : GenericHandler {
                        override fun onSuccess() {
                            Log.i(LOG_TAG, "Confirmed.")
                            // SharedPreferencesに書き込み
                            val editor = pref.edit()
                            editor.putString("confirm", "OK")
                            editor.apply()
                            // 最初のログイン時刻をセット
                            val df = SimpleDateFormat("yyyy-MM-dd")
                            val deviceToday = df.format(System.currentTimeMillis())
                            val acc_pref = getSharedPreferences("account_info", Context.MODE_PRIVATE)
                            val acc_editor = acc_pref.edit()
                            acc_editor.putString("logined_at", deviceToday)
                            acc_editor.apply()
                            // ユーザーを作成
                            startHrUserUpsertLoader()
                        }

                        override fun onFailure(exception: java.lang.Exception?) {
                            Log.e(LOG_TAG, "Failed to Confirm User", exception)
                            alertDialog.dismiss()
                            // 確認コードの認証に失敗のダイアログを作成
                            showAlertDialog(this@MySignUpConfirmActivity, getString(R.string.error), getString(R.string.verification_code_error))
                        }
                    }
                )
            }
        }

        // 確認コード再送信ボタン
        val resend = findViewById<Button>(R.id.resend)
        resend.setOnClickListener {
            Log.i(LOG_TAG, "Resend Start.")
            // 確認コード送信済ダイアログを作成
            showAlertDialog(this, "", getString(R.string.vf_code_resend_success))
            cognitoUser.resendConfirmationCodeInBackground(object : VerificationHandler {
                override fun onSuccess(verificationCodeDeliveryMedium: CognitoUserCodeDeliveryDetails?) {
                    Log.i(LOG_TAG, "Success to Resend")
                }

                override fun onFailure(exception: Exception?) {
                    // 確認コードの送信に失敗のダイアログを作成
                    showAlertDialog(this@MySignUpConfirmActivity, getString(R.string.error), getString(R.string.vf_code_resend_error))
                }
            })
        }
    }


    // startHrUserUpsertLoader
    private fun startHrUserUpsertLoader() {
        // 引数に値を渡してローダーを起動
        val bundle = Bundle()
        bundle.putString("username", username)
        bundle.putString("dispName", "")
        bundle.putString("profile", "")
        supportLoaderManager.restartLoader(13301, bundle,
            HrUserUpsertLoaderCallbacks(this, this))
    }

    // HrUserUpsertLoaderOnLoadFinished
    override fun HrUserUpsertLoaderOnLoadFinished(data: HrUser?) {
        if (data == null) {
            Log.e(LOG_TAG, "Failed to Create User Record")
        }
        alertDialog.dismiss()
        // メインアクティビティを起動
        val bundle = Bundle()
        val myAuth = MyAuth(this, bundle)
        myAuth.StatusCheck()
    }
}
