package com.yanagimachidev.huuurraydevapp


import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView


// RankingCellAdapter
class RankingCellAdapter (
    private val context: Context,
    private val ranking: List<RankData>,
    private val onRankClicked: (RankData) -> Unit)
    : RecyclerView.Adapter<RankingCellAdapter.RankingCellVIewHolder>() {

    // 変数定義
    private val LOG_TAG = RankingCellAdapter::class.java.simpleName // ログ用にクラス名を取得
    private val inflater = LayoutInflater.from(context)


    // getItemCount
    override fun getItemCount(): Int = ranking.size

    // onCreateViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingCellVIewHolder {
        val view = inflater.inflate(R.layout.ranking_cell, parent, false)
        val viewHolder = RankingCellVIewHolder(view)

        view.setOnClickListener {
            val position = viewHolder.adapterPosition
            val rank = ranking[position]
            if (rank.point != -1 && rank.point != -2) {
                onRankClicked(rank)
            }
        }

        return viewHolder
    }

    // onBindViewHolder
    override fun onBindViewHolder(holder: RankingCellVIewHolder, position: Int) {
        val rank = ranking[position]
        if (rank.point == -1 || rank.point == -2) {
            holder.backImage.setVisibility(View.GONE)
            holder.transparentBack.setVisibility(View.GONE)
            holder.accountImage.setVisibility(View.GONE)
            holder.userRank.setVisibility(View.GONE)
            holder.point.setVisibility(View.GONE)
            holder.dispName.setVisibility(View.GONE)
            holder.wpName.setVisibility(View.GONE)
            holder.profile.setVisibility(View.GONE)
            holder.margin.setVisibility(View.GONE)
            holder.transparentBackImage.setVisibility(View.VISIBLE)
            if (rank.point == -1) {
                holder.noMoreRank.setVisibility(View.GONE)
                holder.progressBar.setVisibility(View.VISIBLE)
            } else {
                holder.noMoreRank.setVisibility(View.VISIBLE)
                holder.progressBar.setVisibility(View.GONE)
            }
        } else {
            holder.backImage.setVisibility(View.VISIBLE)
            holder.transparentBack.setVisibility(View.VISIBLE)
            holder.accountImage.setVisibility(View.VISIBLE)
            holder.userRank.setVisibility(View.VISIBLE)
            holder.point.setVisibility(View.VISIBLE)
            holder.dispName.setVisibility(View.VISIBLE)
            holder.wpName.setVisibility(View.VISIBLE)
            holder.profile.setVisibility(View.VISIBLE)
            holder.margin.setVisibility(View.VISIBLE)
            holder.transparentBackImage.setVisibility(View.GONE)
            holder.progressBar.setVisibility(View.GONE)
            holder.noMoreRank.setVisibility(View.GONE)

            // 表示名をセット
            holder.dispName.text = rank.username
            if (rank.dispName != "") {
                holder.dispName.text = rank.dispName
            }
            // 店舗名をセット
            holder.wpName.text = context.getString(R.string.no_shop_data)
            if (rank.wpName != "") {
                val conWpName = "@" + rank.wpName
                holder.wpName.text = conWpName
            }
            // プロフィールをセット
            holder.profile.text = ""
            if (rank.profile != "") {
                var conProfile = rank.profile
                conProfile = conProfile.replace("\n", " ")
                conProfile = conProfile.replace("\r", " ")
                if (conProfile.length > 50) {
                    conProfile = conProfile.substring(0, 49) + "…"
                }
                holder.profile.text = conProfile
            }
            // ランクをセット
            val pos = position + 1
            val userRank = String.format("%,d", pos) + "位"
            holder.userRank.text = userRank
            // ポイントをセット
            val point = String.format("%,d", rank.point) + " SP"
            holder.point.text = point
            // 画像をセット
            if (rank.accountImage != "" && rank.accountImage != "null") {
                getS3ImageViaGlide(context, "account", rank.accountImage, holder.accountImage)
            } else {
                holder.accountImage.setImageDrawable(context.getDrawable(R.drawable.ic_account_circle_white_24dp))
            }
        }
    }

    // VIewHolder
    class RankingCellVIewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // 変数定義
        private val LOG_TAG = RankingCellVIewHolder::class.java.simpleName // ログ用にクラス名を取得
        val backImage = view.findViewById<ImageView>(R.id.back_image)
        val transparentBack = view.findViewById<ImageView>(R.id.transparent_back)
        val margin = view.findViewById<ImageView>(R.id.margin)
        val userRank = view.findViewById<TextView>(R.id.rank)
        val accountImage = view.findViewById<ImageView>(R.id.avatar_image)
        val dispName = view.findViewById<TextView>(R.id.disp_name)
        val wpName = view.findViewById<TextView>(R.id.wp_name)
        val profile = view.findViewById<TextView>(R.id.profile)
        val point = view.findViewById<TextView>(R.id.point)
        val transparentBackImage = view.findViewById<ImageView>(R.id.transparent_back_image)
        val progressBar =  view.findViewById<ProgressBar>(R.id.progress_bar)
        val noMoreRank = view.findViewById<TextView>(R.id.no_more_rank)
    }
}