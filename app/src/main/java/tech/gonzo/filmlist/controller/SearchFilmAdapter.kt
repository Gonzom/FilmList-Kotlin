package tech.gonzo.filmlist.controller

import android.view.LayoutInflater
import android.view.ViewGroup
import tech.gonzo.filmlist.R
import tech.gonzo.filmlist.ui.viewholders.FilmSearchViewHolder
import tech.gonzo.filmlist.ui.viewholders.FilmViewHolder

/**
 * RecyclerView Adapter used in the ImportFilmFragment search list.
 *
 * Created by Gonny on 21/3/2017.
 *
 * @param listener The adapter listener.
 */
class SearchFilmAdapter(val listener: OnShortFilmSelectedListener) : FilmAdapter() {

    /**
     * Called when RecyclerView needs a new RecyclerView.ViewHolder
     * of the given type to represent an item.
     *
     * This new ViewHolder should be constructed with a new View
     * that can represent the items of the given type.
     * You can either create a new View manually or inflate it from an XML layout file.
     *
     * The new ViewHolder will be used to display items of the adapter
     * using onBindViewHolder(RecyclerView.ViewHolder, int, List).
     *
     * Since it will be re-used to display different items in the data set,
     * it is a good idea to cache references to sub views of the View
     * to avoid unnecessary View.findViewById(int) calls.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     *
     * @return FilmHolder Returns A new ViewHolder that holds a View of the given view type.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilmViewHolder {
        // Create a LayoutInflater.
        val inflater = LayoutInflater.from(parent.context)

        // Inflate layout.
        val view = inflater.inflate(R.layout.row_item_search, parent, false)

        // Return a FilmSearchViewHolder with the inflated layout.
        return FilmSearchViewHolder(view, listener)
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * This method should update the contents of the RecyclerView.ViewHolder.itemView
     * to reflect the item at the given position.
     *
     * Note that unlike android.widget.ListView, RecyclerView will not call this method again
     * if the position of the item changes in the data set unless the item itself is invalidated
     * or the new position cannot be determined.
     * For this reason, you should only use the position parameter while acquiring the related data
     * item inside this method and should not keep a copy of it.
     * If you need the position of an item later on (e.g. in a click listener),
     * use RecyclerView.ViewHolder.getAdapterPosition() which will have the updated adapter position.
     * Override onBindViewHolder(RecyclerView.ViewHolder, int, List) instead
     * if Adapter can handle efficient partial bind.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the item
     * *                 at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    override fun onBindViewHolder(holder: FilmViewHolder, position: Int) {
        // Call bindView() to bind the data to the view.
        (holder as FilmSearchViewHolder).bindView(list[position])
    }
}