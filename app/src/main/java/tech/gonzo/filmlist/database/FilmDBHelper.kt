package tech.gonzo.filmlist.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

/**
 * Created by Gonny on 17/1/2016.
 *
 * The FilmDBHelper is used to create the SQL database.
 *
 * @param context The current Context.
 */
internal class FilmDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        // Create Log tag constant.
        private val TAG = "FilmDBHelper"

        // Database Name constant.
        private val DATABASE_NAME = "films.db"

        // Database Version number constant.
        private val DATABASE_VERSION = 1
    }

    /**
     * Called when the database is created for the first time.
     * This is where the creation of tables and the initial population of the tables should happen.

     * @param db The database name which is being written to/read from.
     */
    override fun onCreate(db: SQLiteDatabase) {
        // Create database table string.
        val sql = "CREATE TABLE ${FilmDBHandler.TABLE_NAME_FILM} (${FilmDBHandler.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT, ${FilmDBHandler.COLUMN_TIMESTAMP} INTEGER, ${FilmDBHandler.COLUMN_IMDB_ID} TEXT," +
                " ${FilmDBHandler.COLUMN_TITLE} TEXT, ${FilmDBHandler.COLUMN_YEAR} INTEGER, ${FilmDBHandler.COLUMN_DIRECTOR} TEXT, ${FilmDBHandler.COLUMN_PLOT} TEXT, ${FilmDBHandler.COLUMN_POSTER_EXTERNAL} TEXT," +
                " ${FilmDBHandler.COLUMN_POSTER_INTERNAL} TEXT, ${FilmDBHandler.COLUMN_HAS_BEEN_SEEN} BOOLEAN, ${FilmDBHandler.COLUMN_USER_RATING} REAL, ${FilmDBHandler.COLUMN_IMDB_RATING} REAL)"

        // Create database table.
        db.execSQL(sql)

        // Create a log when a new table has been created.
        Log.d(TAG, "onCreate: + " + sql)
    }

    /**
     * Called when the database needs to be upgraded.
     * The implementation should use this method to:
     * - drop tables,
     * - add tables,
     * - or do anything else it needs to upgrade to the new schema version.
     *
     * @param db         The database name which is being written to/read from.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }
}