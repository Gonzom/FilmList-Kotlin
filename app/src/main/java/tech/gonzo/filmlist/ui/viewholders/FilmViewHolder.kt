package tech.gonzo.filmlist.ui.viewholders

import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * Created by Gonny on 2/2/2017.
 * -
 */
abstract class FilmViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {

    companion object {
        // Time between clicks allowed constant.
        private val CLICK_TIME_INTERVAL: Long = 300
    }

    /**
     * Called to verify if the time between the user clicks is valid,
     * as to prevent multiple clicks on the same item.
     *
     * @return true if the time between clicks is valid, false if it is too short.
     */
    internal fun isClickIntervalValid(lastClickTime: Long): Boolean {
        // Get current time.
        val now = System.currentTimeMillis()

        // Check if current click time minus the previous click time
        // is less than the minimum time allowed.
        return now - lastClickTime >= CLICK_TIME_INTERVAL
    }
}