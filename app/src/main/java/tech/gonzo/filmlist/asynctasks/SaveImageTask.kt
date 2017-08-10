package tech.gonzo.filmlist.asynctasks

import android.app.Activity
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Bundle
import android.support.annotation.MainThread
import android.support.annotation.WorkerThread
import android.support.v4.app.Fragment
import android.util.Log
import tech.gonzo.filmlist.ui.fragments.ViewFilmFragment
import tech.gonzo.filmlist.utils.Constants
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Called to save the film poster on the device.
 *
 *
 * Extends AsyncTask,
 * receives an image (Bitmap),
 * and returns a File (File).
 *
 * @param fragment The fragment that started the AsyncTask.
 */
class SaveImageTask(private val activity: Activity, fragment: Fragment) : AsyncTask<Bundle, Void, File>() {

    companion object {
        // Log tag constant.
        private val TAG = "SaveImageTask"

        // Directory constants.
        private val IMAGE_DIRECTORY = "Film Posters"
    }

    // The AsyncTask Listener.
    private val listener: SaveImageTaskListener?

    init {
        // Initialize Listener;
        listener = fragment as SaveImageTaskListener
    }

    /**
     * Method runs in UI Thread.
     * Saves the current film title for later use.
     */
    @MainThread
    override fun onPreExecute() {
        // Check if the listener is not null.
        listener?.onImageTaskStarted(Constants.TASK_SAVE_IMAGE)
    }

    /**
     * Method runs in Second Thread.
     * Saves Bitmap image on device and returns a File.
     * @param bundles The Bundle containing film information and the Bitmap image.
     *
     * @return File Returns a File that is saved on the device.
     */
    @WorkerThread
    // @RequiresPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    override fun doInBackground(vararg bundles: Bundle): File? {
        // Log operation.
        Log.d(TAG, "Saving image on device...")

        // Get the bundle.
        val bundle = bundles[0]

        // Get the information from the bundle.
        val image = bundle.getParcelable<Bitmap>(ViewFilmFragment.POSTER_IMAGE)
        var filmTitle = bundle.getString(ViewFilmFragment.FILM_TITLE)
        val imdbId = bundle.getString(ViewFilmFragment.FILM_IMDB_ID)

        // Create a new ByteArrayOutputStream.
        val byteArrayOutputStream = ByteArrayOutputStream()

        // Create a null FileOutputStream;
        var fileOutputStream: FileOutputStream? = null

        // Create a null File to be populated later.
        var posterFile: File? = null

        try {
            if (image != null) {
                // Compress image to a PNG format.
                image.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, byteArrayOutputStream)

                //  Create a path to a new folder where we will place our picture
                // as a sub directory in the App's directory.
                val dir = File(activity.filesDir, IMAGE_DIRECTORY)

                // Make sure the Film Posters directory exists.
                // If it doesn't exist, create it.
                val dirAvailable = dir.exists() || dir.mkdirs()

                if (dirAvailable) {
                    // Directory created; Log it.
                    Log.i(TAG, "Directory created or previously existed.")

                    // Check that the filmTitle is not null
                    // and that it doesn't contain a backslash in its name.
                    if (filmTitle != null && filmTitle.contains("/")) {
                        // filmTitle has a backslash in its name. Replace it with an underscore.
                        filmTitle = filmTitle.replace("/".toRegex(), "_")
                    }

                    // Create a new file of type PNG with the name of the current film title.
                    posterFile = File(dir, String.format("%1\$s_%2\$s.png", filmTitle, imdbId))

                    // Create a FileOutputStream to write into posterFile.
                    fileOutputStream = FileOutputStream(posterFile)

                    // Save file as a ByteArray
                    val bitmapData = byteArrayOutputStream.toByteArray()

                    // Write bitmapData data to the posterFile.
                    fileOutputStream.write(bitmapData)

                    // Clear the extra unwritten bytes from the fileOutputStream.
                    fileOutputStream.flush()
                } else {
                    // Directory wasn't created; Log it.
                    Log.i(TAG, "Error! Directory not created!")
                }
            }
            // Catch Exceptions.
        } catch (e: IOException) {
            // Log Exception.
            Log.e(TAG, e.message)
        } catch (e: NullPointerException) {
            Log.e(TAG, e.message)
        } finally {
            try {
                // Check if the FileOutputStream is still open.
                if (fileOutputStream != null) {
                    // It is; Close the FileOutputStream.
                    fileOutputStream.close()
                }
                // Catch Exceptions.
            } catch (e: IOException) {
                // Log Exception.
                Log.e(TAG, e.message)
            }

            try {
                // Close the ByteArrayOutputStream.
                byteArrayOutputStream.close()
            } catch (e: IOException) {
                // Log Exception.
                Log.e(TAG, e.message)
            }

        }
        // Return the file.
        return posterFile
    }

    /**
     * Method runs in UI Thread.
     * Sets the File to the ImageView.
     *
     * @param file The File that was saved on the device.
     */
    @MainThread
    override fun onPostExecute(file: File?) {
        // Check if the listener is not null.
        listener?.getFile(file)
    }

    /**
     * Interface to communicate with the calling Fragment.
     */
    interface SaveImageTaskListener {
        /**
         * Called when the task starts.
         *
         * @param task The type of task that started operation.
         */
        fun onImageTaskStarted(task: Int)

        /**
         * Called after the poster image is saved locally on the device
         * and should be sent back to calling fragment.
         *
         * @param file The File of the image poster saved on the device.
         */
        fun getFile(file: File?)
    }
}