package tech.gonzo.filmlist.asynctasks

import android.support.annotation.WorkerThread
import android.util.Log

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Called to open a connection to OMDB's API
 * and receive a result from the server.
 */
internal object HttpAPIConnection {

    private val API_KEY = "&apikey=547255e5"

    /**
     * Query method that is shared by tasks.
     * The method requires the webAPI string, the user input search query
     * and the API search parameters. It then will return a string ready to be parsed.
     * In addition the method requires the static final Log tag string
     * for correct error logging
     *
     * @param omdbHome     The OMDB API used.
     * @param searchQuery  The search the user requested.
     * @param searchParams The default parameters for the relevant search.
     * @param TAG          The Async Task log tag name.
     *
     * @return String Returns a string result which will be converted later to a JSON object.
     */
    @WorkerThread
    fun getDataFromAPI(omdbHome: String, searchQuery: String, searchParams: String, TAG: String): String? {
        // Create a null HttpURLConnection to be populated next.
        var connection: HttpURLConnection? = null

        // Create a null BufferedReader to be populated next.
        var reader: BufferedReader? = null

        // Create a StringBuilder to be populated next.
        val builder = StringBuilder()

        try {
            // Use an Encoder to encode special characters.
            val searchQueryEncoded = URLEncoder.encode(searchQuery, "UTF-8")

            // Create a URL object
            val url = URL(omdbHome + searchQueryEncoded + searchParams + API_KEY)

            // Open an HTTP connection.
            connection = url.openConnection() as HttpURLConnection

            // Check the result status of the connection.
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                // An error response code returned;
                // Get the Error Stream from the connection.
                reader = BufferedReader(InputStreamReader(connection.errorStream))
            } else {
                // A 200 response code returned;
                // Get the Input Stream from the connection.
                reader = BufferedReader(InputStreamReader(connection.inputStream))
            }

            reader.lineSequence().forEach {
                // Append line to the String Builder.
                builder.append(it)
            }

            // Catch Exceptions.
        } catch (e: IOException) {
            // Log Exception.
            Log.e(TAG, e.message)
        } finally {
            // Check if the internet connection is still open.
            if (connection != null) {
                // Internet connection is still open;
                // Close the internet connection.
                connection.disconnect()
            }

            // Check if the BufferedReader stream is still open.
            if (reader != null) {
                try {
                    // BufferedReader stream is still open;
                    // Close the stream.
                    reader.close()
                    // Catch Exceptions.
                } catch (e: IOException) {
                    // Log Exception.
                    Log.e(TAG, e.message)
                }
            }
        }

        // Convert the StringBuilder to a regular String;
        // Return String.
        return builder.toString()
    }
}