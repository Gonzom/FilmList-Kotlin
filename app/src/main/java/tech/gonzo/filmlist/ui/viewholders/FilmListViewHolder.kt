package tech.gonzo.filmlist.ui.viewholders

import android.net.Uri
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.view.View
import android.widget.*
import tech.gonzo.filmlist.R
import tech.gonzo.filmlist.controller.OnFilmSelectedListener
import tech.gonzo.filmlist.models.Film

/**
 * Created by Gonny on 2/2/2017.
 *
 * @param itemView The layout used to bind data to.
 */
class FilmListViewHolder(itemView: View, private val listener: OnFilmSelectedListener?) : FilmViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {

    // Register views.
    private val textViewTitle: TextView = itemView.findViewById(R.id.row_item_main_text_view_title) as TextView
    private val textViewDirector: TextView = itemView.findViewById(R.id.row_item_main_text_view_directer) as TextView
    private val toggleButtonSeen: ToggleButton = itemView.findViewById(R.id.row_item_main_toggle_button) as ToggleButton
    private val ratingBarUserRating: RatingBar = itemView.findViewById(R.id.row_item_main_rating_bar) as RatingBar
    private val imageViewPoster: ImageView = itemView.findViewById(R.id.row_item_main_image_view_poster) as ImageView

    // The Film object.
    private lateinit var film: Film

    // Initialize the last click time.
    private var lastClickTime = System.currentTimeMillis()

    init {
        // Register an OnClickListener to the ToggleButton.
        toggleButtonSeen.setOnClickListener(this)

        // Register an OnClickListener to the Layout.
        itemView.setOnClickListener(this)

        // Register an OnLongClickListener to the Layout.
        itemView.setOnLongClickListener(this)
    }

    /**
     * Called when the current entry in the ArrayList binds to the given layout.
     *
     * @param film The current Film object used to bind data.
     */
    fun bindView(film: Film) {
        // Get the current place.
        this.film = film

        // Call setTitleTextView() to set the film's title TextView.
        setTitleTextView(film)

        // Set the Film's Director to the Director TextView.
        textViewDirector.text = film.director

        // Set the Seen value to the ToggleButton.
        toggleButtonSeen.isChecked = film.seen

        // Call setPosterImageView() to set the film's poster ImageView.
        setPosterImageView(film)

        // Set the User Rating to the RatingBar.
        ratingBarUserRating.rating = film.userRating
    }

    /**
     * Called to set the Film's title TextView.
     *
     * @param film The current Film object used to bind data.
     */
    private fun setTitleTextView(film: Film) {
        // Get the year value.
        val year = film.year

        // Check if the year is equal to zero (0);
        // Which means no year was set.
        if (year != 0) {
            // Year wasn't equal to zero;
            // Set year string to the year value.
            val yearAsString = year.toString()

            // Create a SpannableStringBuilder in order to set year text to a different size.
            val filmAndYear = SpannableStringBuilder()

            // Add Title value.
            filmAndYear.append(film.title)

            // Get the title length.
            val start = filmAndYear.length

            // Add Year value.
            filmAndYear.append(" (").append(yearAsString).append(")")

            // Set a different size to the year value.
            filmAndYear.setSpan(AbsoluteSizeSpan(16, true), start, filmAndYear.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

            // Set the Film's Title and Release Year to the Title TextView.
            textViewTitle.text = filmAndYear
        } else {
            // Year was equal to zero;
            // Set the Film's Title to the Title TextView.
            textViewTitle.text = film.title
        }
    }

    /**
     * Called to set the Film's poster ImageView.
     *
     * @param film The current Film object used to bind data.
     */
    private fun setPosterImageView(film: Film) {
        // Get the URI string from the cursor.
        val posterUri = film.posterInternalUri

        // Check if the URI is empty.
        if (posterUri.isNullOrEmpty()) {
            // The URI is empty;
            // Set default image to the ImageView.
            imageViewPoster.setImageResource(android.R.drawable.ic_menu_report_image)
        } else {
            // The URI is not empty;
            // Set the URI to the ImageView.
            imageViewPoster.setImageURI(Uri.parse(posterUri))

            // Set the Content Description to the ImageView.
            imageViewPoster.contentDescription = itemView.context.getString(R.string.content_description_film_poster)
        }
    }

    override fun onClick(v: View) {
        // Check if the time between clicks is valid.
        if (isClickIntervalValid(lastClickTime)) {
            // Time between clicks is valid;
            // Save the new click time.
            lastClickTime = System.currentTimeMillis()

            // Check which view was clicked on.
            when (v.id) {

            // The Seen/Not seen ToggleButton was clicked.
                R.id.row_item_main_toggle_button ->
                    // Call onSeenToggleChanged() to handle seen changes.
                    onSeenToggleChanged()

            // The Film object was clicked.
                else ->
                    // Pass the film to the Listener.
                    listener?.onItemClick(v, film)
            }
        }
    }

    /**
     * Called when the Seen/Not seen ToggleButton was clicked.
     */
    private fun onSeenToggleChanged() {
        // Check new seen value.
        val seenChanged = toggleButtonSeen.isChecked

        // Set the new seen value.
        film.seen = seenChanged

        // Check if the listener is not null.
        if (listener != null) {
            // The listener is not null;
            // call onToggleClick() to handle ToggleButton click events.
            listener.onToggleClick(film)

            // Get the current text shown on the ToggleButton.
            val seen = toggleButtonSeen.text.toString().toLowerCase()

            // Create the string
            val toast = String.format(itemView.context.getString(R.string.toast_film_now_marked_as_seen), film.title, seen)

            // Generate a Toast to inform the user that the film has been updated.
            Toast.makeText(itemView.context, toast, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Called when a view has been clicked and held.
     *
     * @param view The view that was clicked and held.
     *
     * @return boolean Returns true if the callback consumed the long click, false otherwise.
     */
    override fun onLongClick(view: View): Boolean {
        // Check if the listener is not null.
        listener?.onItemLongClick(film, adapterPosition)
        return true
    }
}