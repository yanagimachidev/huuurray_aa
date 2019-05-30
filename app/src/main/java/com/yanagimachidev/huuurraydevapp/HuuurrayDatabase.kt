package com.yanagimachidev.huuurraydevapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.lang.Exception

private const val DB_NAME = "HuuurrayDatabase"
private const val DB_VERSION = 18

// HuuurrayDBOpenHelper
class HuuurrayDBOpenHelper(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        // hr_user テーブル
        db?.execSQL("CREATE TABLE hr_user ( " +
                "username TEXT PRIMARY KEY, " +
                "disp_name TEXT, " +
                "sex TEXT, " +
                "birthday TEXT, " +
                "profile TEXT, " +
                "account_image TEXT, " +
                "back_image TEXT, " +
                "st_sp INTEGER DEFAULT 20, " +
                "sp_sp INTEGER DEFAULT 0, " +
                "send_sp_to INTEGER DEFAULT 0, "+
                "last_check_in TEXT, " +
                "favorite_user TEXT DEFAULT '0', " +
                "wp_on TEXT DEFAULT '0', " +
                "wp1_name TEXT, " +
                "wp1_category TEXT, " +
                "wp1_url TEXT, " +
                "wp1_lat REAL, " +
                "wp1_lng REAL, " +
                "wp1_lat_sort REAL, " +
                "wp1_lng_sort REAL, " +
                "wp1_latlng_sort REAL, " +
                "wp1_image TEXT, " +
                /*
                "wp2_name TEXT, " +
                "wp2_category TEXT, " +
                "wp2_url TEXT, " +
                "wp2_lat REAL, " +
                "wp2_lng REAL, " +
                "wp2_image TEXT, " +
                "wp3_name TEXT, " +
                "wp3_category TEXT, " +
                "wp3_url TEXT, " +
                "wp3_lat REAL, " +
                "wp3_lng REAL, " +
                "wp3_image TEXT, " +
                */
                "delete_flg TEXT DEFAULT '0')")

        // hr_message テーブル
        db?.execSQL("CREATE TABLE hr_message ( " +
                "id INTEGER PRIMARY KEY, " +
                "to_username TEXT, " +
                "from_username TEXT, " +
                "content TEXT, " +
                "open_flg TEXT DEFAULT '0', " +
                "delete_flg TEXT DEFAULT '0', " +
                "created_at TEXT, " +
                "modified_at TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS hr_user")
        db?.execSQL("DROP TABLE IF EXISTS hr_message")
        onCreate(db)
    }

}