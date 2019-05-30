package com.yanagimachidev.huuurraydevapp


import android.content.Context
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


// MessageCellAdapter
class MessageCellAdapter (
    private val context: Context,
    private val messages: List<MessageDispData>,
    private val pagePosition: Int,
    private val onButtonClicked: (MessageDispData) -> Unit)
    : RecyclerView.Adapter<MessageCellAdapter.MessageCellVIewHolder>() {

    // 変数定義
    private val LOG_TAG = MessageCellAdapter::class.java.simpleName // ログ用にクラス名を取得
    private val inflater = LayoutInflater.from(context)
    private lateinit var username: String


    // getItemCount
    override fun getItemCount(): Int = messages.size

    // onCreateViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageCellVIewHolder {
        val view = inflater.inflate(R.layout.message_cell, parent, false)
        val viewHolder = MessageCellVIewHolder(view)

        // SharedPreferencesからusernameの取得
        val pref = context.getSharedPreferences("aws_credentials", Context.MODE_PRIVATE)
        username = pref.getString("username", "")

        return viewHolder
    }

    // onBindViewHolder
    override fun onBindViewHolder(holder: MessageCellAdapter.MessageCellVIewHolder, position: Int) {
        val message = messages[position]
        if (message.id == -1 || message.id == -2) {
            holder.backImage.setVisibility(View.GONE)
            holder.transparentBack.setVisibility(View.GONE)
            holder.margin.setVisibility(View.GONE)
            holder.marginFooter.setVisibility(View.GONE)
            holder.accountImage.setVisibility(View.GONE)
            holder.dispName.setVisibility(View.GONE)
            holder.wpName.setVisibility(View.GONE)
            holder.content.setVisibility(View.GONE)
            holder.createdAt.setVisibility(View.GONE)
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
            holder.margin.setVisibility(View.VISIBLE)
            holder.marginFooter.setVisibility(View.VISIBLE)
            holder.accountImage.setVisibility(View.VISIBLE)
            holder.dispName.setVisibility(View.VISIBLE)
            holder.wpName.setVisibility(View.VISIBLE)
            holder.content.setVisibility(View.VISIBLE)
            holder.createdAt.setVisibility(View.VISIBLE)
            holder.transparentBackImage.setVisibility(View.GONE)
            holder.progressBar.setVisibility(View.GONE)
            holder.noMorePost.setVisibility(View.GONE)

            // 表示名をセット
            var prefix = "From: "
            if (pagePosition == 1) {
                prefix = "To: "
            }
            var dispNameText = message.toUsername
            if (username == message.toUsername) {
                dispNameText = message.fromUsername
            }
            if (message.dispName != "") {
                dispNameText = message.dispName
            }
            dispNameText = prefix + dispNameText
            holder.dispName.text = dispNameText

            // 店舗名をセット
            var conWpName = context.getString(R.string.no_shop_data)
            if (message.wpName != "") {
                conWpName = "@" + message.wpName
            }
            holder.wpName.text = conWpName

            // 本文をセット
            holder.content.text = message.content

            // 投稿日時をセット
            var createdAtStrOrg = message.createdAt!!.substring(0, 19)
            createdAtStrOrg = createdAtStrOrg.replace("T", " ")
            val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val createdAt = df.parse(createdAtStrOrg)
            val outDf = SimpleDateFormat("yyyy年M月d日H時m分")
            outDf.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"))
            val postDay = outDf.format(createdAt)
            holder.createdAt.text = postDay

            // アカウント画像をセット
            if (message.accountImage != null && message.accountImage != "" && message.accountImage != "null") {
                getS3ImageViaGlide(context, "account", message.accountImage!!, holder.accountImage)
            } else {
                holder.accountImage.setImageDrawable(context.getDrawable(R.drawable.ic_account_circle_white_24dp))
            }

            // アカウント画像のクリックイベントを登録
            holder.accountImage.setOnClickListener {
                message.clickType = "accountImage"
                onButtonClicked(message)
            }
        }
    }

    // VIewHolder
    class MessageCellVIewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // 変数定義
        private val LOG_TAG = MessageCellVIewHolder::class.java.simpleName // ログ用にクラス名を取得
        val backImage = view.findViewById<ImageView>(R.id.back_image)
        val transparentBack = view.findViewById<ImageView>(R.id.transparent_back)
        val margin = view.findViewById<ImageView>(R.id.margin)
        val marginFooter = view.findViewById<ImageView>(R.id.margin_footer)
        val accountImage = view.findViewById<ImageView>(R.id.avatar_image)
        val dispName = view.findViewById<TextView>(R.id.disp_name)
        val wpName = view.findViewById<TextView>(R.id.wp_name)
        val content = view.findViewById<TextView>(R.id.content)
        val createdAt = view.findViewById<TextView>(R.id.date_time)
        val transparentBackImage = view.findViewById<ImageView>(R.id.transparent_back_image)
        val progressBar =  view.findViewById<ProgressBar>(R.id.progress_bar)
        val noMorePost = view.findViewById<TextView>(R.id.no_more_post)
    }
}