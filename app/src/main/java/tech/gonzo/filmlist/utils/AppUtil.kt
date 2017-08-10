package tech.gonzo.filmlist.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.util.Log
import tech.gonzo.filmlist.R

/**
 * Created by Gonny on 31/1/2017.
 * -
 */
object AppUtil {

    /**
     * Checks if the application is connected to the internet.
     *
     * @param context The current Context.
     *
     * @return boolean Returns true if there is a network connection.
     */
    // @RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
    fun isAppConnectedToNetwork(context: Context): Boolean {
        // Create a ConnectivityManager.
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // Check if the internet connection is on.
        if (connectivityManager.activeNetworkInfo != null) {
            // There is an active connection;
            // Check if the internet connection is in a connected state.
            if (connectivityManager.activeNetworkInfo.isConnected) {
                // Return true, there is a network connection.
                return true
            }
        }
        // The internet connection is off or not connected.
        return false
    }

    /**
     * Creates an AlertDialog to inform the user that there isn't an active Internet connection.
     *
     * @param context The current Context.
     */
    fun showNoNetworkConnectionDialog(context: Context) {
        // Instantiate an AlertDialog.Builder with its constructor.
        val builder = AlertDialog.Builder(context)

        // Chain together various setter methods to set the dialog characteristics.
        builder
                // Set the Dialog's title.
                .setTitle(R.string.dialog_network_connection_error_title)

                // Set the Dialog's message
                .setMessage(R.string.dialog_network_connection_error_msg)

                // Add the Dialog's Positive Button.
                .setPositiveButton(R.string.dialog_option_turn_on) { _, _ ->
                    // Call startSettingsIntent() to start the Settings Intent.
                    startSettingsIntent(context)
                }

                // Add the Dialog's Negative Button.
                .setNegativeButton(R.string.dialog_option_later) { dialog, _ ->
                    // Dismiss the dialog.
                    dialog.dismiss()
                }

                // Creates an AlertDialog with the arguments supplied to this builder and immediately displays the dialog.
                .show()
    }

    /**
     * Called when there is no Internet Connection and the user clicked to open the Settings Menu.
     */
    fun startSettingsIntent(context: Context) {
        try {
            // Create an Intent to the device's WIFI settings page.
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS)

            // Start Intent.
            context.startActivity(intent)

            // Unknown which exception this throws.
        } catch (e: Exception) {
            // Error log.
            Log.e("WIFI settings", "Trying to open WIFI settings ${e.message}")
        }
    }

    /**
     * Called when an image was picked from the gallery and it should be decoded into a Bitmap.
     *
     * @param pathName  The location path of the image on the device.
     * @param reqWidth  The width of the ImageView.
     * @param reqHeight The height of the ImageView.
     *
     * @return Bitmap Returns the Bitmap image.
     */
    fun decodeSampledBitmapFromFile(pathName: String, reqWidth: Int, reqHeight: Int): Bitmap {
        // First decode with inJustDecodeBounds=true to check dimensions.
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(pathName, options)

        // Call calculateInSampleSize() to calculate inSampleSize.
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)

        // Decode bitmap with inSampleSize set.
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(pathName, options)
    }

    /**
     * Called before the Bitmap image is ready to be used, in order to validate the size of the image
     * is not bigger than the size of the ImageView, if it is, the image is scaled down.
     *
     * @param options   The BitmapFactory.Options object.
     * @param reqWidth  The width of the ImageView.
     * @param reqHeight The height of the ImageView.
     *
     * @return int Returns the inSampleSize value to decode with.
     */
    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        // Check if the raw height and width are bigger than the required.
        if (height > reqHeight || width > reqWidth) {

            // The raw height and width are bigger than the required; Divide by half.
            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize > reqHeight && halfWidth / inSampleSize > reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }
}