package com.yanagimachidev.huuurraydevapp


import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import java.lang.Exception


// 単一メッセージ情報の取得
fun queryMessage(context: Context, id: Int?) : MessageData {
    val columnNameListString = mutableListOf<String>(
        "to_username",
        "from_username",
        "content",
        "delete_flg",
        "created_at",
        "modified_at"
    )
    val columnNameListInt = mutableListOf<String>(
        "id"
    )
    val data = MessageData(
        id = null,
        toUsername = null,
        fromUsername = null,
        content = null,
        deleteFlg = null,
        createdAt = null,
        modifiedAt = null
    )
    val db = HuuurrayDBOpenHelper(context).readableDatabase
    val cursor = db.query(
        "hr_message",
        null,
        "id = ?",
        arrayOf(id.toString()),
        null,
        null,
        null,
        "1"
    )

    cursor.use { c ->
        while (c.moveToNext()) {
            columnNameListString.forEach {
                if (cursor.getString(cursor.getColumnIndex(it)) != null) {
                    data.set(it, cursor.getString(cursor.getColumnIndex(it)))
                    if (data.get(it) == "null") {
                        data.set(it, "")
                    }
                } else {
                    data.set(it, "")
                }
            }
            columnNameListInt.forEach {
                data.set(it, cursor.getInt(cursor.getColumnIndex(it)))
            }
        }
    }
    cursor.close()
    db.close()
    return data
}

// メッセージを種別ごとに全件取得
fun queryAllMessage(context: Context, tfFlg: Int, page: Int, username: String) : MutableList<MessageDispData> {
    val columnNameListString = mutableListOf<String>(
        "to_username",
        "from_username",
        "content",
        "delete_flg",
        "created_at",
        "modified_at",
        "disp_name",
        "account_image",
        "wp_on",
        "wp_name",
        "wp_url"
    )
    val columnNameListInt = mutableListOf<String>(
        "id"
    )
    val columnNameListDouble = mutableListOf<String>(
        "wp_lat",
        "wp_lng"
    )
    val data = mutableListOf<MessageDispData>()
    val db = HuuurrayDBOpenHelper(context).readableDatabase
    lateinit var cursor: Cursor
    val limit = 10
    val offset = page * limit
    if (tfFlg == 0) {
        val sql = "SELECT" +
            " msg.id as id," +
            " msg.to_username as to_username," +
            " msg.from_username as from_username," +
            " msg.content as content," +
            " msg.delete_flg as delete_flg," +
            " msg.created_at as created_at," +
            " msg.modified_at as modified_at," +
            " user.disp_name as disp_name," +
            " user.account_image as account_image," +
            " user.wp_on as wp_on," +
            " user.wp1_name as wp_name," +
            " user.wp1_url as wp_url," +
            " user.wp1_lat as wp_lat," +
            " user.wp1_lng as wp_lng" +
            " FROM hr_user as user INNER JOIN" +
            " (SELECT * FROM hr_message WHERE delete_flg = '0' and to_username = ?) as msg" +
            " ON user.username = msg.from_username" +
            " ORDER BY msg.created_at" +
            " LIMIT ?" +
            " OFFSET ?"
        cursor = db.rawQuery(sql, arrayOf(username, limit.toString(), offset.toString()))
    } else {
        val sql = "SELECT" +
            " msg.id as id," +
            " msg.to_username as to_username," +
            " msg.from_username as from_username," +
            " msg.content as content," +
            " msg.delete_flg as delete_flg," +
            " msg.created_at as created_at," +
            " msg.modified_at as modified_at," +
            " user.disp_name as disp_name," +
            " user.account_image as account_image," +
            " user.wp_on as wp_on," +
            " user.wp1_name as wp_name," +
            " user.wp1_url as wp_url," +
            " user.wp1_lat as wp_lat," +
            " user.wp1_lng as wp_lng" +
            " FROM hr_user as user INNER JOIN" +
            " (SELECT * FROM hr_message WHERE delete_flg = '0' and from_username = ?) as msg" +
            " ON user.username = msg.to_username" +
            " ORDER BY msg.created_at" +
            " LIMIT ?" +
            " OFFSET ?"
        cursor = db.rawQuery(sql, arrayOf(username, limit.toString(), offset.toString()))
    }

    cursor.use { c ->
        while (c.moveToNext()) {
            val record =  MessageDispData(
                id = null,
                toUsername = null,
                fromUsername = null,
                content = null,
                deleteFlg = null,
                createdAt = null,
                modifiedAt = null,
                dispName = null,
                accountImage = null,
                wpOn = null,
                wpName = null,
                wpUrl = null,
                wpLat = null,
                wpLng = null,
                clickType = null,
                position = null
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
            columnNameListInt.forEach {
                record.set(it, cursor.getInt(cursor.getColumnIndex(it)))
            }
            columnNameListDouble.forEach {
                record.set(it, cursor.getDouble(cursor.getColumnIndex(it)))
            }
            data.add(record)
        }
    }
    cursor.close()
    db.close()
    return data
}

// メッセージデータの作成/更新
fun upsertHrMessage(
    context: Context,
    dataStirng: Map<String, String?>,
    dataInt: Map<String, Int?>
) : Boolean {
    // レコードの存在確認
    val oldData = queryMessage(context, dataInt["id"])
    // 存在しなければ作成
    val db = HuuurrayDBOpenHelper(context).writableDatabase
    if (oldData.id == null) {
        try {
            db.use {
                val record = ContentValues().apply {
                    dataStirng.forEach { (k, v) ->
                        put(k, v)
                    }
                    dataInt.forEach{ (k, v) ->
                        put(k, v)
                    }
                }
                it.insert("hr_message", null, record)
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
                    dataInt.forEach{ (k, v) ->
                        put(k, v)
                    }
                }
                it.update("hr_message", record, "id = ?", arrayOf(dataInt["id"].toString()))
            }
        } catch (e: Exception) {
            return false
        }
    }
    return true
}

// 未読件数を取得
fun getNoOpenRecordCount (context: Context, username: String): Int {
    val db = HuuurrayDBOpenHelper(context).readableDatabase
    val sql = " SELECT COUNT(*) FROM hr_message" +
            " WHERE open_flg = '0' AND to_username = ?"
    val cursor = db.rawQuery(sql, arrayOf(username))
    var count = 0
    if (cursor.moveToNext()) {
        count = cursor.getInt(0)
    }
    cursor.close()
    db.close()
    return count
}

// 未読を既読に更新
fun NoOpenRecordUpdate (context: Context) {
    val db = HuuurrayDBOpenHelper(context).writableDatabase
    val contentValues = ContentValues()
    contentValues.put("open_flg", "1")
    db.update("hr_message", contentValues, "open_flg = ?", arrayOf("0"))
    db.close()
}