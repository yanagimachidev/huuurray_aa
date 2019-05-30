package com.yanagimachidev.huuurraydevapp


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.widget.ImageView


// ImageDisplayDialogFragment
class ImageDisplayDialogFragment : DialogFragment() {

    // 変数定義
    private val LOG_TAG = ImageDisplayDialogFragment::class.java.simpleName // ログ用にクラス名を取得
    private lateinit var dialog: AlertDialog
    private lateinit var alert: AlertDialog.Builder


    // リスナーを親へ渡すためのインターフェイス
    interface OnFragmentInteractionListener {
    }
    private var onFragmentInteractionListener: OnFragmentInteractionListener? = null


    // onAttach
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is ImageDisplayDialogFragment.OnFragmentInteractionListener) {
            onFragmentInteractionListener = context
        } else {
            throw RuntimeException(context!!.toString() + getString(R.string.no_listener_error))
        }
    }

    // onCreateDialog
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // 引数から値を取得
        val imageType = arguments?.getString("imageType")
        val imageName = arguments?.getString("imageName")

        // カスタムレイアウトの生成
        val alertView = activity!!.layoutInflater.inflate(R.layout.fragment_image_display_dialog, null)

        // 画像を表示
        val dispImage = alertView.findViewById<ImageView>(R.id.image)
        if (imageType != null && imageName != null && imageType != "" && imageName != "") {
            getS3ImageViaGlide(activity as Context, imageType, imageName, dispImage)
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
