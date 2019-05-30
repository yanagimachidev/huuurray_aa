package com.yanagimachidev.huuurraydevapp


import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView


// MapMarkerInfoDialogFragment
class MapMarkerInfoDialogFragment : DialogFragment() {

    // 変数定義
    private val LOG_TAG = MapMarkerInfoDialogFragment::class.java.simpleName // ログ用にクラス名を取得
    private lateinit var dialog: AlertDialog
    private lateinit var alert: AlertDialog.Builder
    private var alertView: View? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: MarkerCellAdapter


    // リスナーを親へ渡すためのインターフェイス
    interface OnFragmentInteractionListener {
        fun onMapInfoClicked(dialog: AlertDialog, username: String, myLat: Double, myLng: Double, wpLat: Double, wpLng: Double)

        fun onCloseClicked(dialog: AlertDialog)
    }
    private var onFragmentInteractionListener: OnFragmentInteractionListener? = null


    // onAttach
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is MapMarkerInfoDialogFragment.OnFragmentInteractionListener) {
            onFragmentInteractionListener = context
        } else {
            throw RuntimeException(context!!.toString() + getString(R.string.no_listener_error))
        }
    }

    // onCreateView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // カスタムレイアウトの生成
        alertView = inflater.inflate(R.layout.fragment_map_marker_info_dialog, null)

        // 引数から値を取得
        val snippet = arguments?.getString("snippet")
        val usernameList = snippet!!.split("&!$")
        val myLat = arguments?.getDouble("myLat")
        val myLng = arguments?.getDouble("myLng")

        val userList = mutableListOf<Map<String, Any?>>()
        usernameList.forEach{
            val user = queryHrUser(activity as Context, it)
            userList.add(user)
        }

        // recyclerViewを設定
        recyclerView = alertView!!.findViewById<RecyclerView>(R.id.map_marker_info)
        layoutManager = LinearLayoutManager(activity as Context)
        recyclerView.layoutManager = layoutManager
        // Adapterをセット
        adapter = MarkerCellAdapter(activity as Context, userList) {
            val listener = context as? MapMarkerInfoDialogFragment.OnFragmentInteractionListener
            listener?.onMapInfoClicked(dialog, it["username"] as String, myLat!!, myLng!!, it["wp1_lat"] as Double, it["wp1_lng"] as Double)
        }
        recyclerView.adapter = adapter

        val closeText = alertView!!.findViewById<TextView>(R.id.close)
        closeText.setOnClickListener {
            val listener = context as? MapMarkerInfoDialogFragment.OnFragmentInteractionListener
            listener?.onCloseClicked(dialog)
        }

        return alertView
    }

    // onCreateDialog
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Dialogを生成
        alert = AlertDialog.Builder(activity!!)
        alert.setView(alertView)
        dialog = alert.create()
        dialog.window.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show();

        return dialog
    }


    // onDetach
    override fun onDetach() {
        super.onDetach()
        onFragmentInteractionListener = null
    }
}
