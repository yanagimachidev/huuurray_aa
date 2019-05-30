package com.yanagimachidev.huuurraydevapp


import android.app.Dialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.target.CustomViewTarget


// AfterGifAlertDialogFragment
class AfterGifAlertDialogFragment : DialogFragment() {

    // 変数定義
    private val LOG_TAG = AfterGifAlertDialogFragment::class.java.simpleName // ログ用にクラス名を取得
    private lateinit var dialog: AlertDialog
    private lateinit var alert: AlertDialog.Builder


    // リスナーを親へ渡すためのインターフェイス
    interface OnFragmentInteractionListener {
        fun yesButtonOnClick(type: Int, args: Bundle?)

        fun noButtonOnClick(type: Int)
    }
    private var onFragmentInteractionListener: OnFragmentInteractionListener? = null


    // onAttach
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is AfterGifAlertDialogFragment.OnFragmentInteractionListener) {
            onFragmentInteractionListener = context
        } else {
            throw RuntimeException(context!!.toString() + getString(R.string.no_listener_error))
        }
    }

    // onCreateDialog
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // 引数から値を取得
        val title = arguments?.getString("title")
        val message = arguments?.getString("message")
        val yes = arguments?.getString("yes")
        val no = arguments?.getString("no")
        val toUsername = arguments?.getString("toUsername")
        val fromUsername = arguments?.getString("fromUsername")
        val point = arguments?.getInt("point")
        val sendSpTo = arguments?.getInt("sendSpTo")
        val type = arguments?.getString("type")

        // カスタムレイアウトの生成
        val alertView = activity!!.layoutInflater.inflate(R.layout.fragment_after_gif_alert_dialog, null)

        // 画像を一旦非表示
        val goodJobImage = alertView.findViewById<ImageView>(R.id.good_job_gif)
        goodJobImage.setVisibility(View.GONE)

        // タイトルをセット
        val titleText = alertView.findViewById<TextView>(R.id.title)
        titleText.text = title

        // メッセージをセット
        val messageText = alertView.findViewById<TextView>(R.id.message)
        messageText.text = message

        // noボタンの設定
        val noButton = alertView.findViewById<Button>(R.id.no_button)
        noButton.text = no
        noButton.setOnClickListener{
            dialog.dismiss()
            val listener = context as? AfterGifAlertDialogFragment.OnFragmentInteractionListener
            listener?.noButtonOnClick(1)
        }

        // yesボタンの設定
        val yesButton = alertView.findViewById<Button>(R.id.yes_button)
        yesButton.text = yes
        yesButton.setOnClickListener{
            // リスナーを呼び出す
            val listener = context as? AfterGifAlertDialogFragment.OnFragmentInteractionListener
            val bundle = Bundle()
            bundle.putString("toUsername", toUsername)
            bundle.putString("fromUsername", fromUsername)
            bundle.putInt("point", point!!)
            bundle.putString("type", type)
            listener?.yesButtonOnClick(1, bundle)

            // 表示する画像を選択
            var drawableResource = R.drawable.good_job1
            if (type == "mg") {
                drawableResource = R.drawable.dora_mail_send
            }

            // 画像をセット
            Glide
                .with(this)
                .asGif()
                .load(drawableResource)
                .into(object: CustomViewTarget<ImageView, GifDrawable>(goodJobImage) {
                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        Log.d("Glide", "onLoadFailed")
                    }

                    override fun onResourceCleared(placeholder: Drawable?) {
                        Log.d("Glide", "onResourceCleared")
                    }

                    override fun onResourceReady(
                        resource: GifDrawable,
                        transition: com.bumptech.glide.request.transition.Transition<in GifDrawable>?
                    ) {
                        Log.d("Glide", "onResourceReady")
                        goodJobImage.setImageDrawable(resource)
                        resource.start()
                        var delayMillis: Long = 2000
                        if (type == "mg") {
                            delayMillis = 750
                        }
                        // 2秒後にダイアログを閉じる
                        val handler = Handler()
                        val func = Runnable {
                            if (sendSpTo != null && type != "mg") {
                                if (Math.floor((sendSpTo + point) / SEND_MESSAGE_CHANCE) != Math.floor(sendSpTo / SEND_MESSAGE_CHANCE)) {
                                    startSendMessageActivity(activity!!, toUsername!!)
                                }
                            }
                            dialog.dismiss()
                            if (type == "mg") {
                                activity!!.finish()
                            }
                        }

                        handler.postDelayed(func, delayMillis)
                    }
                })

            // GIF画像の表示
            titleText.setVisibility(View.GONE)
            messageText.setVisibility(View.GONE)
            yesButton.setVisibility(View.GONE)
            noButton.setVisibility(View.GONE)
            goodJobImage.setVisibility(View.VISIBLE)
        }

        // ViewをAlertDialog.Builderに追加
        alert = AlertDialog.Builder(activity!!)
        alert.setView(alertView);

        // Dialogを生成
        dialog = alert.create();
        dialog.show();

        return dialog
    }

    // onDetach
    override fun onDetach() {
        super.onDetach()
        onFragmentInteractionListener = null
    }
}
