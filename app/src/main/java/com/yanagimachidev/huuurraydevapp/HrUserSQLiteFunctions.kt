package com.yanagimachidev.huuurraydevapp

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import java.lang.Exception


// 単一アカウント情報の取得
fun queryHrUser(context: Context, username: String?) : Map<String, Any?> {
    val data = mutableMapOf<String, Any?>()
    val db = HuuurrayDBOpenHelper(context).readableDatabase
    val cursor = db.query(
        "hr_user",
        null,
        "username = ?",
        arrayOf(username),
        null,
        null,
        null,
        "1"
    )

    val columnNameListString = mutableListOf<String>(
        "username",
        "disp_name",
        "sex",
        "birthday",
        "profile",
        "account_image",
        "back_image",
        "last_check_in",
        "favorite_user",
        "wp_on",
        "wp1_name",
        "wp1_category",
        "wp1_url",
        "wp1_image"
    )

    val columnNameListInt = mutableListOf<String>(
        "st_sp",
        "sp_sp",
        "send_sp_to"
    )

    val columnNameListDouble = mutableListOf<String>(
        "wp1_lat",
        "wp1_lng"
    )

    cursor.use { c ->
        while (c.moveToNext()) {
            columnNameListString.forEach {
                if (cursor.getString(cursor.getColumnIndex(it)) != null) {
                    data[it] = cursor.getString(cursor.getColumnIndex(it))
                    if (data[it] == "null") {
                        data[it] = ""
                    }
                } else {
                    data[it] = ""
                }
            }
            columnNameListInt.forEach {
                data[it] = cursor.getInt(cursor.getColumnIndex(it))
            }
            columnNameListDouble.forEach {
                data[it] = cursor.getDouble(cursor.getColumnIndex(it))
            }
        }
    }
    cursor.close()
    db.close()
    return data
}


// 全店舗情報の取得
fun queryAllHrUser(context: Context) : List<Map<String, Any?>> {
    val data = mutableListOf<Map<String, Any?>>()
    val db = HuuurrayDBOpenHelper(context).readableDatabase
    val cursor = db.query(
        "hr_user",
        null,
        "wp_on <> '0' and delete_flg = '0'",
        null,
        null,
        null,
        "wp1_lat_sort ASC, wp1_lng_sort ASC, wp1_latlng_sort ASC"
    )


    val columnNameListString = mutableListOf<String>(
        "username",
        "disp_name",
        "account_image",
        "wp1_image",
        "wp_on",
        "wp1_name",
        "wp1_category",
        "wp1_url"
    )

    val columnNameListInt = mutableListOf<String>(

    )

    val columnNameListDouble = mutableListOf<String>(
        "wp1_lat",
        "wp1_lng",
        "wp1_lat_sort",
        "wp1_lng_sort",
        "wp1_latlng_sort"
    )

    cursor.use { c ->
        while (c.moveToNext()) {
            val record = mutableMapOf<String, Any?>()
            columnNameListString.forEach {
                if (cursor.getString(cursor.getColumnIndex(it)) != null) {
                    record[it] = cursor.getString(cursor.getColumnIndex(it))
                    if (record[it] == "null") {
                        record[it] = ""
                    }
                } else {
                    record[it] = ""
                }
            }
            columnNameListInt.forEach {
                record[it] = cursor.getInt(cursor.getColumnIndex(it))
            }
            columnNameListDouble.forEach {
                record[it] = cursor.getDouble(cursor.getColumnIndex(it))
            }
            data.add(record)
        }
    }
    cursor.close()
    db.close()
    return data
}


// ユーザーデータの作成/更新
fun upsertHrUser(
    context: Context,
    dataStirng: Map<String, String>,
    dataInt: Map<String, Int?>,
    dataDouble: Map<String, Double?>) : Boolean {
    // レコードの存在確認
    val oldData = queryHrUser(context, dataStirng["username"])
    // 存在しなければ作成
    val db = HuuurrayDBOpenHelper(context).writableDatabase
    if (oldData.isEmpty()) {
        try {
            db.use {
                val record = ContentValues().apply {
                    dataStirng.forEach { (k, v) ->
                        put(k, v)
                    }
                    dataDouble.forEach { (k, v) ->
                        put(k, v)
                    }
                    dataInt.forEach{ (k, v) ->
                        put(k, v)
                    }
                }
                it.insert("hr_user", null, record)
            }
        } catch (e: Exception) {
            return false
        }
        // 存在した場合は更新
    } else {
        try {
            db.use {
                val record = ContentValues().apply {
                    dataStirng.forEach { (k, v) ->
                        put(k, v)
                    }
                    dataDouble.forEach { (k, v) ->
                        put(k, v)
                    }
                    dataInt.forEach{ (k, v) ->
                        put(k, v)
                    }
                }
                it.update("hr_user", record, "username = ?", arrayOf(dataStirng["username"]))
            }
        } catch (e: Exception) {
            return false
        }
    }
    return true
}



// 検索結果を取得
fun SearchHrUser(context: Context, page: Int, queryString: String) : List<AccountData> {
    val data = mutableListOf<AccountData>()
    val db = HuuurrayDBOpenHelper(context).readableDatabase
    val columnNameListString = arrayOf(
        "username",
        "disp_name",
        "account_image",
        "wp_on",
        "wp1_name"
    )
    val limit = 10
    val offset = page * limit
    val limitOffset = String.format("%d, %d", offset, limit);
    val likeQueryString = "%" + queryString + "%"
    val cursor = db.query(
        "hr_user",
        columnNameListString,
        "wp_on <> '0' and delete_flg = '0' and" +
        " (username LIKE ? or disp_name LIKE ? or wp1_name LIKE ? )",
        arrayOf(likeQueryString, likeQueryString, likeQueryString),
        null,
        null,
        null,
        limitOffset
    )

    cursor.use { c ->
        while (c.moveToNext()) {
            val record =  AccountData(
                username = "",
                dispName = "",
                accountImage = "",
                wpOn = "",
                wpName = "",
                peFlg = null
            )
            columnNameListString.forEach {
                if (cursor.getString(cursor.getColumnIndex(it)) != null) {
                    record.set(it, cursor.getString(cursor.getColumnIndex(it)))
                    if (record.get(it) == "null") {
                        record.set(it, "")
                    }
                } else {
                    record.set(it, "")
                }
            }
            data.add(record)
        }
    }
    cursor.close()
    db.close()
    return data
}