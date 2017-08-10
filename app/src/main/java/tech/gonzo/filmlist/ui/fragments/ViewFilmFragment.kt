package tech.gonzo.filmlist.ui.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.*
import android.widget.*
import tech.gonzo.filmlist.R
import tech.gonzo.filmlist.asynctasks.APIImportTask
import tech.gonzo.filmlist.asynctasks.DownloadImageTask
import tech.gonzo.filmlist.asynctasks.SaveImageTask
import tech.gonzo.filmlist.database.FilmDBHandler
import tech.gonzo.filmlist.models.Film
import tech.gonzo.filmlist.utils.AppUtil
import tech.gonzo.filmlist.utils.Constants
import java.io.File

class ViewFilmFragment : Fragment(), DownloadImageTask.DownloadImageTaskListener, SaveImageTask.SaveImageTaskListener, APIImportTask.APIImportTaskListener {

    companion object {
        // Saved Instance State constants.
        private val SAVED_INSTANCE_STATE_FILM = "saved_instance_state_film"
        private val SAVED_INSTANCE_STATE_ACTION = "saved_instance_state_action"
        private val SAVED_STATE_API_TRACKING_BOOLEAN = "api_tracking"
        private val SAVED_STATE_API_TRACKING_BOOLEAN_POSTER_URI = "uri_tracking"

        // Share Message Type constants.
        private val ONLY_TITLE = 0
        private val TITLE_YEAR = 1
        private val TITLE_YEAR_DIRECTOR = 2
        private val TITLE_YEAR_DIRECTOR_RATING = 3
        private val TITLE_DIRECTOR_RATING = 4
        private val TITLE_DIRECTOR = 5
        private val TITLE_RATING = 6
        private val TITLE_YEAR_RATING = 7

        // Request Code constants.
        private val REQUEST_CODE_IMAGE_GET = 432

        // Bundle constants.
        val POSTER_IMAGE = "posterName"
        val FILM_TITLE = "filmTitle"
        val FILM_IMDB_ID = "filmImdbId"
    }

    // UI references.
    // private val textViewTitle: TextView by lazy { view?.findViewById(R.id.fragment_view_film_text_view_title) as TextView }
    // private lateinit var textViewTitle: TextView
    private var textViewTitle: TextView? = null
    private var textViewYear: TextView? = null
    private var textViewDirector: TextView? = null
    private var textViewPlot: TextView? = null
    private var textViewImdbRating: TextView? = null
    private var checkBoxSeenFilm: CheckBox? = null
    private var ratingBarUserRating: RatingBar? = null
    private var imageViewPoster: ImageView? = null
    private var progressDialogDownloadImage: ProgressDialog? = null
    private var progressDialogUpdateFilm: ProgressDialog? = null
    private var progressDialogSaveImage: ProgressDialog? = null

    // The Fragment Listeners.
    private var listener: OnFilmSavedListener? = null

    // FilmDBHandler.
    private var handler: FilmDBHandler? = null

    // The Film object.
    private var film: Film? = null

    // The Film's poster URI.
    private var posterUri: String? = null

    // The Film's poster Bitmap.
    private var thumbnail: Bitmap? = null

    // The User action that started the activity.
    private var action: Int = 0

    // Tracking boolean that checks if the initial API request has returned before the fragment was recreated.
    private var hasApiReturned: Boolean = false

    // Tracking boolean that checks if the updated poster URI is for a new poster.
    private var isPosterUriNew: Boolean = false

    /**
     * Called when a fragment is first attached to its context.
     * [.onCreate] will be called after this.

     * @param context The context hosting the fragment.
     */
    override fun onAttach(context: Context?) {
        super.onAttach(context)

        // Check if the context has implemented the OnFilmSavedListener listener.
        if (context is OnFilmSavedListener) {
            // The context has implemented the listener;
            // Initialize the OnFilmSavedListener.
            listener = context

        } else {
            // The context has not implemented the listener;
            // Throw an exception.
            throw RuntimeException(context!!.toString() + " must implement OnFilmSavedListener")
        }
    }

    /**
     * Called when the fragment is no longer attached to its activity.
     * This is called after onDestroy().
     */
    override fun onDetach() {
        super.onDetach()

        // Clear the listener.
        listener = null

        // Check if the progressDialogDownloadImage is not null;
        if (progressDialogDownloadImage != null) {

            // Call dismissDialog() to dismiss the ProgressDialog.
            dismissDialog(progressDialogDownloadImage)

            // Set dialog to null.
            progressDialogDownloadImage = null
        }

        // Check if the progressDialogSaveImage is not null;
        if (progressDialogSaveImage != null) {
            // Call dismissDialog() to dismiss the ProgressDialog.
            dismissDialog(progressDialogSaveImage)

            // Set dialog to null.
            progressDialogSaveImage = null
        }

        // Check if the progressDialogUpdateFilm is not null;
        if (progressDialogUpdateFilm != null) {
            // Call dismissDialog() to dismiss the ProgressDialog.
            dismissDialog(progressDialogUpdateFilm)

            // Set dialog to null.
            progressDialogUpdateFilm = null
        }
    }

    /**
     * Called to do initial creation of a fragment.
     * This is called after onAttach(Activity)
     * and before onCreateView(LayoutInflater, ViewGroup, Bundle).
     *
     * Note that this can be called while the fragment's activity
     * is still in the process of being created.
     * As such, you can not rely on things like
     * the activity's content view hierarchy being initialized at this point.
     * If you want to do work once the activity itself is created, see onActivityCreated(Bundle).
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state,
     * *                           this is the state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Preform default onCreate operations;
        // and get previous savedInstanceState if any are saved and reload them.
        super.onCreate(savedInstanceState)

        // Create FilmDBHandler object.
        handler = FilmDBHandler(activity)

        // Enable fragment OptionsMenu.
        setHasOptionsMenu(true)
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * This is optional, and non-graphical fragments can return null
     * (which is the default implementation).
     *
     * This will be called between onCreate(Bundle) and onActivityCreated(Bundle).
     *
     * If you return a View from here,
     * you will later be called in onDestroyView when the view is being released.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's UI should be attached to.
     * *                           The fragment should not add the view itself,
     * *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * *                           from a previous saved state as given here.
     *
     * @return View Return the View for the fragment's UI, or null.
     */
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater!!.inflate(R.layout.fragment_view_film, container, false)

        // Call initViews() to initialize views.
        initViews(rootView)

        // Check if the fragment is new or was recreated by checking if savedInstanceState is null.
        if (savedInstanceState == null) {
            // The fragment is new;
            // Initialize the tracking boolean to false;
            hasApiReturned = false
        } else {
            // The fragment was recreated;
            // Call useDataFromBundle() to re-add the data from the bundle.
            useDataFromBundle(savedInstanceState)
        }

        // Return root layout.
        return rootView
    }

    /**
     * Helper method to initialize views at startup.

     * @param rootView The root view of the fragment.
     */
    private fun initViews(rootView: View) {
        // Get the Film's Title TextView.
        textViewTitle = rootView.findViewById(R.id.fragment_view_film_text_view_title) as TextView

        // Get the Film's Year TextView.
        textViewYear = rootView.findViewById(R.id.fragment_view_film_text_view_year) as TextView

        // Get the Film's Director TextView.
        textViewDirector = rootView.findViewById(R.id.fragment_view_film_text_view_director) as TextView

        // Get the Film's Plot TextView.
        textViewPlot = rootView.findViewById(R.id.fragment_view_film_text_view_plot) as TextView

        // Get the Seen CheckBox.
        checkBoxSeenFilm = rootView.findViewById(R.id.fragment_view_film_check_box_seen_film) as CheckBox

        // Get the User's Rating RatingBar.
        ratingBarUserRating = rootView.findViewById(R.id.fragment_view_film_rating_bar_user_rating) as RatingBar

        // Get the IMDB Rating TextView.
        textViewImdbRating = rootView.findViewById(R.id.fragment_view_film_text_view_imdb_rating) as TextView

        // Get the Film's Poster ImageView.
        imageViewPoster = rootView.findViewById(R.id.fragment_view_film_image_view_poster) as ImageView
    }

    /**
     * Called when a fragment is recreated.
     * Extracts all saved data, re-saves it globally and re-sets it.
     *
     * @param savedInstanceState If the fragment is being re-created from
     * *                           a previous saved state, this is the state.
     */
    private fun useDataFromBundle(savedInstanceState: Bundle) {
        // Get the Film object from the bundle.
        film = savedInstanceState.getParcelable<Film>(SAVED_INSTANCE_STATE_FILM)

        // Get the URI tracking boolean from the bundle.
        isPosterUriNew = savedInstanceState.getBoolean(SAVED_STATE_API_TRACKING_BOOLEAN)

        // Get the API returned tracking boolean from the bundle.
        hasApiReturned = savedInstanceState.getBoolean(SAVED_STATE_API_TRACKING_BOOLEAN)

        // Check if the initial API was returned.
        if (hasApiReturned) {
            // Initial API was returned;
            // Get the Film's thumbnail from the bundle.
            thumbnail = savedInstanceState.getParcelable<Bitmap>(POSTER_IMAGE)

            // Set the thumbnail to the ImageView.
            imageViewPoster!!.setImageBitmap(thumbnail)

            // Set the Content Description to the ImageView.
            imageViewPoster!!.contentDescription = getString(R.string.content_description_film_poster)
        } else {
            // Initial API was not returned;
            // Get the User Action from the bundle.
            action = savedInstanceState.getInt(SAVED_INSTANCE_STATE_ACTION)
        }

        // Call setFilmInformation() to re-set film information.
        setFilmInformation()
    }

    /**
     * Called to ask the fragment to save its current dynamic state,
     * so it can later be reconstructed in a new instance if its process is restarted.
     *
     * If a new instance of the fragment later needs to be created,
     * the data you place in the Bundle here will be available in the Bundle given to
     * onCreate(Bundle), onCreateView(LayoutInflater, ViewGroup, Bundle), and onActivityCreated(Bundle).
     *
     * This corresponds to Activity.onSaveInstanceState(Bundle)
     * and most of the discussion there applies here as well.
     *
     * Note however: this method may be called at any time before onDestroy().
     * There are many situations where a fragment may be mostly torn down
     * (such as when placed on the back stack with no UI showing),
     * but its state will not be saved until its owning activity actually needs to save its state.
     *
     * @param outState Bundle in which to place your saved state.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        // Add the User Action to the saved state bundle.
        outState.putInt(SAVED_INSTANCE_STATE_ACTION, action)

        // Add the Film object to the saved state bundle.
        outState.putParcelable(SAVED_INSTANCE_STATE_FILM, film)

        // Add the Film's thumbnail to the saved state bundle.
        outState.putParcelable(POSTER_IMAGE, thumbnail)

        // Add the API returned tracking boolean to the saved state bundle.
        outState.putBoolean(SAVED_STATE_API_TRACKING_BOOLEAN, hasApiReturned)

        // Add the URI tracking boolean to the saved state bundle.
        outState.putBoolean(SAVED_STATE_API_TRACKING_BOOLEAN_POSTER_URI, isPosterUriNew)

        // Do default onSaveInstanceState operations.
        super.onSaveInstanceState(outState)
    }

    /**
     * Called to initialise the Fragment with the Film data.
     *
     * @param action The action that started the fragment.
     * @param film   The Film object which data is being extracted from.
     */
    fun initializeFragmentData(action: Int, film: Film) {
        // Save objects globally.
        this.action = action
        this.film = film

        // Call setFilmInformation() to set film information.
        setFilmInformation()
    }

    /**
     * Called when initializing film data.
     *
     * If the film is being updated, populate the TextViews with the
     * current film's data from the database.
     *
     * If the film data was imported from the internet,
     * populate the TextViews with the imported data.
     */
    private fun setFilmInformation() {
        // Call setTitleTextView() to set the film's Title.
        setTitleTextView()

        // Call setYearTextView() to set the film's Release Year.
        setYearTextView()

        // Call setDirectorTextView() to set the film's Director.
        setDirectorTextView()

        // Call setPlotTextView() to set the film's Plot.
        setPlotTextView()

        // Call setImdbRatingTextView() to set the film's IMDB Rating.
        setImdbRatingTextView()

        // Check if the action is not ACTION_UPDATE.
        if (action != Constants.ACTION_UPDATE) {
            // The action is not ACTION_UPDATE;
            // Call setSeenCheckBox() to set the film's Check Box.
            setSeenCheckBox()

            // Call setUserRatingRatingBar() to set the film's Rating Bar.
            setUserRatingRatingBar()
        }

        // Check if the image was already set.
        if (!hasApiReturned) {
            // The image was not already set;
            // Call setFilmPosterImageView() to set the film's Poster.
            setFilmPosterImageView()
        }
    }

    /**
     * Called to set the Film's Title to the TextView.
     */
    private fun setTitleTextView() {
        // Get the color from the resource file.
        val blackColor = ContextCompat.getColor(activity, R.color.colorTextBlack)

        // Get the "Title" resource.
        val titleWithoutData = getString(R.string.header_title)

        // Extract the "Title" information.
        val title = String.format(titleWithoutData, film!!.title)

        // Call setSpannableString() to set the header and value to different colors.
        setSpannableString(textViewTitle!!, title, titleWithoutData, blackColor)
    }

    /**
     * Called when a current field should have different colors for the header and the value.
     *
     * @param textView        The TextView to set the text to.
     * @param completeText    The film information for the current field
     * *                        filled with the relevant film data.
     * @param textWithoutData The field text with StringFormat keys and without film data.
     * @param color           The color which should be used for the header part.
     */
    private fun setSpannableString(textView: TextView, completeText: String, textWithoutData: String, color: Int) {
        // Get the length of the header (removing the 4 StringFormat keys).
        val maxLength = textWithoutData.length - 4

        // Create a SpannableString in order to set the header text to a different color.
        val fieldText = SpannableString(completeText)

        // Set a different color to the header.
        fieldText.setSpan(ForegroundColorSpan(color), 0, maxLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Set the field information to the TextView.
        textView.text = fieldText
    }

    /**
     * Called to set the Film's Release Year to the TextView.
     */
    private fun setYearTextView() {
        // Extract the "Year" information and set it to the TextView.
        textViewYear!!.text = String.format("%1\$s", film!!.year)
    }

    /**
     * Called to set the Film's Director(s) to the TextView.
     */
    private fun setDirectorTextView() {
        // Extract the "Director" information.
        var director = film!!.director

        // Check if the director value is not null.
        if (director.isNullOrEmpty()) {
            // Director value is not null;
            // Check if there is more than 1 director by checking if there is a comma in the name.
            if (director.contains(",")) {
                // There is more than 1 director;
                // Add a line break after each comma found.
                director = director.replace(", ".toRegex(), ",\n")
            }
        } else {
            // The Director value is null;
            // Let the user know no director info was found.
            director = getString(R.string.missing_info_no_director_found)
        }

        // Set the "Director" information to the TextView.
        textViewDirector!!.text = director
    }

    /**
     * Called to set the Film's Plot to the TextView.
     */
    private fun setPlotTextView() {
        // Extract the "Plot" information.
        var plot = film!!.plot

        // Check if the plot value is null.
        if (plot == null) {
            // No plot was found;
            // Let the user know no plot info was found.
            plot = getString(R.string.missing_info_no_plot_found)
        }

        // Set "Plot" information to the TextView.
        textViewPlot!!.text = plot
    }

    /**
     * Called to set the Film's IMDB Rating to the TextView.
     */
    private fun setImdbRatingTextView() {
        // Extract the "IMDB Rating" information and set it to the TextView.
        textViewImdbRating!!.text = String.format("%1\$s", film!!.imdbRating)
    }

    /**
     * Called to set the Seen value to the CheckBox.
     */
    private fun setSeenCheckBox() {
        // Extract the "Seen" information and set it to the CheckBox.
        checkBoxSeenFilm!!.isChecked = film!!.seen
    }

    /**
     * Called to set the User's Rating to the RatingBar.
     */
    private fun setUserRatingRatingBar() {
        // Extract the "User Rating" information and set it to the RatingBar.
        ratingBarUserRating!!.rating = film!!.userRating
    }

    /**
     * Called to set the Film's Poster URI to the ImageView.
     */
    private fun setFilmPosterImageView() {
        // Extract the "Poster Uri" information and save it globally.
        posterUri = film!!.posterExternalUri

        // Check if the Poster URI is empty.
        if (posterUri!!.isEmpty()) {
            // The Poster URI is empty;
            // Exit method.
            return
        }

        // Check what action was initiated.
        when (action) {
        // The activity was started from an Edit film request from MainActivity.
            Constants.ACTION_EDIT -> {
                // Set the poster image URI to the ImageView.
                imageViewPoster!!.setImageURI(Uri.parse(film!!.posterInternalUri))

                // Set the Content Description to the ImageView.
                imageViewPoster!!.contentDescription = getString(R.string.content_description_film_poster)
            }

        // The activity was started from the ImportFilmActivity.
            Constants.ACTION_IMPORT ->
                // Start the DownloadImageTask Async task;
                // The task will download the image and set it to the ImageView.
                DownloadImageTask(this).execute(posterUri)

        // The activity was started from an Update information request.
            Constants.ACTION_UPDATE ->
                // Check if the updated poster URI is not the same as the previous one.
                if (posterUri != film!!.posterInternalUri) {
                    // The updated poster URI is not the same as the previous one;
                    // Start the DownloadImageTask Async task;
                    // The task will download the image and set it to the ImageView.
                    DownloadImageTask(this).execute(posterUri)
                }

        // Do nothing.
            else -> {
            }
        }
    }

    /**
     * Initialize the contents of the Activity's standard options menu.
     * You should place your menu items in to menu.
     * For this method to be called, you must have first called setHasOptionsMenu.
     *
     * See Activity.onCreateOptionsMenu for more information.
     *
     * @param menu     The options menu in which you place your items.
     * @param inflater The MenuInflater object that can be used to inflate the menu items.
     */
    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        // Do default onCreateOptionsMenu operations.
        super.onCreateOptionsMenu(menu, inflater)

        // Inflate the menu items.
        inflater!!.inflate(R.menu.menu_view_film, menu)

        // Check if the film is already a film saved in the database
        // by checking if it has a timestamp value.
        if (film!!.timestamp == 0L) {
            // The film is not saved in the database;
            // Disable the update film option and hide it has the data is new.
            val actionUpdateInformation = menu!!.findItem(R.id.action_update_film_information).setEnabled(false)
            actionUpdateInformation.isVisible = false
            actionUpdateInformation.isEnabled = false
        }
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal processing happen
     * (calling the item's Runnable or sending a message to its Handler as appropriate).
     * You can use this method for any items for which you would like to do processing without
     * those other facilities.
     *
     * Derived classes should call through to the base class for it to perform
     * the default menu handling.
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to proceed, true to consume it here.
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        // Check which menu item was selected.
        when (item!!.itemId) {

        // Save Film was selected.
            R.id.action_save_film ->
                // Call beforeSavingSaveImage() to start the saving process.
                beforeSavingSaveImage()

        // Update Film Information was selected.
            R.id.action_update_film_information ->
                // Call showUpdateFilmDataConfirmationDialog() to update the film's data.
                showUpdateFilmDataConfirmationDialog()

        // Share was selected.
            R.id.action_share ->
                // Call shareFilm() to share the film's information.
                shareFilm()
        }

        // Close the options menu.
        return super.onOptionsItemSelected(item)
    }

    /**
     * Creates an AlertDialog asking the user if they wish to update the film's data.
     */
    private fun showUpdateFilmDataConfirmationDialog() {
        // Instantiate an AlertDialog.Builder with its constructor.
        val builder = AlertDialog.Builder(activity)

        // Chain together various setter methods to set the dialog characteristics.
        builder
                // Set the Dialog's title.
                .setTitle(R.string.dialog_update_film_data_title)

                // Set the Dialog's message.
                .setMessage(getString(R.string.dialog_update_film_data_msg))

                // Add the Dialog's Positive Button.
                .setPositiveButton(R.string.dialog_option_update_data) { _, _ ->
                    // Call AppUtil.isAppConnectedToNetwork() and check if there is a working network connection.
                    if (AppUtil.isAppConnectedToNetwork(context)) {
                        // A working network connection was found;
                        // Start the APIImportTask AsyncTask;
                        // Use the imdbID as the parameter.
                        APIImportTask(this).execute(film!!.imdbId)
                    } else {
                        // There is no working network connection;
                        // Call AppUtil.showNoNetworkConnectionDialog() to open a Confirmation Alert Dialog to the user.
                        AppUtil.showNoNetworkConnectionDialog(context)
                    }
                }

                // Add the Dialog's Negative Button.
                .setNegativeButton(R.string.dialog_option_cancel) { dialog, _ ->
                    // Dismiss the dialog.
                    dialog.dismiss()
                }

                // Creates an AlertDialog with the arguments supplied to this builder and immediately displays the dialog.
                .show()
    }

    /**
     * Called when the user selects the Share option.
     */
    private fun shareFilm() {
        // Create an Intent.
        val shareIntent = Intent()

        // Inform the intent that it is of a type Share (Action_SEND).
        shareIntent.action = Intent.ACTION_SEND

        // Check if the title field is empty.
        if (film!!.title.isNullOrEmpty()) {
            // Title field is empty;
            // Can't share information.
            Toast.makeText(activity, R.string.toast_error_cannot_share_film_tittle_missing, Toast.LENGTH_SHORT).show()
        } else {
            // Get year field empty status.
            val isYearEmpty = film!!.year == 0

            // Get director field empty status.
            val isDirectorEmpty = film!!.director.isNullOrEmpty()

            // Get rating bar empty status (zero value).
            val isRatingEmpty = ratingBarUserRating!!.rating == 0f

            // Get the typeOfSharedMessage value from the getTypeOfSharedMessage method.
            val typeOfSharedMessage = getTypeOfSharedMessage(isYearEmpty, isDirectorEmpty, isRatingEmpty)

            // Get the Intent message from the getSharedIntentMessage method.
            val intentMessage = getSharedIntentMessage(typeOfSharedMessage)

            // Attach the information passed to the Intent.
            shareIntent.putExtra(Intent.EXTRA_TEXT, intentMessage)

            // Set the type of the information attached.
            shareIntent.type = "text/plain"

            // Set the Intent to seek applications able to handle text type shares;
            // Set the title of the Intent;
            // Start the Intent.
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_with_title)))
        }
    }

    /**
     * Checks the Year, Director and Rating fields for data,
     * and sets the Intent message value depending on which fields have information.
     *
     * @param isYearEmpty     The status of the Year field - empty or not.
     * @param isDirectorEmpty The status of the Director field  - empty or not.
     * @param isRatingEmpty   The status of the Rating field - empty or not.
     *
     * @return int Returns The type of message to create.
     */
    private fun getTypeOfSharedMessage(isYearEmpty: Boolean, isDirectorEmpty: Boolean, isRatingEmpty: Boolean): Int {

        val typeOfMessage = isYearEmpty && isDirectorEmpty && isRatingEmpty

        val message: Int

        when (typeOfMessage) {
        // All fields have data;
            (!isYearEmpty && !isDirectorEmpty && !isRatingEmpty) -> message = TITLE_YEAR_DIRECTOR_RATING

        // Only the Year and Rating fields have data;
            (!isYearEmpty && isDirectorEmpty && !isRatingEmpty) -> message = TITLE_YEAR_RATING

        // Only the Year and Director fields have data;
            (!isYearEmpty && !isDirectorEmpty && isRatingEmpty) -> message = TITLE_YEAR_DIRECTOR

        // Only the Year field has data;
            (!isYearEmpty && isDirectorEmpty && isRatingEmpty) -> message = TITLE_YEAR

        // Only the Director and Rating fields have data;
            (isYearEmpty && !isDirectorEmpty && !isRatingEmpty) -> message = TITLE_DIRECTOR_RATING

        // Only the Director field has data;
            (isYearEmpty && !isDirectorEmpty && isRatingEmpty) -> message = TITLE_DIRECTOR

        // Only the Rating field has data;
            (isYearEmpty && isDirectorEmpty && !isRatingEmpty) -> message = TITLE_RATING

        // All fields are empty;
            (isYearEmpty && isDirectorEmpty && isRatingEmpty) -> message = ONLY_TITLE

        // Default in case of error.
            else -> message = ONLY_TITLE
        }

        return message
    }

    /**
     * Creates the correct message to share depending on what value was received.
     *
     * @param typeOfSharedMessage The type of message to create.
     *
     * @return String The Intent message to share.
     */
    private fun getSharedIntentMessage(typeOfSharedMessage: Int): String {
        // Get film title.
        val title = film!!.title

        // Get release year.
        val year = film!!.year.toString()

        // Get director(s).
        val director = film!!.director

        // Get rating number.
        val rating = ratingBarUserRating!!.rating.toString()

        // Create an empty Intent message to be populated next.
        val intentMessage: String

        // Check which message to send.
        when (typeOfSharedMessage) {
        // Create a message with only the Title information.
            ONLY_TITLE -> intentMessage = String.format(getString(R.string.share_msg_only_title), title)

        // Create a message with the Title and Year information.
            TITLE_YEAR -> intentMessage = String.format(getString(R.string.share_msg_title_year), title, year)

        // Create a message with the Title, Year and Director information.
            TITLE_YEAR_DIRECTOR -> intentMessage = String.format(getString(R.string.share_msg_title_year_director), title, year, director)

        // Create a message with the Title, Year, Director and Rating information.
            TITLE_YEAR_DIRECTOR_RATING -> intentMessage = String.format(getString(R.string.share_msg_title_year_director_rating), title, year, director, rating)

        // Create a message with the Title, Director and Rating information.
            TITLE_DIRECTOR_RATING -> intentMessage = String.format(getString(R.string.share_msg_title_director_rating), title, director, rating)

        // Create a message with the Title and Director information.
            TITLE_DIRECTOR -> intentMessage = String.format(getString(R.string.share_msg_title_director), title, director)

        // Create a message with the Title and Rating information.
            TITLE_RATING -> intentMessage = String.format(getString(R.string.share_msg_title_rating), title, rating)

        // Create a message with the Title, Year and Rating information.
            TITLE_YEAR_RATING -> intentMessage = String.format(getString(R.string.share_msg_title_year_rating), title, year, rating)

        // Create a message with only the Title information.
            else -> intentMessage = String.format(getString(R.string.share_msg_only_title), title)
        }
        return intentMessage
    }

    /**
     * Called when the Save button was clicked.
     * Save process steps:
     * - Check if the poster URI is null or not;
     * -- Check if the poster URI is empty or not.
     * --- If there is a URI, check if it came from Imported film or an Edit;
     * ---- If it came from an Edit, check if the saved URI is equal to the URI in the EditText;
     * ----- If they aren't equal, start the SaveImageTask AsyncTask and save the poster.
     * ----- If they are equal, call the saveFilm method.
     * ---- If it came from a New or Imported film, start the SaveImageTask Async task
     * and save the poster.
     * -- If the URI is empty, call the saveFilm method.
     * - If there isn't a URI, call the saveFilm method.
     */
    private fun beforeSavingSaveImage() {
        // Check if the URI is not null or empty.
        if (!posterUri.isNullOrEmpty()) {
            // The URI is not null or empty;
            // Create a new bundle.
            val bundle = Bundle()

            // Add data to the bundle.
            bundle.putParcelable(POSTER_IMAGE, thumbnail)
            bundle.putString(FILM_TITLE, film!!.title)
            bundle.putString(FILM_IMDB_ID, film!!.imdbId)

            // Check if the activity started from an edit or new film intent request.
            when (action) {

            // The Film object being saved had its data imported.
                Constants.ACTION_IMPORT ->
                    // Start the SaveImageTask which will Save the poster
                    // image shown in the ImageView on the device
                    // and set the new device URI to the EditText.
                    SaveImageTask(activity, this).execute(bundle)

            // The Film object being saved had its data updated via re-import.
                Constants.ACTION_UPDATE ->
                    // Check if the updated URI was for a new poster.
                    if (isPosterUriNew) {
                        // The Updated URI was a new poster;
                        // Start the SaveImageTask which will Save the poster
                        // image shown in the ImageView on the device
                        // and set the new device URI to the EditText.
                        SaveImageTask(activity, this).execute(bundle)
                    } else {
                        // The Updated URI is the same as the previously saved one;
                        // Save film.
                        saveFilm()
                    }

                else ->
                    // Save film.
                    saveFilm()
            }
        } else {
            // There URI is null or empty;
            // Save film.
            saveFilm()
        }
    }

    /**
     * The Save button was clicked.
     * Save the film information into the SQL database.
     */
    private fun saveFilm() {
        // Call getFilmObject() to update the film object.
        getFilmObject()

        // Check if the activity started from an edit or new film intent request.
        when (action) {
        // The Film object being saved had its data imported.
            Constants.ACTION_IMPORT -> {
                // Insert the Film object into the database.
                handler!!.addFilm(film!!)

                // Check if the listener is not null.
                if (listener != null) {
                    // The listener is not null;
                    // Attach film object to the listener.
                    // Attach the ACTION_IMPORT to the listener.
                    listener!!.onFilmSaved(film!!, Constants.ACTION_IMPORT)
                }
            }

        // The Film object being saved is a previously saved film
        // that had its data edited, or updated via import.
            Constants.ACTION_EDIT, Constants.ACTION_UPDATE -> {
                // Get the film's ID and set it to the new object.
                film!!.id = film!!.id

                // Update the film information in the database.
                handler!!.updateFilm(film!!)

                // Check if the listener is not null.
                if (listener != null) {
                    // The listener is not null;
                    // Attach film object to the listener.
                    // Attach the ACTION_UPDATE to the listener.
                    listener!!.onFilmSaved(film!!, Constants.ACTION_UPDATE)
                }
            }

        // Do nothing.
            else -> {
            }
        }
    }

    /**
     * Called when a new film object should be created.
     */
    private fun getFilmObject() {
        // Create a timestamp object to be populated next.
        val timestamp: Long

        when (action) {

        // The film is new.
            Constants.ACTION_IMPORT ->
                // Get current timestamp.
                timestamp = System.currentTimeMillis()

        // The film was already in the database.
            else ->
                // Get saved timestamp.
                timestamp = film!!.timestamp
        }

        // Set the Timestamp.
        film!!.timestamp = timestamp

        // Get and set the Seen Status.
        film!!.seen = checkBoxSeenFilm!!.isChecked

        // Get and set the User Rating.
        film!!.userRating = ratingBarUserRating!!.rating
    }

    /**
     * Receive the result from a previous call to startActivityForResult(Intent, int).
     * This follows the related Activity API as described
     * there in Activity.onActivityResult(int, int, Intent).
     *
     * @param requestCode The integer request code originally supplied to startActivityForResult(),
     * *                    allowing you to identify who this result came from.
     * @param resultCode  The integer result code returned by the child activity
     * *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     * *                    (various data can be attached to Intent "extras").
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Here we need to check if the activity that was triggered was the Image Gallery.
        // If it is, the requestCode will match the REQUEST_IMAGE_GET value.
        // If the resultCode is RESULT_OK and there is some data we know that an image was picked.
        if (requestCode == REQUEST_CODE_IMAGE_GET && resultCode == Activity.RESULT_OK && data != null) {
            // Get the address of the image on the SD card.
            val fullPhotoUri = data.data

            // Create a column String array.
            val filePath = arrayOf(MediaStore.Images.Media.DATA)

            // Create a cursor.
            val cursor = activity.contentResolver.query(fullPhotoUri, filePath, null, null, null)

            // Check that the cursor is not null;
            if (cursor != null) {
                // Cursor is not null;
                // Set cursor as the first entry.
                cursor.moveToFirst()

                // Get the string of the image path from the cursor.
                val pathName = cursor.getString(cursor.getColumnIndex(filePath[0]))

                // Close the cursor.
                cursor.close()

                // Save the URI globally.
                posterUri = fullPhotoUri.toString()

                // Call AppUtil.decodeSampledBitmapFromFile() to create a thumbnail with a limit on the image size.
                thumbnail = AppUtil.decodeSampledBitmapFromFile(pathName, imageViewPoster!!.width, imageViewPoster!!.height)

                // Set the Bitmap to the ImageView.
                imageViewPoster!!.setImageBitmap(thumbnail)
            }
        }
    }

    /**
     * Called when the task starts.
     *
     * @param task The type of task that started operation.
     */
    override fun onImageTaskStarted(task: Int) {
        // Check which task started the progress dialog.
        when (task) {

            Constants.TASK_DOWNLOAD_IMAGE -> {
                // Create a new Progress Dialog.
                progressDialogDownloadImage = ProgressDialog(activity)

                // Set the dialog's style (widget) to use.
                progressDialogDownloadImage!!.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)

                // Set the ProgressDialog as cancelable.
                progressDialogDownloadImage!!.setCancelable(true)

                // Set the dialog's message.
                progressDialogDownloadImage!!.setMessage(getString(R.string.dialog_progress_downloading_poster))

                // Set the ProgressDialog progress to zero (0).
                progressDialogDownloadImage!!.progress = 0
            }

            Constants.TASK_SAVE_IMAGE -> {
                // Create a new Progress Dialog.
                progressDialogSaveImage = ProgressDialog(activity)

                // Set the dialog's message.
                progressDialogSaveImage!!.setMessage(getString(R.string.dialog_progress_saving_poster))

                // Show Progress Dialog.
                progressDialogSaveImage!!.show()
            }

        // Do nothing.
            else -> {
            }
        }
    }

    /**
     * Called when the file size is known and is passed to the Progress Dialog.
     *
     * @param contentLength The size of the file being downloaded.
     */
    override fun onDownloadImageTaskSetMaxProgress(contentLength: Int) {
        // Security check (in case of rotation) if the Progress Dialog is not null.
        if (progressDialogDownloadImage != null) {
            // Progress Dialog is not null; Set the max size.
            progressDialogDownloadImage!!.max = contentLength
        }
    }

    /**
     * Called when the download progress should be updated in the Progress Dialog.
     *
     * @param progress The totalBytesRead sent by downloadImage every 2K downloaded (2048 bytes).
     */
    override fun onDownloadImageProgressUpdate(vararg progress: Int) {
        // Security check (in case of rotation) if the Progress Dialog is not null.
        if (progressDialogDownloadImage != null) {
            // Progress Dialog is not null; Show ProgressDialog first (might be buggy if set after progress).
            progressDialogDownloadImage!!.show()

            // Set download progress to ProgressDialog.
            progressDialogDownloadImage!!.progress = progress[0]
        }
    }

    /**
     * Called after the poster image has been downloaded.
     *
     * @param poster The poster image as a Bitmap object.
     */
    override fun getPoster(poster: Bitmap?) {
        // Check if the image is not null.
        if (poster != null) {
            // The image is not null; Save the poster to a global Bitmap variable.
            thumbnail = poster

            // Set the tracking boolean to true.
            hasApiReturned = true

            // Set the image to the ImageView.
            imageViewPoster!!.setImageBitmap(thumbnail)

            // Call dismissDialog() to dismiss the ProgressDialog.
            dismissDialog(progressDialogDownloadImage)
        } else {
            // The image is null;
            // Call dismissDialog() to dismiss the ProgressDialog.
            dismissDialog(progressDialogDownloadImage)

            // Create a Toast to inform the user that there were problems in downloading the poster.
            Toast.makeText(activity, R.string.toast_error_problem_downloading_image, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Called after the poster image is saved locally on the device.
     *
     * @param file The File of the image poster saved on the device.
     */
    override fun getFile(file: File?) {
        // Call dismissDialog() to dismiss the ProgressDialog.
        dismissDialog(progressDialogSaveImage)

        // Check if the file is not null.
        if (file != null) {
            // Set the file to the ImageView.
            imageViewPoster!!.setImageURI(Uri.fromFile(file))

            // Set the Content Description to the ImageView.
            imageViewPoster!!.contentDescription = getString(R.string.content_description_film_poster)

            // Set the new device path to the URI EditText.
            posterUri = file.path

            // Set the Film's internal poster URI.
            film!!.posterInternalUri = posterUri

            // Once the new image is saved, call the saveFilm method.
            saveFilm()

        } else {
            // Create a Toast to inform the user that there were problems in saving the image.
            Toast.makeText(activity, R.string.toast_error_cannot_save_film_problem_saving_image, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Called when the task starts.
     */
    override fun onAPIImportTaskStarted() {
        // Create a new Progress Dialog.
        progressDialogUpdateFilm = ProgressDialog(activity)

        // Set the Dialog's message.
        progressDialogUpdateFilm!!.setMessage(getString(R.string.dialog_progress_getting_updated_information))

        // Show Progress Dialog.
        progressDialogUpdateFilm!!.show()
    }

    /**
     * Called when the task finished with an error message.

     * @param errorMessage The error message returned from the server.
     */
    override fun onAPITaskFinishedWithError(errorMessage: String) {
        // Security check if the layout view is available.
        if (view != null) {
            // The view is available; Create a Snackbar informing the user of the error received.
            Snackbar.make(view!!, errorMessage, Snackbar.LENGTH_LONG).show()
        }

        // Call dismissDialog() to dismiss the ProgressDialog.
        dismissDialog(progressDialogUpdateFilm)
    }

    /**
     * Called after a film's updated data has been received.
     *
     * @param requestedFilm The Film object with the updated data.
     */
    override fun getSelectedFilm(requestedFilm: Film?) {
        if (requestedFilm != null) {
            // Update the film's data.
            film!!.title = requestedFilm.title
            film!!.year = requestedFilm.year
            film!!.director = requestedFilm.director
            film!!.plot = requestedFilm.plot
            film!!.imdbRating = requestedFilm.imdbRating

            // Check if the updated poster URI is the same as the current one.
            if (film!!.posterExternalUri == requestedFilm.posterExternalUri) {
                // The updated poster URI is the same as the current one;
                // Set tracking boolean to false;
                isPosterUriNew = false
            } else {
                // The updated poster URI is not the same as the current one;
                // Set the tracking boolean to true;
                film!!.posterExternalUri = requestedFilm.posterExternalUri
                isPosterUriNew = true
            }

            // Call initializeFragmentData() and update fields with new information.
            initializeFragmentData(Constants.ACTION_UPDATE, film!!)
        }

        // Dismiss ProgressDialog when the Async Task is done.
        progressDialogUpdateFilm!!.dismiss()
    }

    /**
     * Called when the dialog should be closed.
     *
     * @param dialog The ProgressDialog that is running.
     */
    private fun dismissDialog(dialog: ProgressDialog?) {
        // Dismiss dialog.
        dialog?.dismiss()
    }

    /**
     * Interface that handles saving film events.
     */
    interface OnFilmSavedListener {

        /**
         * Called when a Film object was saved.
         *
         * @param film   The Film object saved.
         * @param action The User action that initiated the process.
         */
        fun onFilmSaved(film: Film, action: Int)
    }
}