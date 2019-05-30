package com.yanagimachidev.huuurraydevapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import jp.supership.vamp.AdvancedListener
import jp.supership.vamp.VAMP
import jp.supership.vamp.VAMPError
import jp.supership.vamp.VAMPListener

private const val VAMP_AD_ID = "12416";
private const val TAG = "VAMP"

class AdVampActivity : AppCompatActivity() {

        private lateinit var vamp: VAMP

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            vamp = VAMP.getVampInstance(this, VAMP_AD_ID);
            vamp.setVAMPListener(AdListener(vamp))      // VAMPListenerをセット
            vamp.setAdvancedListener(AdvListener()) // AdvancedListenerをセット

            VAMP.setTestMode(true);  // テストモードを有効にする
            VAMP.setDebugMode(true); // デバッグモードを有効にする

            // 広告取得
            vamp.load();
        }


        private class AdListener(private val vamp: VAMP) : VAMPListener {

            override fun onReceive(placementId: String?, adnwName: String?) {
                // 広告表示の準備完了
                Log.d(TAG, "onReceive(" + adnwName + ")");
                // 広告表示
                vamp.show();
            }

            override fun onExpired(placementId: String?) {
                // 有効期限オーバー
                // ＜注意：onReceiveを受けてからの有効期限が切れました。showするには再度loadを行う必要が有ります＞
                Log.d(TAG, "onExpired()");
            }

            override fun onFail(p0: String?, p1: VAMPError?) {
                // 廃止予定
            }

            override fun onComplete(placementId: String?, adnwName: String?) {
                // 動画再生正常終了（インセンティブ付与可能）
                Log.d(TAG, "onComplete(" + adnwName + ")");
            }

            override fun onFailedToShow(error: VAMPError?, placementId: String?) {
                // 動画の表示に失敗
                Log.e(TAG, "onFailedToShow() " + error);
            }

            override fun onFailedToLoad(error: VAMPError?, placementId: String?) {
                // 広告準備に失敗
                Log.e(TAG, "onFailedToLoad() " + error);
            }

            override fun onClose(placementId: String?, adnwName: String?) {
                // 動画プレーヤーやエンドカードが表示終了
                // ＜注意：ユーザキャンセルなども含むので、インセンティブ付与はonCompleteで判定すること＞
                Log.d(TAG, "onClose(" + adnwName + ")");
            }
        }

        private class AdvListener : AdvancedListener {
            override fun onLoadResult(placementId: String?, success: Boolean, adnwName: String?, message: String?) {
                // アドネットワークごとの広告取得結果
                Log.d(TAG, "onLoadResult(" + adnwName + ") " + message);
            }

            override fun onLoadStart(placementId: String?, adnwName: String?) {
                // 優先順位順にアドネットワークごとの広告取得を開始
                Log.d(TAG, "onLoadStart(" + adnwName + ")");
            }
        }
    }
