package com.yanagimachidev.huuurraydevapp


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView


// NoAccountUserFragment
class NoAccountUserFragment : Fragment() {

    // 変数定義
    private val LOG_TAG = NoAccountUserFragment::class.java.simpleName // ログ用にクラス名を取得

    // リスナーを親へ渡すためのインターフェイス
    interface OnFragmentInteractionListener {
        // アカウント作成ボタン押下時のイベント
        fun accountCreateButtonOnClick()
        // アカウント認証ボタン押下時のイベント
        fun accountConfirmButtonOnClick()
    }
    private var onFragmentInteractionListener: OnFragmentInteractionListener? = null


    // onAttach
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            onFragmentInteractionListener = context
        } else {
            throw RuntimeException(context!!.toString() + getString(R.string.no_listener_error))
        }
    }

    // onCreateView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // layoutファイルを指定
        val view = inflater.inflate(R.layout.fragment_no_account_user, container, false)
        val accountCreateButton = view.findViewById<Button>(R.id.account_create)

        // ユーザーデータの取得
        val pref = activity!!.getSharedPreferences("aws_credentials", Context.MODE_PRIVATE)
        if (pref.contains("username") && pref.contains("password")) {
            val noAccountMesse = view.findViewById<TextView>(R.id.no_account_message)
            noAccountMesse.setText(getString(R.string.please_confirm))
            val befAccountCreate = view.findViewById<TextView>(R.id.message)
            befAccountCreate.setText(getString(R.string.before_confirm_message))
            accountCreateButton.setText(getString(R.string.confirm_execute))
            accountCreateButton.setOnClickListener {
                it.notPressTwice()
                val listener = context as? OnFragmentInteractionListener
                listener?.accountConfirmButtonOnClick()
            }
        } else {
            // アカウント作成ボタンへのイベント登録
            accountCreateButton.setOnClickListener {
                it.notPressTwice()
                val listener = context as? OnFragmentInteractionListener
                listener?.accountCreateButtonOnClick()
            }
        }
        return view
    }

    // onDetach
    override fun onDetach() {
        super.onDetach()
        onFragmentInteractionListener = null
    }
}


fun newNoAccountUserFragment() : NoAccountUserFragment {
    return NoAccountUserFragment()
}
