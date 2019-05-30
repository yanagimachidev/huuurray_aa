package com.yanagimachidev.huuurraydevapp


import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.widget.*


// EditAccountActivity
class EditAccountActivity : AppCompatActivity(),
    HrUserUpsertLoaderInterface {

    // 変数定義
    private val LOG_TAG = EditAccountActivity::class.java.simpleName // ログ用にクラス名を取得
    private lateinit var dispNameEditText: TextView
    private lateinit var profileEditText: TextView
    private lateinit var sexEditText: Spinner
    private lateinit var birthdayEditText: TextView


    // onCreate
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_account)

        // StatusBarを表示、透過する設定
        findViewById<View>(android.R.id.content).systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        // ユーザーデータの取得
        val pref = getSharedPreferences("aws_credentials", Context.MODE_PRIVATE)
        val username = pref.getString("username", "")
        val data = queryHrUser(this, username)

        // 表示名のセット
        dispNameEditText = findViewById<EditText>(R.id.disp_name)
        if (!(data.isEmpty() || data["disp_name"] == "")) {
            dispNameEditText.text = data["disp_name"] as String
        }

        // プロフィールのセット
        profileEditText = findViewById<EditText>(R.id.profile)
        if (!(data.isEmpty() || data["profile"] == "")) {
            profileEditText.text = data["profile"] as String
        }

        // 性別のセット
        sexEditText = findViewById<Spinner>(R.id.sex)
        if (!(data.isEmpty() || data["sex"] == "")) {
            val items = resources.getStringArray(R.array.sex)
            for (i in 0 .. items.size - 1) {
                sexEditText.setSelection(i)
                if (i.toString() == data["sex"]) {
                    break
                }
            }
        }

        // 誕生日のセット
        birthdayEditText = findViewById<EditText>(R.id.birthday)
        if (!(data.isEmpty() || data["birthday"] == "")) {
            var birthdaySlaText = data["birthday"] as String
            birthdaySlaText = birthdaySlaText.replace("-", "/")
            birthdayEditText.text = birthdaySlaText
        }
        // 日付情報の初期設定
        val calendar = Calendar.getInstance()
        var year = calendar.get(Calendar.YEAR) - 20 // 年
        var monthOfYear = calendar.get(Calendar.MONTH) // 月
        var dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH) // 日
        // 日付設定時のリスナ作成
        val DateSetListener = DatePickerDialog.OnDateSetListener { datePicker, dpYear, dpMonthOfYear, dpDayOfMonth ->
            birthdayEditText.setText(String.format("%d/%02d/%02d", dpYear, dpMonthOfYear+1, dpDayOfMonth))
            year = dpYear
            monthOfYear = dpMonthOfYear
            dayOfMonth = dpDayOfMonth
        }
        // 日付設定ダイアログの作成・リスナの登録
        val datePickerDialog = DatePickerDialog(
            this,
            R.style.MyDatePickerStyle,
            DateSetListener,
            year,
            monthOfYear,
            dayOfMonth
        )
        // 誕生日項目へのクリックリスナーを登録
        birthdayEditText.setOnClickListener {
            datePickerDialog.show()
            datePickerDialog.datePicker.touchables[0].performClick()
        }

        // 保存ボタン
        val save = findViewById<Button>(R.id.save)
        save.setOnClickListener {
            it.notPressTwice()
            // エラーフラグ
            var inValid = true

            // 表示名の入力値を取得
            val dispNameText = dispNameEditText.text.toString()

            //  表示名の長さエラー
            if (dispNameText.length > 30 && inValid) {
                dispNameEditText.error = getString(R.string.too_long_disp_name_error) +
                        dispNameText.length + getString(R.string.too_long_error_end)
                dispNameEditText.requestFocus()
                inValid = false
            }

            // 性別の入力値を取得
            val sexText = sexEditText.selectedItemPosition.toString()

            // 誕生日の入力値を取得
            val birthdayText = birthdayEditText.text.toString().replace("/", "-")

            // プロフィールの入力値を取得
            val profileText = profileEditText.text.toString()

            //  プロフィールの長さエラー
            if (profileText.length > 500 && inValid) {
                profileEditText.error = getString(R.string.too_long_profile_error) +
                        profileText.length + getString(R.string.too_long_error_end)
                profileEditText.requestFocus()
                inValid = false
            }

            // エラーがなければアカウント情報を更新するローダーを起動
            if (inValid) {
                startHrUserUpsertLoader(username, dispNameText, sexText, birthdayText, profileText)
            }
        }
    }


    // startHrUserUpsertLoader
    private fun startHrUserUpsertLoader(username: String, dispNameText: String, sexText: String,
                                        birthdayText: String, profileText: String) {
        // 引数に値を渡してローダーを起動
        val bundle = Bundle()
        bundle.putString("username", username)
        bundle.putString("dispName", dispNameText)
        bundle.putString("sex", sexText)
        bundle.putString("birthday", birthdayText)
        bundle.putString("profile", profileText)
        supportLoaderManager.restartLoader(13301, bundle,
            HrUserUpsertLoaderCallbacks(this, this))
    }

    // HrUserUpsertLoaderOnLoadFinished
    override fun HrUserUpsertLoaderOnLoadFinished(data: HrUser?) {
        var result: Boolean = false
        if (data != null) {
            result = true
        }
        val intent = Intent()
        intent.putExtra("SaveUserData", result)
        setResult(RESULT_OK, intent)
        finish()
    }
}
