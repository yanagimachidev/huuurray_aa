package com.yanagimachidev.huuurraydevapp


import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView


// SearchCellAdapter
class SearchCellAdapter (
    private val context: Context,
    private val accountList: List<AccountData>,
    private val onAccountClicked: (AccountData) -> Unit)
    : RecyclerView.Adapter<SearchCellAdapter.SearchCellVIewHolder>() {

    // 変数定義
    private val LOG_TAG = SearchCellAdapter::class.java.simpleName // ログ用にクラス名を取得
    private val inflater = LayoutInflater.from(context)


    // getItemCount
    override fun getItemCount(): Int = accountList.size

    // onCreateViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchCellVIewHolder {
        val view = inflater.inflate(R.layout.search_cell, parent, false)
        val viewHolder = SearchCellVIewHolder(view)

        view.setOnClickListener {
            val position = viewHolder.adapterPosition
            val account = accountList[position]
            onAccountClicked(account)
        }

        return viewHolder
    }

    // onBindViewHolder
    override fun onBindViewHolder(holder: SearchCellVIewHolder, position: Int) {
        val account = accountList[position]
        if (account.peFlg == -1) {
            holder.backImage.setVisibility(View.GONE)
            holder.transparentBack.setVisibility(View.GONE)
            holder.username.setVisibility(View.GONE)
            holder.accountImage.setVisibility(View.GONE)
            holder.dispName.setVisibility(View.GONE)
            holder.wpName.setVisibility(View.GONE)
            holder.margin.setVisibility(View.GONE)
            holder.transparentBackImage.setVisibility(View.VISIBLE)
            holder.progressBar.setVisibility(View.VISIBLE)
        } else {
            holder.backImage.setVisibility(View.VISIBLE)
            holder.transparentBack.setVisibility(View.VISIBLE)
            holder.username.setVisibility(View.VISIBLE)
            holder.accountImage.setVisibility(View.VISIBLE)
            holder.dispName.setVisibility(View.VISIBLE)
            holder.wpName.setVisibility(View.VISIBLE)
            holder.margin.setVisibility(View.VISIBLE)
            holder.transparentBackImage.setVisibility(View.GONE)
            holder.progressBar.setVisibility(View.GONE)

            // ユーザーIDをセット
            holder.username.text = account.username
            // 表示名をセット
            holder.dispName.text = context.getString(R.string.no_disp_name)
            if (account.dispName != "") {
                holder.dispName.text = account.dispName
            }
            // 店舗名をセット
            holder.wpName.text = context.getString(R.string.no_shop_data)
            if (account.wpName != "") {
                val wpName = "@" + account.wpName
                holder.wpName.text = wpName
            }
            // 画像をセット
            if (account.accountImage != "" && account.accountImage != "null") {
                getS3ImageViaGlide(context, "account", account.accountImage, holder.accountImage)
            } else {
                holder.accountImage.setImageDrawable(context.getDrawable(R.drawable.ic_account_circle_white_24dp))
            }
        }
    }

    // VIewHolder
    class SearchCellVIewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // 変数定義
        private val LOG_TAG = SearchCellVIewHolder::class.java.simpleName // ログ用にクラス名を取得
        val backImage = view.findViewById<ImageView>(R.id.back_image)
        val transparentBack = view.findViewById<ImageView>(R.id.transparent_back)
        val accountImage = view.findViewById<ImageView>(R.id.avatar_image)
        val username = view.findViewById<TextView>(R.id.username)
        val dispName = view.findViewById<TextView>(R.id.disp_name)
        val wpName = view.findViewById<TextView>(R.id.wp_name)
        val margin = view.findViewById<ImageView>(R.id.margin)
        val transparentBackImage = view.findViewById<ImageView>(R.id.transparent_back_image)
        val progressBar =  view.findViewById<ProgressBar>(R.id.progress_bar)
    }
}