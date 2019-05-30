package com.yanagimachidev.huuurraydevapp


import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView


// MessageFeedCellAdapter
class MessageFeedCellAdapter (
    private val context: Context,
    private val messages: List<MessageFeedData>,
    private val onAccount: Boolean,
    private val onButtonClicked: (MessageFeedData) -> Unit)
    : RecyclerView.Adapter<MessageFeedCellAdapter.MessageFeedCellVIewHolder>() {

    // 変数定義
    private val LOG_TAG = MessageFeedCellAdapter::class.java.simpleName // ログ用にクラス名を取得
    private val inflater = LayoutInflater.from(context)
    private lateinit var username: String


    // getItemCount
    override fun getItemCount(): Int = messages.size

    // onCreateViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageFeedCellVIewHolder {
        val view = inflater.inflate(R.layout.message_feed_cell, parent, false)
        val viewHolder = MessageFeedCellVIewHolder(view)

        // SharedPreferencesからusernameの取得
        val pref = context.getSharedPreferences("aws_credentials", Context.MODE_PRIVATE)
        username = pref.getString("username", "")

        return viewHolder
    }

    // onBindViewHolder
    override fun onBindViewHolder(holder: MessageFeedCellAdapter.MessageFeedCellVIewHolder, position: Int) {
        val message = messages[position]
        if (message.id == -1 || message.id == -2) {
            holder.backImage.setVisibility(View.GONE)
            holder.transparentBack.setVisibility(View.GONE)
            holder.accountImage.setVisibility(View.GONE)
            holder.dispName.setVisibility(View.GONE)
            holder.wpName.setVisibility(View.GONE)
            holder.fromAccountImage.setVisibility(View.GONE)
            holder.fromName.setVisibility(View.GONE)
            holder.content.setVisibility(View.GONE)
            holder.margin.setVisibility(View.GONE)
            holder.createdAt.setVisibility(View.GONE)
            holder.favoriteCnt.setVisibility(View.GONE)
            holder.niceButton.setVisibility(View.GONE)
            holder.marginFooter.setVisibility(View.GONE)
            holder.transparentBackImage.setVisibility(View.VISIBLE)
            if (message.id == -1) {
                holder.noMorePost.setVisibility(View.GONE)
                holder.progressBar.setVisibility(View.VISIBLE)
            } else {
                holder.noMorePost.setVisibility(View.VISIBLE)
                holder.progressBar.setVisibility(View.GONE)
            }
        } else {
            holder.backImage.setVisibility(View.VISIBLE)
            holder.transparentBack.setVisibility(View.VISIBLE)
            holder.accountImage.setVisibility(View.VISIBLE)
            holder.dispName.setVisibility(View.VISIBLE)
            holder.wpName.setVisibility(View.VISIBLE)
            holder.fromAccountImage.setVisibility(View.VISIBLE)
            holder.fromName.setVisibility(View.VISIBLE)
            holder.content.setVisibility(View.VISIBLE)
            holder.margin.setVisibility(View.VISIBLE)
            holder.createdAt.setVisibility(View.VISIBLE)
            holder.favoriteCnt.setVisibility(View.VISIBLE)
            holder.niceButton.setVisibility(View.VISIBLE)
            holder.marginFooter.setVisibility(View.VISIBLE)
            holder.transparentBackImage.setVisibility(View.GONE)
            holder.progressBar.setVisibility(View.GONE)
            holder.noMorePost.setVisibility(View.GONE)

            // 表示名をセット
            var dispNameText = message.toUsername
            if (message.toDispName != "") {
                dispNameText = message.toDispName
            }
            val conDispName = "To:" + dispNameText
            holder.dispName.text = conDispName

            // 表示名のクリックイベントを登録
            holder.dispName.setOnClickListener {
                message.clickType = "accountImage"
                onButtonClicked(message)
            }

            // 店舗名をセット
            var conWpName = context.getString(R.string.no_shop_data)
            if (message.toWpName != "") {
                conWpName = "@" + message.toWpName
            }
            holder.wpName.text = conWpName

            // FromNameをセット
            var fromNameText = message.fromUsername
            if (message.fromDispName != "") {
                fromNameText = message.fromDispName
            }
            var conFromWpName = "From:" + fromNameText
            if (message.fromWpName != "") {
                conFromWpName += "@" + message.fromWpName
            }
            holder.fromName.text = conFromWpName

            // FromNameのクリックイベントを登録
            holder.fromName.setOnClickListener {
                message.clickType = "fromAccountImage"
                onButtonClicked(message)
            }

            // 本文をセット
            holder.content.text = message.content

            // 投稿日時をセット
            var createdAtStrOrg = message.createdAt.substring(0, 19)
            createdAtStrOrg = createdAtStrOrg.replace("T", " ")
            val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val createdAt = df.parse(createdAtStrOrg)
            val outDf = SimpleDateFormat("yyyy年M月d日H時m分")
            //outDf.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"))
            val postDay = outDf.format(createdAt)
            holder.createdAt.text = postDay

            // アカウント画像をセット
            if (message.toAccountImage != "" && message.toAccountImage != "null") {
                getS3ImageViaGlide(context, "account", message.toAccountImage, holder.accountImage)
            } else {
                holder.accountImage.setImageDrawable(context.getDrawable(R.drawable.ic_account_circle_white_24dp))
            }

            // アカウント画像のクリックイベントを登録
            holder.accountImage.setOnClickListener {
                message.clickType = "accountImage"
                onButtonClicked(message)
            }

            // アカウント画像をセット
            if (message.fromAccountImage != "" && message.fromAccountImage != "null") {
                getS3ImageViaGlide(context, "account", message.fromAccountImage, holder.fromAccountImage)
            } else {
                holder.fromAccountImage.setImageDrawable(context.getDrawable(R.drawable.ic_account_circle_white_24dp))
            }

            // アカウント画像のクリックイベントを登録
            holder.fromAccountImage.setOnClickListener {
                message.clickType = "fromAccountImage"
                onButtonClicked(message)
            }

            // いいね！の件数のセット
            holder.favoriteCnt.text = String.format("%,d " + context.getString(R.string.nice_cnt), message.favoriteCnt)

            // いいね！数のクリックイベントを登録
            holder.favoriteCnt.setOnClickListener {
                message.clickType = "favoriteCnt"
                onButtonClicked(message)
            }

            // いいね！のクリックイベントを登録
            val red = "#F44336"
            val black = "#000000"
            if (onAccount) {
                if (message.favorite) {
                    holder.niceButton.setColorFilter(Color.parseColor(red), PorterDuff.Mode.SRC_IN)
                } else {
                    holder.niceButton.setColorFilter(Color.parseColor(black), PorterDuff.Mode.SRC_IN)
                }
                holder.niceButton.setOnClickListener {
                    it.notPressTwice()
                    message.clickType = "niceButton"
                    message.position = position
                    onButtonClicked(message)
                }
            }
        }
    }

    // VIewHolder
    class MessageFeedCellVIewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // 変数定義
        private val LOG_TAG = MessageFeedCellVIewHolder::class.java.simpleName // ログ用にクラス名を取得
        val backImage = view.findViewById<ImageView>(R.id.back_image)
        val transparentBack = view.findViewById<ImageView>(R.id.transparent_back)
        val accountImage = view.findViewById<ImageView>(R.id.avatar_image)
        val dispName = view.findViewById<TextView>(R.id.disp_name)
        val wpName = view.findViewById<TextView>(R.id.wp_name)
        val fromAccountImage = view.findViewById<ImageView>(R.id.from_avatar_image)
        val fromName = view.findViewById<TextView>(R.id.from_name)
        val content = view.findViewById<TextView>(R.id.content)
        val margin = view.findViewById<ImageView>(R.id.margin)
        val createdAt = view.findViewById<TextView>(R.id.date_time)
        val favoriteCnt = view.findViewById<TextView>(R.id.favorite_cnt)
        val niceButton = view.findViewById<ImageView>(R.id.nice_button)
        val marginFooter = view.findViewById<ImageView>(R.id.margin_footer)
        val transparentBackImage = view.findViewById<ImageView>(R.id.transparent_back_image)
        val progressBar =  view.findViewById<ProgressBar>(R.id.progress_bar)
        val noMorePost = view.findViewById<TextView>(R.id.no_more_post)
    }
}