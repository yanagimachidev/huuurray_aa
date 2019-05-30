package com.yanagimachidev.huuurraydevapp


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Loader
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.provider.MediaStore
import android.graphics.Bitmap
import android.graphics.Matrix
import android.view.View
import com.avito.android.krop.KropView
import android.widget.Button
import android.widget.TextView
import java.io.ByteArrayOutputStream


// EditAccountImageActivity
class EditAccountImageActivity : AppCompatActivity(),
    AccountImageUpsertLoaderInterface {

    // 変数定義
    private val LOG_TAG = EditAccountImageActivity::class.java.simpleName // ログ用にクラス名を取得
    private lateinit var username: String
    private var imageType: String? = null
    private var bytes: ByteArray? = null


    // onCreate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // StatusBarを表示、透過する設定
        findViewById<View>(android.R.id.content).systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        // 画像タイプを取得
        imageType = intent.getStringExtra("imageType")

        // 画像タイプによってlayoutを分ける
        if (imageType == "back") {
            setContentView(R.layout.activity_edit_back_image)
        } else {
            setContentView(R.layout.activity_edit_account_image)
            // タイトルをtypeによって変更
            val appBarEditText = findViewById<TextView>(R.id.app_bar_text)
            appBarEditText.text = getString(R.string.title_edit_account_image)
            if (imageType != "account") {
                appBarEditText.text = getString(R.string.title_edit_shop_image)
            }
        }

        // 画像切り取りのViewをセット
        val kropView = findViewById<KropView>(R.id.krop_view)

        // 画像取得のアクティビティを起動
        startImageGetActivity(this)

        // ユーザー名をSharedPreferencesから取得
        val pref = getSharedPreferences("aws_credentials", Context.MODE_PRIVATE)
        username = pref.getString("username", "")

        // 切り取りボタンの設定
        val crop = findViewById<Button>(R.id.crop)
        crop.setOnClickListener {
            it.notPressTwice()
            val bitmap = kropView.getCroppedBitmap()
            val byteArrOutputStream = ByteArrayOutputStream()
            if (bitmap != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrOutputStream)
                bytes = byteArrOutputStream.toByteArray()
                startAccountImageUpsertLoader()
                byteArrOutputStream.close()
            }
        }
    }

    // onActivityResult
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // 画像選択画面からの結果を処理
        if (requestCode == 17705 && resultCode == Activity.RESULT_OK && data != null) {
            val uri = data.data
            try {
                val bitmapOrg = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                val varMat = Matrix()
                val scale =  500F / bitmapOrg.width
                Log.i(LOG_TAG, "scale:" + scale.toString())
                varMat.postScale(scale, scale)
                val bitmap = Bitmap.createBitmap(
                    bitmapOrg, 0, 0,
                    bitmapOrg.getWidth(),
                    bitmapOrg.getHeight(),
                    varMat, true
                )
                val kropView = findViewById<KropView>(R.id.krop_view)
                kropView.setBitmap(bitmap)
            } catch (e: Exception) {
                // サインアップ中のダイアログを作成
                showAlertDialog(this,
                    getString(R.string.error), getString(R.string.Image_get_error))
                finish()
            }
        }else{
            finish()
        }
    }


    // startAccountImageUpsertLoader
    private fun startAccountImageUpsertLoader() {
        val bundle = Bundle()
        bundle.putString("username", username)
        bundle.putString("imageType", imageType)
        bundle.putByteArray("image", bytes)
        supportLoaderManager.restartLoader(13302, bundle,
            AccountImageUpsertLoaderCallbacks(this, this))
    }

    // AccountImageUpsertLoaderOnLoadFinished
    override fun AccountImageUpsertLoaderOnLoadFinished(data: AccountImage?) {
        var result: Boolean = false
        if (data != null) {
            result = true
        }
        val intent = Intent()
        intent.putExtra("SaveAccountImage", result)
        setResult(RESULT_OK, intent)
        finish()
    }
}
