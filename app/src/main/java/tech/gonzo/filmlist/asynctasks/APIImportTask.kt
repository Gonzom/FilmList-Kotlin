package tech.gonzo.filmlist.asynctasks

import android.os.AsyncTask
import android.support.annotation.MainThread
import android.support.annotation.WorkerThread
import android.support.v4.app.Fragment
import android.util.Log

import org.json.JSONException
import org.json.JSONObject

import tech.gonzo.filmlist.models.Film

/**
 * Called to get the requested film details.
 *
 * Extends AsyncTask,
 * receives the IMDB ID of the film (String),
 * and returns a result from the server (String).
 *
 * @param fragment The fragment that started the AsyncTask.
 */
class APIImportTask(fragment: Fragment) : AsyncTask<String, Void, String>() {

    companion object {
        // Log tag constant.
        private val TAG = "ImportTask"

        // JSON constants.
        private val JSON_RESPONSE = "Response"
        private val JSON_ERROR = "Error"
        private val JSON_RESULT_NA = "N/A"
        private val JSON_RESPONSE_FALSE = "False"
        private val JSON_IMDB_ID = "imdbID"
        private val JSON_TITLE = "Title"
        private val JSON_YEAR = "Year"
        private val JSON_DIRECTOR = "Director"
        private val JSON_PLOT = "Plot"
        private val JSON_POSTER = "Poster"
        private val JSON_IMDB_RATING = "imdbRating"

        // API constants.
        private val OMDB_HOME = "http://www.omdbapi.com/?i="
        private val SEARCH_PARAMS = "&type=movie&y=&r=json"
    }

    // The AsyncTask Listener.
    private val listener: APIImportTaskListener?

    init {
        // Initialize Listener;
        listener = fragment as APIImportTaskListener
    }

    /**
     * Method runs in UI Thread.
     */
    @MainThread
    override fun onPreExecute() {
        // Check if the listener is not null.
        listener?.onAPIImportTaskStarted()
    }

    /**
     * Method runs in Second Thread.
     *
     * Uses the OMDB API to search for the film with
     * the requested IMDB ID and returns the search results.
     *
     * @param params The IMDB ID of the film.
     * @return String Returns the results from the server.
     */
    @WorkerThread
    override fun doInBackground(vararg params: String): String? {
        // Call HttpAPIConnection.getDataFromAPI() to get the API result;
        // Return a string.
        return HttpAPIConnection.getDataFromAPI(OMDB_HOME, params[0], SEARCH_PARAMS, TAG)
    }

    /**
     * Method runs in UI Thread.
     *
     * Extracts from the results the JSON data
     * and converts the data to Film objects.
     *
     * @param result The String result returned from the query.
     */
    @MainThread
    override fun onPostExecute(result: String?) {
        // Check if the result is not null.
        if (result == null) {
            // No results received;
            // Pass null to the listener.
            listener?.getSelectedFilm(null)
        } else {
            // String result is not null;
            // Log result for debug.
            Log.d(TAG, result)

            // Parse JSON.
            try {
                // Create a JSON object from the String Builder.
                val rootJSON = JSONObject(result)

                // Check if an "Error" has been returned.
                if (jsonCheckErrorResponse(rootJSON)) {
                    // Value is an error;
                    // Get error message.
                    val errorMessage = rootJSON.optString(JSON_ERROR)

                    // Pass the error message to the listener.
                    listener?.onAPITaskFinishedWithError(errorMessage)

                    // Log error.
                    Log.e(TAG, errorMessage)
                } else {
                    // Response is not an error;
                    // Pass the result to the listener.
                    listener?.getSelectedFilm(jsonConverter(rootJSON))
                }
                // Catch Exceptions.
            } catch (e: JSONException) {
                // Log Exception.
                Log.e(TAG, e.message)

                // Pass the "result" string to the listener. It should be an error.
                listener?.onAPITaskFinishedWithError(result)
            }
        }
    }

    /**
     * Check if the JSON results returned an error ("False).
     *
     * @param root The root JSON result.
     *
     * @return boolean Returns true if returned an error, false if it didn't.
     * @throws JSONException The exception that can be thrown.
     */
    @Throws(JSONException::class)
    private fun jsonCheckErrorResponse(root: JSONObject): Boolean {
        // Get the Response field.
        val response = root.getString(JSON_RESPONSE)

        // Check if the response is equal to "False".
        return response == JSON_RESPONSE_FALSE
    }

    /**
     * Converts the JSON results into a Film object.
     *
     * @param rootJSON The root JSONObject.
     *
     * @return Film The Film object requested.
     * @throws JSONException The exception that can be thrown.
     */
    @Throws(JSONException::class)
    private fun jsonConverter(rootJSON: JSONObject): Film {
        // Get the "imdbID" (String).
        val imdbId = rootJSON.getString(JSON_IMDB_ID)

        // Get the "Title" (String).
        val title = rootJSON.getString(JSON_TITLE)

        // Get the "Year" (int).
        val year = rootJSON.getInt(JSON_YEAR)

        // Get the "Director" (String).
        val director = rootJSON.getString(JSON_DIRECTOR)

        // Get the "Plot" (String).
        var plot: String? = rootJSON.getString(JSON_PLOT)

        // Check if a plot had a value.
        if (plot == JSON_RESULT_NA) {
            // No plot found;
            // Set plot to null.
            plot = null
        }

        // Get the "Poster" (String).
        var posterUri = rootJSON.getString(JSON_POSTER)

        // Check if a poster URI had a value.
        if (posterUri == JSON_RESULT_NA) {
            // No posterURI found;
            // Set posterURI to an empty string.
            posterUri = ""
        }

        // Get the "imdbRating" (String).
        val imdbRatingString = rootJSON.getString(JSON_IMDB_RATING)

        // Create an imdbRating object to be populated next.
        val imdbRating: Float

        // Check if the imdbRating had a value.
        if (imdbRatingString == JSON_RESULT_NA) {
            // No imdbRating found;
            // Set imdbRating to 0.
            imdbRating = 0f
        } else {
            // Convert String value to float.
            imdbRating = imdbRatingString.toFloat()
        }

        // Create a new Film object.
        return Film(imdbId = imdbId, title = title, year = year, director = director, plot = plot, posterExternalUri = posterUri, seen = false, imdbRating = imdbRating)
    }

    /**
     * Interface to communicate with the calling Fragment.
     */
    interface APIImportTaskListener {
        /**
         * Called when the task starts.
         */
        fun onAPIImportTaskStarted()

        /**
         * Called when the task finished with an error message.
         *
         * @param errorMessage The error message returned from the server.
         */
        fun onAPITaskFinishedWithError(errorMessage: String)

        /**
         * Called after a film result has been received
         * and should be sent back to calling fragment.
         *
         * @param requestedFilm The requestedFilm object created by the search.
         */
        fun getSelectedFilm(requestedFilm: Film?)
    }
}