package tech.gonzo.filmlist.controller

import android.support.v7.widget.RecyclerView
import tech.gonzo.filmlist.models.Film
import tech.gonzo.filmlist.models.ShortFilm
import tech.gonzo.filmlist.ui.viewholders.FilmViewHolder

/**
 * RecyclerView Adapter used to populate film data in the lists.
 */
abstract class FilmAdapter : RecyclerView.Adapter<FilmViewHolder>() {

    // ArrayList holding the adapter data.
    var list: ArrayList<ShortFilm> = ArrayList()
        private set

    /**
     * Returns the total number of items in the data set hold by the adapter.
     *
     * @return int The total number of items in this adapter.
     */
    override fun getItemCount(): Int {
        // Returns zero (0) if the Films array is null,
        // returns its actual size if it has entries.
        return if (list.isEmpty()) 0 else list.size
    }

    /**
     * Called when a Film has been removed from the list.
     *
     * @param position The Film's position in the list.
     */
    fun removeFilm(position: Int) {
        // Remove the Film from the ArrayList.
        list.removeAt(position)

        // Notify adapter the entry at the current position has been removed.
        notifyItemRemoved(position)
    }

    /**
     * Called when the UNDO option has been selected
     * after selecting the Delete Film option.
     *
     * @param film             The previously deleted Film.
     * @param previousPosition The previously deleted Film's position in the list.
     */
    fun undoRemoveFilm(film: Film, previousPosition: Int) {
        // Re-Add the previous Film to its old position in the ArrayList.
        list.add(previousPosition, film)

        // Notify adapter the data has been changed.
        notifyItemInserted(previousPosition)
    }

    /**
     * Called when the user requested to delete his saved list.
     */
    fun clearList() {
        // The ArrayList is not null;
        // Clear the ArrayList.
        list.clear()

        // Notify adapter the data has been changed.
        notifyDataSetChanged()
    }

    /**
     * Called when the UNDO option has been selected after selecting the Clear List option.
     *
     * @param backupList The previously deleted ShortFilm ArrayList.
     */
    fun undoClearList(backupList: ArrayList<out ShortFilm>) {
        // Re-Add the previous list.
        list = backupList as ArrayList<ShortFilm>

        // Notify adapter the data has been changed.
        notifyDataSetChanged()
    }

    /**
     * Called when changing ArrayLists in the adapter.
     *
     * @param films The new ArrayList.
     */
    fun swapArray(films: ArrayList<out ShortFilm>) {
        // Save the ArrayList globally.
        this.list = films as ArrayList<ShortFilm>

        // Notify adapter the data has been changed.
        notifyDataSetChanged()
    }
}