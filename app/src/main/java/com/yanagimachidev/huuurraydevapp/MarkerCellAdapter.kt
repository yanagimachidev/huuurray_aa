package com.yanagimachidev.huuurraydevapp


import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView


// MarkerCellAdapter
class MarkerCellAdapter (
    private val context: Context,
    private val userList: List<Map<String, Any?>>,
    private val onRankClicked: (user: Map<String, Any?>) -> Unit
) : RecyclerView.Adapter<MarkerCellAdapter.MarkerCellVIewHolder>() {

    // 変数定義
    private val LOG_TAG = MarkerCellAdapter::class.java.simpleName // ログ用にクラス名を取得
    private val inflater = LayoutInflater.from(context)


    // getItemCount
    override fun getItemCount(): Int = userList.size

    // onCreateViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarkerCellVIewHolder {

        val view = inflater.inflate(R.layout.marker_cell, parent, false)
        val viewHolder = MarkerCellVIewHolder(view)

        view.setOnClickListener {
            val position = viewHolder.adapterPosition
            val user = userList[position]
            onRankClicked(user)
        }

        return viewHolder
    }

    // onBindViewHolder
    override fun onBindViewHolder(holder: MarkerCellVIewHolder, position: Int) {
        val user = userList[position]
        // 表示名をセット
        holder.dispName.text = user["username"] as String
        if (user["disp_name"] != "" && user["disp_name"] != null) {
            holder.dispName.text = user["disp_name"] as String
        }
        // 店舗名をセット
        holder.wpName.text = context.getString(R.string.no_shop_data)
        if (user["wp1_name"] != "" && user["wp1_name"] != null) {
            val conWpName = "@" + user["wp1_name"] as String
            holder.wpName.text = conWpName
        }
        // プロフィールをセット
        holder.profile.text = ""
        if (user["profile"] != "" && user["profile"] != null) {
            var conProfile = user["profile"] as String
            conProfile = conProfile.replace("\n", " ")
            conProfile = conProfile.replace("\r", " ")
            if (conProfile.length > 30) {
                conProfile = conProfile.substring(0, 29) + "…"
            }
            holder.profile.text = conProfile
        }
        // 画像をセット
        if (user["account_image"] != "" && user["account_image"] != "null") {
            getS3ImageViaGlide(context, "account", user["account_image"] as String, holder.accountImage)
        } else {
            holder.accountImage.setImageDrawable(context.getDrawable(R.drawable.ic_account_circle_white_24dp))
        }
    }

    // ViewHolder
    class MarkerCellVIewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // 変数定義
        private val LOG_TAG = MarkerCellVIewHolder::class.java.simpleName // ログ用にクラス名を取得
        val accountImage = view.findViewById<ImageView>(R.id.avatar_image)
        val dispName = view.findViewById<TextView>(R.id.disp_name)
        val wpName = view.findViewById<TextView>(R.id.wp_name)
        val profile = view.findViewById<TextView>(R.id.profile)
    }
}