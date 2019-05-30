package com.yanagimachidev.huuurraydevapp


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.Button
import android.widget.TextView


// NoGifAlertDialogFragment
class NoGifAlertDialogFragment : DialogFragment() {

    // 変数定義
    private val LOG_TAG = NoGifAlertDialogFragment::class.java.simpleName // ログ用にクラス名を取得
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
        if (context is NoGifAlertDialogFragment.OnFragmentInteractionListener) {
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

        // カスタムレイアウトの生成
        val alertView = activity!!.layoutInflater.inflate(R.layout.fragment_no_gif_alert_dialog, null)

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
            val listener = context as? NoGifAlertDialogFragment.OnFragmentInteractionListener
            listener?.noButtonOnClick(1)
        }

        // yesボタンの設定
        val yesButton = alertView.findViewById<Button>(R.id.yes_button)
        yesButton.text = yes
        yesButton.setOnClickListener{
            // リスナーを呼び出す
            val listener = context as? NoGifAlertDialogFragment.OnFragmentInteractionListener
            val bundle = Bundle()
            listener?.yesButtonOnClick(1, bundle)
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
