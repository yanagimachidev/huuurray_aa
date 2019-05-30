package com.yanagimachidev.huuurraydevapp


import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.util.Log
import android.widget.ProgressBar


// PostCellAdapter
class PostCellAdapter (
    private val context: Context,
    private val posts: List<PostData>,
    private val onAccount: Boolean,
    private val onButtonClicked: (PostData) -> Unit)
    : RecyclerView.Adapter<PostCellAdapter.PostCellVIewHolder>() {

    // 変数定義
    private val LOG_TAG = PostCellAdapter::class.java.simpleName // ログ用にクラス名を取得
    private val inflater = LayoutInflater.from(context)
    private var username = ""


    // getItemCount
    override fun getItemCount(): Int = posts.size

    // onCreateViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostCellVIewHolder {
        val view = inflater.inflate(R.layout.post_cell, parent, false)
        val viewHolder = PostCellVIewHolder(view)

        // SharedPreferencesからusernameの取得
        val pref = context.getSharedPreferences("aws_credentials", Context.MODE_PRIVATE)
        if (pref.contains("username") && pref.contains("password") && pref.contains("confirm")) {
            username = pref.getString("username", "")
        }

        return viewHolder
    }

    // onBindViewHolder
    override fun onBindViewHolder(holder: PostCellVIewHolder, position: Int) {
        val post = posts[position]
        if (post.id == -1 || post.id == -2 || post.id == -3) {
            holder.backImage.setVisibility(View.GONE)
            holder.transparentBack.setVisibility(View.GONE)
            holder.accountImage.setVisibility(View.GONE)
            holder.dispName.setVisibility(View.GONE)
            holder.wpName.setVisibility(View.GONE)
            holder.edit.setVisibility(View.GONE)
            holder.content.setVisibility(View.GONE)
            holder.margin.setVisibility(View.GONE)
            holder.createdAt.setVisibility(View.GONE)
            holder.favoriteCnt.setVisibility(View.GONE)
            holder.niceButton.setVisibility(View.GONE)
            holder.marginFooter.setVisibility(View.GONE)
            holder.transparentBackImage.setVisibility(View.VISIBLE)
            if (post.id == -1) {
                holder.noMorePost.setVisibility(View.GONE)
                holder.noMorePostNoPad.setVisibility(View.GONE)
                holder.progressBar.setVisibility(View.VISIBLE)
            } else if (post.id == -2) {
                holder.noMorePost.setVisibility(View.VISIBLE)
                holder.noMorePostNoPad.setVisibility(View.GONE)
                holder.progressBar.setVisibility(View.GONE)
            } else {
                holder.noMorePost.setVisibility(View.GONE)
                holder.noMorePostNoPad.setVisibility(View.VISIBLE)
                holder.progressBar.setVisibility(View.GONE)
            }
        } else{
            holder.backImage.setVisibility(View.VISIBLE)
            holder.transparentBack.setVisibility(View.VISIBLE)
            holder.accountImage.setVisibility(View.VISIBLE)
            holder.dispName.setVisibility(View.VISIBLE)
            holder.wpName.setVisibility(View.VISIBLE)
            holder.edit.setVisibility(View.GONE)
            holder.content.setVisibility(View.VISIBLE)
            holder.margin.setVisibility(View.VISIBLE)
            holder.createdAt.setVisibility(View.VISIBLE)
            holder.favoriteCnt.setVisibility(View.VISIBLE)
            holder.niceButton.setVisibility(View.VISIBLE)
            holder.marginFooter.setVisibility(View.VISIBLE)
            holder.transparentBackImage.setVisibility(View.GONE)
            holder.progressBar.setVisibility(View.GONE)
            holder.noMorePost.setVisibility(View.GONE)
            holder.noMorePostNoPad.setVisibility(View.GONE)


            // 編集ボタンの表示
            if (post.username == username) {
                holder.edit.setVisibility(View.VISIBLE)
            }

            // 表示名をセット
            holder.dispName.text = post.username
            if (post.dispName != "") {
                holder.dispName.text = post.dispName
            }

            // 店舗名をセット
            var conWpName = context.getString(R.string.no_shop_data)
            if (post.wpName != "") {
                conWpName = "@" + post.wpName
            }
            holder.wpName.text = conWpName

            // 本文をセット
            holder.content.text = post.content

            // 投稿日時をセット
            var createdAtStrOrg = post.createdAt.substring(0, 19)
            createdAtStrOrg = createdAtStrOrg.replace("T", " ")
            val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val createdAt = df.parse(createdAtStrOrg)
            val outDf = SimpleDateFormat("yyyy年M月d日H時m分")
            outDf.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"))
            val postDay = outDf.format(createdAt)
            holder.createdAt.text = postDay

            // アカウント画像をセット
            if (post.accountImage != "" && post.accountImage != "null") {
                getS3ImageViaGlide(context, "account", post.accountImage, holder.accountImage)
            } else {
                holder.accountImage.setImageDrawable(context.getDrawable(R.drawable.ic_account_circle_white_24dp))
            }

            // アカウント画像のクリックイベントを登録
            holder.accountImage.setOnClickListener {
                post.clickType = "accountImage"
                onButtonClicked(post)
            }

            // 編集のクリックイベントを登録
            holder.edit.setOnClickListener {
                post.clickType = "edit"
                onButtonClicked(post)
            }

            // いいね！の件数のセット
            holder.favoriteCnt.text = String.format("%,d " + context.getString(R.string.nice_cnt), post.favoriteCnt)

            // いいね！数のクリックイベントを登録
            holder.favoriteCnt.setOnClickListener {
                post.clickType = "favoriteCnt"
                onButtonClicked(post)
            }

            // いいね！のクリックイベントを登録
            val red = "#F44336"
            val black = "#000000"
            if (onAccount) {
                if (post.favorite) {
                    holder.niceButton.setColorFilter(Color.parseColor(red), PorterDuff.Mode.SRC_IN)
                } else {
                    holder.niceButton.setColorFilter(Color.parseColor(black), PorterDuff.Mode.SRC_IN)
                }
                holder.niceButton.setOnClickListener {
                    it.notPressTwice()
                    post.clickType = "niceButton"
                    post.position = position
                    onButtonClicked(post)
                }
            }
        }
    }

    // VIewHolder
    class PostCellVIewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // 変数定義
        private val LOG_TAG = PostCellVIewHolder::class.java.simpleName // ログ用にクラス名を取得
        val backImage = view.findViewById<ImageView>(R.id.back_image)
        val transparentBack = view.findViewById<ImageView>(R.id.transparent_back)
        val margin = view.findViewById<ImageView>(R.id.margin)
        val marginFooter = view.findViewById<ImageView>(R.id.margin_footer)
        val accountImage = view.findViewById<ImageView>(R.id.avatar_image)
        val dispName = view.findViewById<TextView>(R.id.disp_name)
        val wpName = view.findViewById<TextView>(R.id.wp_name)
        val edit = view.findViewById<TextView>(R.id.edit)
        val content = view.findViewById<TextView>(R.id.content)
        val createdAt = view.findViewById<TextView>(R.id.date_time)
        val favoriteCnt = view.findViewById<TextView>(R.id.favorite_cnt)
        val niceButton = view.findViewById<ImageView>(R.id.nice_button)
        val transparentBackImage = view.findViewById<ImageView>(R.id.transparent_back_image)
        val progressBar =  view.findViewById<ProgressBar>(R.id.progress_bar)
        val noMorePost = view.findViewById<TextView>(R.id.no_more_post)
        val noMorePostNoPad = view.findViewById<TextView>(R.id.no_more_post_nopad)
    }
}