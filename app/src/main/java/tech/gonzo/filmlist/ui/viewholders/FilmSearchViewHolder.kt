package tech.gonzo.filmlist.ui.viewholders

import android.view.View
import android.widget.TextView
import tech.gonzo.filmlist.R
import tech.gonzo.filmlist.controller.OnShortFilmSelectedListener
import tech.gonzo.filmlist.models.ShortFilm

/**
 * Created by Gonny on 2/2/2017.
 * -
 */
class FilmSearchViewHolder(itemView: View, private val listener: OnShortFilmSelectedListener?) : FilmViewHolder(itemView), View.OnClickListener {

    // Register views.
    private val textViewSearchResults: TextView = itemView.findViewById(R.id.row_item_main_text_view_search_results) as TextView

    // The Film object.
    private lateinit var film: ShortFilm

    // Initialize the last click time.
    private var lastClickTime = System.currentTimeMillis()

    init {
        // Register an OnClickListener to the Layout.
        itemView.setOnClickListener(this)
    }

    /**
     * Called when the current entry in the ArrayList binds to the given layout.
     *
     * @param film The current ShortFilm object used to bind data.
     */
    fun bindView(film: ShortFilm) {
        // Get the current place.
        this.film = film

        // Set the Film's Title and Release Year to the Search Results TextView.
        textViewSearchResults.text = String.format("${film.title} (${film.year})")
    }

    override fun onClick(v: View) {
        // Check if the time between clicks is valid.
        if (!isClickIntervalValid(lastClickTime)) {
            // Time is between clicks is too short;
            // Do nothing.
            return
        }

        // Time between clicks is ok;
        // Save the new click time.
        lastClickTime = System.currentTimeMillis()

        // Pass the Film to the Listener.
        listener?.onItemClick(film)
    }
}