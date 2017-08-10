package tech.gonzo.filmlist.controller

import android.view.View
import tech.gonzo.filmlist.models.Film

/**
 * Created by Gonny on 2/2/2017.
 * -
 */
interface OnFilmSelectedListener {

    /**
     * Called when an item in the list has been clicked.
     *
     * @param view    The View that was clicked.
     * @param film The Film object selected.
     */
    fun onItemClick(view: View, film: Film)

    /**
     * Called when an item in the list has been long clicked.
     *
     * @param film     The Film object selected.
     * @param position The current position in the list of the entry.
     */
    fun onItemLongClick(film: Film, position: Int)

    /**
     * Called when a ToggleButton has been clicked.
     *
     * @param film The Film object of the current entry.
     */
    fun onToggleClick(film: Film)
}