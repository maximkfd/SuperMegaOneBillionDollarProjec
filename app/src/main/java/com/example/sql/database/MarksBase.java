package com.example.sql.database;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;


public class MarksBase extends SQLiteOpenHelper implements BaseColumns{

    private static final String DATABASE_NAME = "marks";
    private static final int DATABASE_VERSION = 1;
    public static final String AUTHOR_NAME = "author_name";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String SHORT_DESCRIPTION = "short_description";
    public static final String FULL_DESCRIPTION = "full_description";
    public static final String REWARD = "reward";
    public static final String TABLE_NAME = "table_marks";


    public static final String MARKER_TABLE_NAME = "table_markers";
    public static final String MARKER= "marker";
    public static final String MARKER_ID= "marker_id";


    public static final String SQL_CREATE_ENTRIES = "CREATE TABLE "
            + TABLE_NAME + " (" + MarksBase._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + FULL_DESCRIPTION + " VARCHAR(255), "
            + SHORT_DESCRIPTION + " VARCHAR(20), "
            + REWARD + " INTEGER, "
            + LATITUDE + " DOUBLE, "
            + LONGITUDE + " DOUBLE, "
            + AUTHOR_NAME + " VARCHAR(20));";
//            + AUTHOR_NAME + " VARCHAR(20), "
//            + MARKER_ID + "VARCHAR(20));";
//    private static final String SQL_CREATE_MARKS_ENTRIES = "CREATE TABLE "
//            + MARKER_TABLE_NAME + " (" + MarksBase._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
//            + MARKER + "";
    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS "
            + TABLE_NAME;

    public MarksBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        //TODO wtf
        db.execSQL(SQL_DELETE_ENTRIES);
        // Создаём новый экземпляр таблицы
        onCreate(db);
    }
    public static void del(SQLiteDatabase db){
        //TODO wtf
        db.execSQL(SQL_DELETE_ENTRIES);
    }
    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(SQL_CREATE_ENTRIES);
//
//     db.execSQL();
    }
}

