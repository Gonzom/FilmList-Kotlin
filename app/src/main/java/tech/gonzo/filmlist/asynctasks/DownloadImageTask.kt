package tech.gonzo.filmlist.asynctasks

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.support.annotation.MainThread
import android.support.annotation.WorkerThread
import android.support.v4.app.Fragment
import android.util.Log
import tech.gonzo.filmlist.utils.Constants
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Called to download the film poster from the URI.
 *
 * Extends AsyncTask,
 * receives a URL (String),
 * posts download progress (int)
 * and returns an image (Bitmap).
 *
 * @param fragment The fragment that started the AsyncTask.
 */
class DownloadImageTask(fragment: Fragment) : AsyncTask<String, Int, Bitmap>() {

    companion object {
        // Log tag constant.
        private val TAG = "DownloadImageTask"
    }

    // The AsyncTask Listener.
    private val listener: DownloadImageTaskListener?

    init {
        // Initialize Listener;
        listener = fragment as DownloadImageTaskListener
    }

    /**
     * Method runs in UI Thread.
     * Clears the ImageView and sets (and resets) the Progress Bar.
     */
    @MainThread
    override fun onPreExecute() {
        // Check if the listener is not null.
        listener?.onImageTaskStarted(Constants.TASK_DOWNLOAD_IMAGE)
    }

    /**
     * Method runs in Second Thread.
     * Downloads film poster and returns a Bitmap image.
     *
     * @param urls The film poster URL supplied by the user.
     *
     * @return Bitmap Returns a Bitmap file type of the poster.
     */
    @WorkerThread
    override fun doInBackground(vararg urls: String): Bitmap? {
        // Create a Bitmap object from the result of the downloadImage method,
        // with the supplied user URL.
        // and pass the image to the UI Thread.
        return downloadImage(urls[0])
    }

    /**
     * Downloads the film poster.
     *
     * @param urlString The film poster URL supplied by the user.
     *
     * @return Bitmap Returns a Bitmap file type of the poster.
     */
    @WorkerThread
    //@RequiresPermission(android.Manifest.permission.INTERNET)
    private fun downloadImage(urlString: String): Bitmap? {
        // Log operation.
        Log.d(TAG, "downloadImage + Downloading poster...")

        // Create a null HttpURLConnection to be populated next.
        var httpURLConnection: HttpURLConnection? = null

        // Create a null InputStream to be populated next.
        var inputStream: InputStream? = null

        // Create a null ByteArray buffered stream to be populated next.
        var buffer: ByteArrayOutputStream? = null

        // Create a null Bitmap to be populated next.
        var bitmap: Bitmap? = null

        try {
            // Create a URL object
            val url = URL(urlString)

            // Open an HTTP connection.
            httpURLConnection = url.openConnection() as HttpURLConnection

            // Get the Input Stream from the connection.
            inputStream = httpURLConnection.inputStream

            // Pass the content length to the listener.
            listener?.onDownloadImageTaskSetMaxProgress(httpURLConnection.contentLength)

            // Create a ByteArray buffered stream.
            buffer = ByteArrayOutputStream()

            // Create attribute to represent a 2 kilobyte (2048 bytes) array.
            val data = ByteArray(2048)

            // Create attribute to represent the total amount of bytes read.
            val totalBytesRead = inputStream.readBytes(data.size)

            // Write to buffer the data read.
            buffer.write(totalBytesRead)

            // Clear the extra unwritten bytes from the buffer.
            buffer.flush()

            // Convert the buffer to a ByteArray.
            val image = buffer.toByteArray()

            // Close Internet connection.
            httpURLConnection.disconnect()

            // Create a Bitmap image from the ByteArray.
            bitmap = BitmapFactory.decodeByteArray(image, 0, image.size)

            // Catch Exceptions.
        } catch (e: IOException) {
            // Log Exception.
            Log.e(TAG, e.message)
        } finally {
            // Check if the InputStream is still open.
            if (inputStream != null) {
                try {
                    // InputStream is still open;
                    // Close InputStream.
                    inputStream.close()
                    // Catch Exceptions.
                } catch (e: IOException) {
                    // Log Exception.
                    Log.e(TAG, e.message)
                }
            }

            // Check if the ByteArray buffered stream is still open.
            if (buffer != null) {
                try {
                    // ByteArray buffered stream is still open;
                    // Close ByteArray buffered stream.
                    buffer.close()
                    // Catch Exceptions.
                } catch (e: IOException) {
                    // Log Exception.
                    Log.e(TAG, e.message)
                }
            }

            // Check if the internet connection is still open.
            if (httpURLConnection != null) {
                // Internet connection is still open;
                // Close Internet connection.
                httpURLConnection.disconnect()
            }
        }
        // The Bitmap has returned;
        // Might return as null.
        return bitmap
    }

    /**
     * Method runs in UI Thread.
     * Sets the Bitmap to the ImageView.
     *
     * @param result The Bitmap image that was downloaded.
     */
    @MainThread
    override fun onPostExecute(result: Bitmap?) {
        // Check if the listener is not null.
        listener?.getPoster(result)
    }

    /**
     * Interface to communicate with the calling Fragment.
     */
    interface DownloadImageTaskListener {
        /**
         * Called when the task starts.
         *
         * @param task The type of task that started operation.
         */
        fun onImageTaskStarted(task: Int)

        /**
         * Called when the file size is known and is passed to the Progress Dialog.
         *
         * @param contentLength The size of the file being downloaded.
         */
        fun onDownloadImageTaskSetMaxProgress(contentLength: Int)

        /**
         * Called when the download progress should be updated in the Progress Dialog.
         *
         * @param progress The totalBytesRead sent by downloadImage every 2K downloaded (2048 bytes).
         */
        fun onDownloadImageProgressUpdate(vararg progress: Int)

        /**
         * Called after the poster image has been downloaded
         * and should be sent back to calling fragment.
         *
         * @param poster The poster image as a Bitmap object.
         */
        fun getPoster(poster: Bitmap?)
    }
}