package tech.gonzo.filmlist.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.util.Log
import tech.gonzo.filmlist.models.Film
import java.util.*

/**
 * Supported methods are: addFilm, updateFilm, deleteFilm,
 * deleteAllFilms, undoClearList, queryFilmWithImdbId and queryAllFilms.
 *
 * @param context The context which is creating the handler.
 */
class FilmDBHandler(context: Context) {

    companion object {
        // Log tag constant.
        private val TAG = "FilmDBHandler"

        // Table name constants.
        internal val TABLE_NAME_FILM = "film"

        // Table columns constants.
        internal val COLUMN_ID = "_id"
        val COLUMN_TIMESTAMP = "timestamp"
        internal val COLUMN_IMDB_ID = "imdbId"
        val COLUMN_TITLE = "title"
        val COLUMN_YEAR = "year"
        internal val COLUMN_DIRECTOR = "director"
        internal val COLUMN_PLOT = "plot"
        internal val COLUMN_POSTER_EXTERNAL = "poster_external"
        internal val COLUMN_POSTER_INTERNAL = "poster_internal"
        val COLUMN_HAS_BEEN_SEEN = "seen"
        val COLUMN_USER_RATING = "userRating"
        internal val COLUMN_IMDB_RATING = "imdbRating"

        // Need to save this as a date.
        // public static final String COLUMN_LAST_UPDATED = "lastUpdated";

        // Sort order constants.
        val SORT_ORDER_ASC = "ASC"
        val SORT_ORDER_DSC = "DESC"
    }

    // Database Helper.
    private val helper: FilmDBHelper = FilmDBHelper(context)

    /**
     * Called when an item is inserted into a table.

     * @param film The Film object being added to the database.
     */
    fun addFilm(film: Film) {
        // Get a reference to the DB file with WRITING permission.
        val db = helper.writableDatabase

        // Create a ContentValues object to insert into the table.
        val values = ContentValues()
        values.put(COLUMN_TIMESTAMP, film.timestamp)
        values.put(COLUMN_IMDB_ID, film.imdbId)
        values.put(COLUMN_TITLE, film.title)
        values.put(COLUMN_YEAR, film.year)
        values.put(COLUMN_DIRECTOR, film.director)
        values.put(COLUMN_PLOT, film.plot)
        values.put(COLUMN_POSTER_EXTERNAL, film.posterExternalUri)
        values.put(COLUMN_POSTER_INTERNAL, film.posterInternalUri)
        values.put(COLUMN_HAS_BEEN_SEEN, film.seen)
        values.put(COLUMN_USER_RATING, film.userRating)
        values.put(COLUMN_IMDB_RATING, film.imdbRating)

        val logMsg = "addFilm: Timestamp: ${film.timestamp}; IMDB_ID: ${film.imdbId}; Title: ${film.title}; Year: ${film.year};" +
                " Director: ${film.director}; Plot: ${film.plot}; External URI: ${film.posterExternalUri}; Internal URI: ${film.posterInternalUri}; Seen: ${film.seen};" +
                " User_Rating: ${film.userRating}; IMDB_Rating: ${film.imdbRating}"

        try {
            // Insert the Film object into the table.
            db.insertOrThrow(TABLE_NAME_FILM, null, values)

            // Catch Exceptions.
        } catch (e: SQLException) {
            // Log Exception.
            Log.e(TAG, e.message)
        } finally {
            // Check if the database is null.
            db.close()
        }
        // Log operation.
        Log.i(TAG, logMsg)
    }

    /**
     * Called when an item had its data changed.
     *
     * @param film The Film object being updated in the database.
     */
    fun updateFilm(film: Film) {
        // Get a reference to the DB file with WRITING permission.
        val db = helper.writableDatabase

        // Create a ContentValues object to update in the table.
        val values = ContentValues()
        values.put(COLUMN_TIMESTAMP, film.timestamp)
        values.put(COLUMN_IMDB_ID, film.imdbId)
        values.put(COLUMN_TITLE, film.title)
        values.put(COLUMN_YEAR, film.year)
        values.put(COLUMN_DIRECTOR, film.director)
        values.put(COLUMN_PLOT, film.plot)
        values.put(COLUMN_POSTER_EXTERNAL, film.posterExternalUri)
        values.put(COLUMN_POSTER_INTERNAL, film.posterInternalUri)
        values.put(COLUMN_HAS_BEEN_SEEN, film.seen)
        values.put(COLUMN_USER_RATING, film.userRating)
        values.put(COLUMN_IMDB_RATING, film.imdbRating)

        // Update the values in the table.
        db.update(TABLE_NAME_FILM, values, COLUMN_ID + " = " + film.id, null)

        // Close database.
        db.close()

        // Log operation.
        Log.d(TAG, "updateFilm")
    }

    /**
     * Called when an item is being removed from a table.
     *
     * @param id The id of the Film object being deleted from the database.
     */
    fun deleteFilm(id: Long) {
        // Get a reference to the DB file with Writing permission.
        val db = helper.writableDatabase

        // Delete the film with the given _id value from the table.
        db.delete(TABLE_NAME_FILM, COLUMN_ID + " = ?", arrayOf(id.toString()))

        // Close database.
        db.close()

        // Log operation.
        Log.d(TAG, "deleteFilm")
    }

    /**
     * Called when all of the items in the table are being removed.
     */
    fun deleteAllFilms() {
        // Get a reference to the DB file with WRITING permission.
        val db = helper.writableDatabase

        // Delete all entries from the table.
        db.delete(TABLE_NAME_FILM, null, null)

        // Close database.
        db.close()

        // Log operation.
        Log.d(TAG, "deleteAllFilms")
    }

    /**
     * Called when an item in the database is being queried.
     *
     * @param imdbId The IMDB ID of the Film object being queried in the database.
     *
     * @return Film Returns the Film object.
     */
    fun queryFilmWithImdbId(imdbId: String): Film? {
        // Get a reference to the DB file with READING permission.
        val db = helper.readableDatabase

        // Query one Film with an IMDB ID.
        val cursor = db.query(TABLE_NAME_FILM, null, COLUMN_IMDB_ID + " = ?", arrayOf(imdbId), null, null, null)

        // Create a null Film object.
        var film: Film? = null

        // Check if the query has returned a result.
        if (cursor.moveToNext()) {
            // It did;
            // Call getFilmFromCursor() to get the Film object.
            film = getFilmFromCursor(cursor, imdbId)
        }

        // Close cursor.
        cursor.close()

        // Close database.
        db.close()

        // Log operation.
        Log.i(TAG, "queryFilm")

        // Return a film object.
        return film
    }

    /**
     * Called when the database is being completely queried.
     *
     * @param sortColumn The requested sorting by column.
     * @param sortOrder  The requested sorting order.
     * @return ArrayList<Film> Returns a new Film ArrayList.
     */
    fun queryAllFilms(sortColumn: String, sortOrder: String): ArrayList<Film> {
        // Get a reference to the DB file with READING permission.
        val db = helper.readableDatabase

        // Query the database and create a cursor.
        val cursor = db.query(TABLE_NAME_FILM, null, null, null, null, null, sortColumn + " COLLATE NOCASE " + sortOrder)

        // Create a new ArrayList.
        val filmsArray = ArrayList<Film>()

        // Check if the cursor has more lines.
        while (cursor.moveToNext()) {
            // It does;
            // Call getFilmFromCursor() to get the Film object;
            // Create a new Film object and add it to the ArrayList.
            filmsArray.add(getFilmFromCursor(cursor))
        }

        // Close cursor.
        cursor.close()

        // Close database.
        db.close()

        // Log operation.
        Log.i(TAG, "queryAllFilms")

        // Return ArrayList.
        return filmsArray
    }

    /**
     * Called to extract a Film object from the Cursor.
     *
     * @param cursor The database Cursor.
     * @param filmImdbId  The Film's IMDB ID, if used.
     *
     * @return Film Returns a new Film object.
     */
    private fun getFilmFromCursor(cursor: Cursor, filmImdbId: String = ""): Film {
        // Get the selected Film's data.
        val id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID))
        val timestamp = cursor.getLong(cursor.getColumnIndex(COLUMN_TIMESTAMP))

        val imdbId = if (filmImdbId.isEmpty()) {
            cursor.getString(cursor.getColumnIndex(COLUMN_IMDB_ID))
        } else {
            filmImdbId
        }

        val title = cursor.getString(cursor.getColumnIndex(COLUMN_TITLE))
        val year = cursor.getInt(cursor.getColumnIndex(COLUMN_YEAR))
        val director = cursor.getString(cursor.getColumnIndex(COLUMN_DIRECTOR))
        val plot = cursor.getString(cursor.getColumnIndex(COLUMN_PLOT))
        val posterExternalUri = cursor.getString(cursor.getColumnIndex(COLUMN_POSTER_EXTERNAL))
        val poserInternalUri = cursor.getString(cursor.getColumnIndex(COLUMN_POSTER_INTERNAL))

        val booleanInNumberFormat = cursor.getInt(cursor.getColumnIndex(COLUMN_HAS_BEEN_SEEN))
        val seen = booleanInNumberFormat != 0

        val userRating = cursor.getFloat(cursor.getColumnIndex(COLUMN_USER_RATING))
        val imdbRating = cursor.getFloat(cursor.getColumnIndex(COLUMN_IMDB_RATING))

        // Create a new Film object.
        return Film(imdbId, title, year, id, timestamp, director, plot, posterExternalUri, poserInternalUri, seen, userRating, imdbRating)
    }
}