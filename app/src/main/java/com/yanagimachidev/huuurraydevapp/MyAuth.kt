package com.yanagimachidev.huuurraydevapp


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat.startActivity
import android.util.Log
import com.amazonaws.mobile.auth.core.IdentityManager
import com.amazonaws.mobile.auth.core.IdentityProvider
import com.amazonaws.mobile.auth.core.signin.SignInManager
import com.amazonaws.mobile.auth.core.signin.SignInProviderResultHandler
import com.amazonaws.mobile.auth.userpools.CognitoUserPoolsSignInProvider
import com.amazonaws.mobile.config.AWSConfiguration
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler
import java.lang.Exception


// MyAuth
class MyAuth (private val activity: Activity, private val bundle: Bundle) {

    // 変数定義
    private val LOG_TAG = MyAuth::class.java.simpleName // ログ用にクラス名を取得
    private val context = activity as Context
    private var status: Int? = null // ユーザーの認証状況＝0:SignIn, 1:NotConfirm, 2:NotSignUp
    private var username: String? = null // ユーザー名
    private var password: String? = null // パスワード
    private var mUserPool: CognitoUserPool? = null // ユーザープール
    private var forMapFlg = false // アカウントMapフラグメントを開く
    private var accLat: Double? = null // アカウント緯度
    private var accLng: Double? = null // アカウント経度

    // signInProviderResultHandler
    private val signInProviderResultHandler = object : SignInProviderResultHandler {
        // onSuccess
        override fun onSuccess(provider: IdentityProvider?) {
            if (provider == null) {
                autoSignIn()
            }
        }

        // onCancel
        override fun onCancel(provider: IdentityProvider?) {
            autoSignIn()
        }
        // onError
        override fun onError(provider: IdentityProvider?, ex: Exception?) {
            autoSignIn()
        }
    }

    // authenticationHandler
    private val authenticationHandler = object : AuthenticationHandler {
        override fun onSuccess(userSession: CognitoUserSession?, newDevice: CognitoDevice?) {
            Log.i(LOG_TAG, "Success to Login" + userSession?.getIdToken())
            status = 0
            startMainActivityFromAuth()
        }

        override fun onFailure(exception: java.lang.Exception?) {
            Log.e(LOG_TAG, "Failed to login", exception)
            status = 0
            startMainActivityFromAuth()
        }

        override fun getAuthenticationDetails(
            authenticationContinuation: AuthenticationContinuation?,
            userId: String?
        ) {
            Log.i(LOG_TAG, "Get AuthenticationDetails")
            if (null != username && null != password) {
                // 続きのタスクを実行
                val authenticationDetails = AuthenticationDetails(username, password, null);
                authenticationContinuation?.setAuthenticationDetails(authenticationDetails);
                authenticationContinuation?.continueTask();
            }
        }

        // 未使用
        override fun authenticationChallenge(continuation: ChallengeContinuation?) {
            Log.e(LOG_TAG, "Authentication Challenge")
            return
        }
        override fun getMFACode(continuation: MultiFactorAuthenticationContinuation?) {
            Log.e(LOG_TAG, "Get MFACode")
            return
        }
    }


    fun StatusCheck() {
        Log.d(LOG_TAG, "Start StatusCheck")

        // 引数の情報を取得
        if (bundle.containsKey("forMapFlg")) {
            forMapFlg = bundle.getBoolean("forMapFlg")
        }
        if (forMapFlg) {
            accLat = bundle.getDouble("AccLat")
            accLng = bundle.getDouble("AccLng")
        }

        // AWSの設定ファイルを取得
        val awsConfiguration = AWSConfiguration(context)
        mUserPool = CognitoUserPool(context, awsConfiguration)
        // 認証情報を取得
        //val credenetialsProvider = CognitoCachingCredentialsProvider(context, awsConfiguration)
        // IdentityManagerのインスタンス化
        val identityManager = IdentityManager(context, awsConfiguration)
        // 自身をデフォルトのIdentityManagerに設定
        IdentityManager.setDefaultIdentityManager(identityManager)
        // CognitoUserPoolsSignInProviderをSignInProviderに追加
        identityManager.addSignInProvider(CognitoUserPoolsSignInProvider::class.java)
        // SignInProviderをインスタンス化させる
        val signInManager = SignInManager.getInstance(context)
        // Sign-In後の処理を設定
        signInManager.setProviderResultsHandler(activity, signInProviderResultHandler)
        // 過去にSign-Inしていた場合はSignInProviderを取得
        val signInProvider = signInManager.getPreviouslySignedInProvider()
        // SharedPreferencesの読み込み
        val pref = activity.getSharedPreferences("aws_credentials", Context.MODE_PRIVATE)
        // 過去にサインインプロバイダーが取得できた場合
        if (signInProvider != null) {
            // Sign-Inのstatusを取得
            if (signInProvider.refreshUserSignInState()) {
                status = 0
                startMainActivityFromAuth()
            }
        }
        // サインインできていない場合
        if(status != 0) {
            // usernameとパスワードを取得できていて認証済
            if (pref.contains("username") && pref.contains("password") && pref.contains("confirm")) {
                autoSignIn()
            } else if (pref.contains("username") && pref.contains("password")) {
                status = 1
                startMainActivityFromAuth()
            } else {
                status = 2
                startMainActivityFromAuth()
            }
        }
    }

    // autoSignIn
    private fun autoSignIn() {
        val pref = activity.getSharedPreferences("aws_credentials", Context.MODE_PRIVATE)
        username = pref.getString("username", "")
        password = pref.getString("password", "")
        val cognitoUser = mUserPool!!.getUser(username)
        cognitoUser.getSessionInBackground(authenticationHandler)
    }

    // startMainActivity
    private fun startMainActivityFromAuth() {
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra("status", status)
        if (forMapFlg) {
            intent.putExtra("forMapFlg", forMapFlg)
            intent.putExtra("AccLat", accLat)
            intent.putExtra("AccLng", accLng)
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        Log.d(LOG_TAG, "MyAuth status：" + status)
        startActivity(context, intent, null)
    }
}
