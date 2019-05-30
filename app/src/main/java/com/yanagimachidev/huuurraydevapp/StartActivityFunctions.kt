package com.yanagimachidev.huuurraydevapp


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.support.v4.content.ContextCompat.startActivity


// startMySignUpActivity
fun startMySignUpActivity(context: Context) {
    val intent = Intent(context, MySignUpActivity::class.java)
    startActivity(context, intent, null)
}

// startMySignUpActivity
fun startEditAccountActivity(activity: Activity) {
    val intent = Intent(activity, EditAccountActivity::class.java)
    startActivityForResult(activity, intent, 17703, null)
}

// startVerificationActivity
fun startVerificationActivity(context: Context) {
    val intent = Intent(context, MySignUpConfirmActivity::class.java)
    startActivity(context, intent, null)
}

// startAccountActivity
fun startAccountActivity(context: Context, username: String, checkInFlg: Boolean) {
    val intent = Intent(context, AccountActivity::class.java)
    intent.putExtra("username", username)
    intent.putExtra("checkInFlg", checkInFlg)
    startActivity(context, intent, null)
}

// startEditAccountImageActivity
fun startEditAccountImageActivity(activity: Activity, imageType: String) {
    val intent = Intent(activity, EditAccountImageActivity::class.java)
    intent.putExtra("imageType", imageType)
    startActivityForResult(activity, intent, 17704, null)
}

// 画像取得のアクティビティを起動
fun startImageGetActivity(activity: Activity) {
    val intentForImageGet = Intent(Intent.ACTION_OPEN_DOCUMENT)
    intentForImageGet.addCategory(Intent.CATEGORY_OPENABLE)
    intentForImageGet.type = "image/*"
    startActivityForResult(activity, intentForImageGet, 17705, null)
}

// startEditShopActivity
fun startEditShopActivity(activity: Activity, ver: String) {
    val intent = Intent(activity, EditShopActivity::class.java)
    intent.putExtra("ver", ver)
    startActivityForResult(activity, intent, 17710, null)
}

// startSendPostActivity
fun startSendPostActivity(activity: Activity, fromType: String) {
    val intent = Intent(activity, SendPostActivity::class.java)
    intent.putExtra("fromType", fromType)
    startActivityForResult(activity, intent, 17701, null)
}

// startEditPostActivity
fun startEditPostActivity(activity: Activity, id: Int, username: String, content: String, fromType: String) {
    val intent = Intent(activity, SendPostActivity::class.java)
    intent.putExtra("id", id)
    intent.putExtra("username", username)
    intent.putExtra("content", content)
    intent.putExtra("fromType", fromType)
    startActivityForResult(activity, intent, 17701, null)
}

// startFollowListActivity
fun startFollowListActivity(context: Context, username: String, flg: Boolean) {
    val intent = Intent(context, FollowListActivity::class.java)
    intent.putExtra("username", username)
    intent.putExtra("flg", flg)
    startActivity(context, intent, null)
}

// startNiceListActivity
fun startNiceListActivity(context: Context, id: Int) {
    val intent = Intent(context, NiceListActivity::class.java)
    intent.putExtra("id", id)
    startActivity(context, intent, null)
}


// startMessageNiceListActivity
fun startMessageNiceListActivity(context: Context, id: Int) {
    val intent = Intent(context, MessageNiceListActivity::class.java)
    intent.putExtra("id", id)
    startActivity(context, intent, null)
}


// startSendMessageActivity
fun startSendMessageActivity(activity: Activity, toUsername: String) {
    val intent = Intent(activity, SendMessageActivity::class.java)
    intent.putExtra("toUsername", toUsername)
    startActivityForResult(activity, intent, 17702, null)
}