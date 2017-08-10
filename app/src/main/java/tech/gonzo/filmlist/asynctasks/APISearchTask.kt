package tech.gonzo.filmlist.asynctasks

import android.os.AsyncTask
import android.support.annotation.MainThread
import android.support.annotation.WorkerThread
import android.support.v4.app.Fragment
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import tech.gonzo.filmlist.models.ShortFilm
import java.util.*

/**
 * Called to get a list of up to 10 films
 * containing the user entered search word.
 *
 * Extends AsyncTask,
 * receives the search word (String),
 * and returns a result from the server (String).
 *
 * @param fragment The fragment that started the AsyncTask.
 */
class APISearchTask(fragment: Fragment) : AsyncTask<String, Void, String>() {

    companion object {
        // Log tag constant.
        private val TAG = "APISearchTask"

        // JSON constants.
        private val JSON_ERROR = "Error"
        private val JSON_SEARCH = "Search"
        private val JSON_TITLE = "Title"
        private val JSON_YEAR = "Year"
        private val JSON_IMDB_ID = "imdbID"

        // API constants.
        private val OMDB_HOME = "http://www.omdbapi.com/?s="
        private val SEARCH_PARAMS = "&type=movie&y=&r=json"
    }

    // The AsyncTask Listener.
    private val listener: APISearchTaskListener?

    // The search query.
    private var searchQuery: String = ""

    init {
        // Initialize Listener;
        listener = fragment as APISearchTaskListener
    }

    /**
     * Method runs in Second Thread.
     *
     * Uses the OMDB API to search for films containing the search key
     * and returns the search results.
     *
     * @param params The user input search key.
     *
     * @return String Returns the results from the server.
     */
    @WorkerThread
    override fun doInBackground(vararg params: String): String? {
        // Get the Search Query.
        searchQuery = params[0]

        // Call HttpAPIConnection.getDataFromAPI() to get the API result;
        // Return a string.
        return HttpAPIConnection.getDataFromAPI(OMDB_HOME, searchQuery, SEARCH_PARAMS, TAG)
    }

    /**
     * Method runs in UI Thread.
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
            listener?.getFilms(null, searchQuery)
        } else {
            // String result is not null;
            // Log result for debug.
            Log.d(TAG, result)

            // Parse JSON.
            try {
                // Create a JSON object from the String Builder.
                val rootJSON = JSONObject(result)

                // Call jsonCheckErrorResponse() to check if an "Error" has been returned.
                if (jsonCheckErrorResponse(rootJSON)) {
                    // An error has returned;
                    // Get error message.
                    val errorMessage = rootJSON.optString(JSON_ERROR)

                    // Pass the error message to the listener.
                    listener?.onAPITaskFinishedWithError(errorMessage)

                    // Log error.
                    Log.d(TAG, errorMessage)
                } else {
                    // Response is not an error;
                    // Pass the result to the listener.
                    listener?.getFilms(jsonConverter(rootJSON), searchQuery)
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
     * Check if the JSON results returned an error.
     *
     * @param root The root JSON result.
     *
     * @return boolean Returns true if returned an error, false if it didn't.
     */
    private fun jsonCheckErrorResponse(root: JSONObject): Boolean {
        // Check if the response has "Error" in it.
        return root.has(JSON_ERROR)
    }

    /**
     * Converts the JSON results into an ArrayList<Place>.
     *
     * @param rootJSON The root JSONObject.
     *
     * @return ArrayList<ShortFilm> An ArrayList of ShortFilm objects.
     * @throws JSONException The exception that can be thrown.
     */
    @Throws(JSONException::class)
    private fun jsonConverter(rootJSON: JSONObject): ArrayList<ShortFilm> {
        // Get the "Search" JSON array.
        val searchJSON = rootJSON.getJSONArray(JSON_SEARCH)

        // Create a new ArrayList.
        val searchResult = ArrayList<ShortFilm>()

        // Go over the length of the data JSONArray.
        for (i in 0..searchJSON.length() - 1) {
            // Create a JSON object from item at position i.
            val filmResult = searchJSON.getJSONObject(i)

            // Get the IMDB ID (String).
            val imdbId = filmResult.getString(JSON_IMDB_ID)

            // Get the "Title" (String).
            val title = filmResult.getString(JSON_TITLE)

            // Get the "Year" (int).
            val year = filmResult.getInt(JSON_YEAR)

            // Create a new Film object; Add it to the ArrayList.
            searchResult.add(ShortFilm(imdbId, title, year))
        }

        // Return ArrayList.
        return searchResult
    }

    /**
     * Interface to communicate with the calling Fragment.
     */
    interface APISearchTaskListener {
        /**
         * Called when the task finished with an error message.
         *
         * @param errorMessage The error message returned from the server.
         */
        fun onAPITaskFinishedWithError(errorMessage: String)

        /**
         * Called when search results have been received
         * and should be sent back to calling fragment.
         *
         * @param films       The search results in an ArrayList of Film objects.
         * @param searchQuery The search query used.
         */
        fun getFilms(films: ArrayList<ShortFilm>?, searchQuery: String)
    }
}