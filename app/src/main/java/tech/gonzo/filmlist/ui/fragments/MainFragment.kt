package tech.gonzo.filmlist.ui.fragments

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.AsyncTask
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.BaseTransientBottomBar
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.*
import android.widget.TextView
import android.widget.Toast
import tech.gonzo.filmlist.R
import tech.gonzo.filmlist.controller.OnFilmSelectedListener
import tech.gonzo.filmlist.controller.SavedFilmAdapter
import tech.gonzo.filmlist.database.FilmDBHandler
import tech.gonzo.filmlist.models.Film
import tech.gonzo.filmlist.ui.views.DividerItemDecoration
import tech.gonzo.filmlist.ui.views.EmptyRecyclerView
import tech.gonzo.filmlist.utils.Constants
import java.io.File
import java.util.*

/**
 * The main fragment, containing the list of the films the user saved.
 */
class MainFragment : Fragment(), PopupMenu.OnMenuItemClickListener, OnFilmSelectedListener {

    companion object {
        // SharedPreferences constants.
        private val SORT_KEY = "sortKey"
        private val SORT_ORDER = "sortOrder"

        // Dialog constants.
        private val DIALOG_OPTION_VIEW = 0
        private val DIALOG_OPTION_DELETE = 1
        private val DIALOG_OPTION_CANCEL = 2

        // Saved instance state constants.
        private val SAVED_STATE_ARRAY_LIST = "saved_state_array_list"
        private val SAVED_STATE_SORTING_KEY = "saved_state_sorting_key"
        private val SAVED_STATE_SORTING_ORDER = "saved_state_sorting_order"
        private val SAVED_STATE_TASK = "saved_state_task"
    }

    // UI references.
    private var recyclerView: EmptyRecyclerView? = null
    private var textViewSorting: TextView? = null

    // SharedPreferences object.
    private var sharedPreferences: SharedPreferences? = null

    // The Fragment Listener.
    private var listener: OnFilmSelectedListener? = null

    // RecyclerView Adapter.
    private var adapter: SavedFilmAdapter? = null

    // Database Handler.
    private var handler: FilmDBHandler? = null

    // The Film object.
    private lateinit var currentFilm: Film

    // The current position in the list.
    private var currentFilmPosition: Int = 0

    // The current Sorting Key.
    private var sortingKey: String? = null

    // The current Sorting Order.
    private var sortingOrder: String? = null

    // Tracking boolean to check if the AsyncTask finished during first loading or if it was interrupted
    private var hasTaskFinished = false

    /**
     * Called when a fragment is first attached to its context.
     * [.onCreate] will be called after this.
     *
     * @param context The context hosting the fragment.
     */
    override fun onAttach(context: Context?) {
        super.onAttach(context)

        // Check if the context has implemented the OnFilmSelectedListener listener.
        if (context is OnFilmSelectedListener) {
            // The context has implemented the listener;
            // Initialize the listener.
            listener = context

        } else {
            // The context has not implemented the listener;
            // Throw an exception.
            throw RuntimeException(context!!.toString() + " must implement OnFilmSelectedListener")
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
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Preform default onCreate operations;
        // and get previous savedInstanceState if any are saved and reload them.
        super.onCreate(savedInstanceState)

        // Create the Database handler.
        handler = FilmDBHandler(activity)

        // Create the SharedPreferences manager.
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)

        // Create the RecyclerView's adapter.
        adapter = SavedFilmAdapter(this)

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
     * The fragment should not add the view itself, but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     *
     * @return View Return the View for the fragment's UI, or null.
     */
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val rootView = inflater!!.inflate(R.layout.fragment_main, container, false)

        // Call initViews() to initialize views.
        initViews(rootView)

        // Call setupRecyclerView() to setup the RecyclerView.
        setupRecyclerView(rootView)

        // Return root layout.
        return rootView
    }

    /**
     * Helper method to initialize views at startup.
     *
     * @param rootView The root view of the fragment.
     */
    private fun initViews(rootView: View) {
        // Get the Sorting TextView.
        textViewSorting = rootView.findViewById(R.id.fragment_main_text_view_sorting) as TextView
    }

    /**
     * Helper method used to setup the RecyclerView.
     *
     * @param rootView The root view of the fragment.
     */
    private fun setupRecyclerView(rootView: View) {
        // Create an EmptyRecyclerView.
        recyclerView = rootView.findViewById(R.id.fragment_main_empty_recycler_view) as EmptyRecyclerView

        // Call getLayoutManager() to get the LayoutManager and set it.
        recyclerView!!.layoutManager = layoutManager

        // Set Item Decorations.
        recyclerView!!.addItemDecoration(DividerItemDecoration(activity, null, true, false))

        // Set Adapter.
        recyclerView!!.adapter = adapter

        // Register EmptyView.
        val textViewEmptyList = rootView.findViewById(R.id.fragment_main_text_view_empty_list) as TextView

        // Set TextView as the EmptyView.
        recyclerView!!.setEmptyView(textViewEmptyList)
    }

    /**
     * Called when the LayoutManager should be set.
     * The LayoutManager can be either Linear or Grid.
     *
     * @return RecyclerView.LayoutManager Returns the LayoutManager that should be used.
     */
    private val layoutManager: RecyclerView.LayoutManager
        get() {
            // Get number of columns from resource file.
            val columns = resources.getInteger(R.integer.gallery_columns)
            // Check if current orientation is portrait.
            if (activity.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                // Current orientation is portrait;
                // Set LayoutManager as Linear.
                return LinearLayoutManager(activity)
            } else {
                // Current orientation is landscape;
                // Check if columns value is at least 2 (= screen width is bigger than a handheld size).
                if (columns >= 2) {
                    // Column value is at least 2;
                    // Set LayoutManager as Grid.
                    return GridLayoutManager(activity, columns)
                } else {
                    // Column value is smaller than 2;
                    // Set LayoutManager as Linear.
                    return LinearLayoutManager(activity)
                }
            }
        }

    /**
     * Called when the fragment's activity has been created
     * and this fragment's view hierarchy instantiated.
     *
     * It can be used to do final initialization once these pieces are in place,
     * such as retrieving views or restoring state.
     *
     * It is also useful for fragments that use setRetainInstance(boolean)
     * to retain their instance, as this callback tells the fragment when it
     * is fully associated with the new activity instance.
     *
     * This is called after onCreateView and before onViewStateRestored(Bundle).
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state.
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Check if the fragment is restored or newly created.
        if (savedInstanceState != null) {
            // The fragment was restored;
            // Get the Tracking boolean from the bundle.
            hasTaskFinished = savedInstanceState.getBoolean(SAVED_STATE_TASK)

            // Check if the AsyncTask finished during first loading or if it was interrupted.
            if (hasTaskFinished) {
                // The AsyncTask finished;
                // Get the ArrayList from the bundle.
                val filmsArray = savedInstanceState.getParcelableArrayList<Film>(SAVED_STATE_ARRAY_LIST)

                // Call adapter.swapArray() to re-add the saved ArrayList.
                adapter!!.swapArray(filmsArray)

                // Get the Sorting Key from the bundle.
                sortingKey = savedInstanceState.getString(SAVED_STATE_SORTING_KEY)

                // Get the Sorting Order from the bundle.
                sortingOrder = savedInstanceState.getString(SAVED_STATE_SORTING_ORDER)
            } else {
                // The AsyncTask was interrupted;
                // Call onFirstLoading() to handle initialising methods.
                onFirstLoading()
            }

        } else {
            // The fragment is new;
            // Call onFirstLoading() to handle initialising methods.
            onFirstLoading()
        }

        // Call setCurrentSortingText() to set the TextView with the current sorting.
        setCurrentSortingText()
    }

    /**
     * Called when the fragment first loads to handle the first dat retrieval from the databases.
     */
    private fun onFirstLoading() {
        // Call getSortingKey() to get the sorting column.
        getSortingKey()

        // Call getSortingOrder() to get the sorting order.
        getSortingOrder()

        // Call swapArray() to get a new ArrayList from the database.
        swapArray()
    }

    /**
     * Called to retrieve the sorting key from the Shared Preferences file.
     */
    private fun getSortingKey() {
        // Get the sortingKey value from the Shared Preferences file.
        sortingKey = sharedPreferences!!.getString(SORT_KEY, FilmDBHandler.COLUMN_TIMESTAMP)
    }

    /**
     * Called to retrieve the sorting order from the Shared Preferences file.
     */
    private fun getSortingOrder() {
        // Get the sortingOrder value from the Shared Preferences file.
        sortingOrder = sharedPreferences!!.getString(SORT_ORDER, FilmDBHandler.SORT_ORDER_ASC)
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
        // Add the ArrayList to the saved state bundle.
        outState.putParcelableArrayList(SAVED_STATE_ARRAY_LIST, adapter!!.list)

        // Add the Sorting Key to the saved state bundle.
        outState.putString(SAVED_STATE_SORTING_KEY, sortingKey)

        // Add the Sorting Order to the saved state bundle.
        outState.putString(SAVED_STATE_SORTING_ORDER, sortingOrder)

        // Add the Tracking boolean to the saved state bundle.
        outState.putBoolean(SAVED_STATE_TASK, hasTaskFinished)

        // Do default onSaveInstanceState operations.
        super.onSaveInstanceState(outState)
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
        inflater!!.inflate(R.menu.menu_main, menu)
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal processing happen
     * (calling the item's Runnable or sending a message to its Handler as appropriate).
     *
     * You can use this method for any items for which you
     * would like to do processing without those other facilities.
     *
     * Derived classes should call through to the
     * base class for it to perform the default menu handling.
     *
     * @param item The menu item that was selected.
     *
     * @return boolean Return false to allow normal menu processing to proceed, true to consume it here.
     */
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        // Check which menu item was selected.
        when (item!!.itemId) {
        // Sorting Filter was selected.
            R.id.action_sorting_filter ->
                // Call showFilterPopupMenu() to show the Filter Popup Menu.
                showFilterPopupMenu()

        // Clear list was selected.
            R.id.action_clear_list ->
                // Call showClearListConfirmationDialog() to show the user the clear list confirmation dialog.
                showClearListConfirmationDialog()
        }

        // Close the options menu.
        return super.onOptionsItemSelected(item)
    }

    /**
     * Called when the Sorting Filter is clicked.
     * Opens the Sorting Filter Popup menu.
     */
    private fun showFilterPopupMenu() {
        // Get the Search MenuItem's View.
        val sortingFilterView = activity.findViewById(R.id.action_sorting_filter)

        // Create the Popup Menu.
        val popupMenu = PopupMenu(context, sortingFilterView)

        // Inflate the Popup Menu with its Menu file.
        popupMenu.inflate(R.menu.menu_sorting_options)

        // Register a OnMenuItemClickListener to the PopupMenu.
        popupMenu.setOnMenuItemClickListener(this)

        // Show the Popup Menu.
        popupMenu.show()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        // Check which menu item was selected.
        when (item.itemId) {
        // Sort by Date Added was selected.
            R.id.action_sort_by_timestamp ->
                // Call setSorting() and pass timestamp key.
                setSorting(SORT_KEY, FilmDBHandler.COLUMN_TIMESTAMP)

        // Sort by Title was selected.
            R.id.action_sort_by_title ->
                // Call setSorting() and pass title key.
                setSorting(SORT_KEY, FilmDBHandler.COLUMN_TITLE)

        // Sort by Release Year was selected.
            R.id.action_sort_by_year ->
                // Call setSorting() and pass year key.
                setSorting(SORT_KEY, FilmDBHandler.COLUMN_YEAR)

        // Sort by User Rating was selected.
            R.id.action_sort_by_rating ->
                // Call setSorting() and pass user rating key.
                setSorting(SORT_KEY, FilmDBHandler.COLUMN_USER_RATING)

        // Sort by Watched was selected.
            R.id.action_sort_by_watched ->
                // Call setSorting() and pass watched key.
                setSorting(SORT_KEY, FilmDBHandler.COLUMN_HAS_BEEN_SEEN)

        // Sort by Ascending was selected.
            R.id.action_sort_by_ascending ->
                // Call setSorting() and pass new sorting order value.
                setSorting(SORT_ORDER, FilmDBHandler.SORT_ORDER_ASC)

        // Sort by Descending was selected.
            R.id.action_sort_by_descending ->
                // Call setSorting() method and pass new sorting order value.
                setSorting(SORT_ORDER, FilmDBHandler.SORT_ORDER_DSC)
        }

        return true
    }

    /**
     * Handle sorting menu item clicks.
     *
     * @param typeOfSorting The type of sorting value that is being updated, either SORT_KEY or SORT_ORDER.
     * @param newSort       The new sorting value.
     */
    private fun setSorting(typeOfSorting: String, newSort: String) {
        // Open SharedPreferences to editing.
        val editor = sharedPreferences!!.edit()

        // Check what type of sorting value was passed.
        when (typeOfSorting) {

        // A SORT_KEY type was passed.
            SORT_KEY -> {
                // Change sorting key value.
                editor.putString(SORT_KEY, newSort)

                // Save the new sortingKey value.
                sortingKey = newSort
            }

        // A SORT_ORDER type was passed.
            SORT_ORDER -> {
                // Change sorting order value.
                editor.putString(SORT_ORDER, newSort)

                // Save the new sortingOrder value.
                sortingOrder = newSort
            }

        // Do nothing.
            else -> {
            }
        }

        // Apply changes.
        editor.apply()

        // Call swapArray() to get a new ArrayList from the database.
        swapArray()

        // Call setCurrentSortingText() to set the TextView with the current sorting.
        setCurrentSortingText()
    }

    /**
     * Called when the TextView's text should be updated.
     */
    private fun setCurrentSortingText() {
        // Call getSortingKeyString() to get the sortingKey string.
        val sortingColumn = sortingKeyString

        // Call getSortingOrderString() to get the sortingOrder string.
        val sortingOrder = sortingOrderString

        // Get current sorting settings.
        val currentSorting = String.format(getString(R.string.sorting_by),
                sortingColumn, sortingOrder)

        // Set TextView text.
        textViewSorting!!.text = currentSorting
    }

    /**
     * Helper method to get the sorting key string.
     *
     * @return String Returns the current key order string.
     */
    private val sortingKeyString: String
        get() {
            when (sortingKey) {
            // sortKey is the Timestamp column.
                FilmDBHandler.COLUMN_TIMESTAMP -> return getString(R.string.sorting_date_added)

            // sortKey is the Title column.
                FilmDBHandler.COLUMN_TITLE -> return getString(R.string.sorting_title)

            // sortKey is the Year column.
                FilmDBHandler.COLUMN_YEAR -> return getString(R.string.sorting_year)

            // sortKey is the User Rating column.
                FilmDBHandler.COLUMN_USER_RATING -> return getString(R.string.sorting_user_rating)

            // sortKey is the Seen column.
                FilmDBHandler.COLUMN_HAS_BEEN_SEEN -> return getString(R.string.sorting_seen)

            // default should use the COLUMN_TIMESTAMP.
                else -> return getString(R.string.sorting_date_added)
            }
        }

    /**
     * Helper method to get the sorting order string.
     *
     * @return String Returns the current sorting order string.
     */
    private val sortingOrderString: String
        get() {
            when (sortingOrder) {
            // sortingOrder is Ascending.
                FilmDBHandler.SORT_ORDER_ASC -> return getString(R.string.sorting_ascending_order)

            // sortingOrder is Descending.
                FilmDBHandler.SORT_ORDER_DSC -> return getString(R.string.sorting_descending_order)

            // default should use the SORT_ORDER_ASC.
                else -> return getString(R.string.sorting_ascending_order)
            }
        }

    /**
     * Called when a new film has been created or a previous film has been updated.
     *
     * Method used when the fragment is shown in a single-fragment activity.
     *
     * @param filmTitle The title of the film added or updated.
     * @param action    The action that was done. Can be either ACTION_IMPORT or ACTION_UPDATE.
     */
    fun filmCreatedOrUpdated(filmTitle: String, action: Int) {
        // Call swapArray() to get a new ArrayList from the database.
        swapArray()

        // Check which action was done.
        when (action) {

        // The film was added.
            Constants.ACTION_IMPORT ->
                // Create a Toast to inform the user.
                Toast.makeText(activity, String.format(getString(R.string.toast_film_has_been_added), filmTitle), Toast.LENGTH_SHORT).show()

        // The film was updated.
            Constants.ACTION_EDIT ->
                // Create a Toast to inform the user.
                Toast.makeText(activity, String.format(getString(R.string.toast_film_has_been_updated), filmTitle), Toast.LENGTH_SHORT).show()

        // Do nothing.
            else -> {
            }
        }
    }

    /**
     * Called when a change has been made to the data
     * or a new sorting is required.
     */
    private fun swapArray() {
        // Call LoadDataFromDatabaseAsyncTask().execute()
        // to retrieve the data from the database via an AsyncTask.
        LoadDataFromDatabaseAsyncTask().execute(sortingKey, sortingOrder)
    }

    /**
     * Called when an item in the list has been clicked.
     *
     * @param view    The View that was clicked.
     * @param film    The Film object selected.
     */
    override fun onItemClick(view: View, film: Film) {
        // Check which view was click on.
        when (view.id) {
        // The Seen/Not seen ToggleButton was clicked.
            R.id.row_item_main_toggle_button ->
                // Call swapArray() to get a new ArrayList from the database.
                swapArray()

        // The film block was clicked.
            else ->
                // Check if the listener is not null.
                if (listener != null) {
                    // The listener is not null;
                    // Call onFilmSelected() to handle click events.
                    listener!!.onFilmSelected(film)
                }
        }
    }

    /**
     * Called when an item in the list has been long clicked.
     *
     * @param film     The Film object selected.
     * @param position The current position in the list of the entry.
     */
    override fun onItemLongClick(film: Film, position: Int) {
        // Save film object and position for the dialog use.
        currentFilm = film
        currentFilmPosition = position

        // Create list items button array.
        val options = arrayOf<CharSequence>(getString(R.string.dialog_option_edit_film), getString(R.string.dialog_option_delete_film), getString(R.string.dialog_option_cancel))

        // Create a new Alert Dialog Builder.
        val builder = AlertDialog.Builder(activity)

        // Chain together various setter methods to set the dialog characteristics.
        builder
                // Set the Dialog's title.
                .setTitle(getString(R.string.dialog_option_title))

                // Set the Dialog's icon.
                .setIcon(R.mipmap.ic_launcher)

                // Set the Dialog's options array.
                .setItems(options) { dialog, which ->
                    // Check which button the user clicked.
                    when (which) {

                    // The view the film's data option was selected.
                        DIALOG_OPTION_VIEW ->
                            // Check if the listener is not null.
                            if (listener != null) {
                                // The listener is not null;
                                // Call onFilmSelected() to handle click events.
                                listener!!.onFilmSelected(currentFilm)
                            }

                    // Delete the film was selected.
                        DIALOG_OPTION_DELETE ->
                            // Call showDeleteFilmConfirmationDialog() to open a confirmation dialog.
                            showDeleteFilmConfirmationDialog()

                    // Cancel was selected.
                        DIALOG_OPTION_CANCEL ->
                            // Do nothing and close the dialog box.
                            dialog.dismiss()
                    }
                }

        // Create an AlertDialog.
        val dialog = builder.create()

        // Set CanceledOnTouchOutside to allow clicking outside of dialog to cancel it.
        dialog.setCanceledOnTouchOutside(true)

        // Creates an AlertDialog with the arguments supplied to this builder and immediately displays the dialog.
        dialog.show()
    }

    /**
     * Called when a ToggleButton has been clicked.
     *
     * @param film The Film object of the current entry.
     */
    override fun onToggleClick(film: Film) {
        // Update SQL database.
        handler!!.updateFilm(film)
    }

    /**
     * Called when the user selected the option to
     * clear (delete) the film list.
     */
    private fun showClearListConfirmationDialog() {
        // Check if the amount of items in the list is
        // greater than zero (the list is not empty).
        if (adapter!!.itemCount > 0) {
            // The list is not empty;
            // Instantiate an AlertDialog.Builder with its constructor.
            val builder = AlertDialog.Builder(activity)

            // Chain together various setter methods to set the dialog characteristics.
            builder
                    // Set the Dialog's title.
                    .setTitle(R.string.dialog_delete_all_films_title)

                    // Add the Dialog's Positive Button.
                    .setPositiveButton(R.string.dialog_option_clear_list) { _, _ ->
                        // Call deleteList() to delete the list.
                        deleteList()
                    }

                    // Add the Dialog's Negative Button.
                    .setNegativeButton(R.string.dialog_option_cancel) { dialog, _ ->
                        // Dismiss the dialog.
                        dialog.dismiss()
                    }

                    // Creates an AlertDialog with the arguments supplied to this builder and immediately displays the dialog.
                    .show()
        } else {
            // The list is empty; Create a Toast informing the user.
            Toast.makeText(activity, R.string.toast_error_clear_list_failed_list_empty, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Called when the user selected the option delete the current Film.
     */
    private fun showDeleteFilmConfirmationDialog() {
        // Instantiate an AlertDialog.Builder with its constructor.
        val builder = AlertDialog.Builder(activity)

        // Chain together various setter methods to set the dialog characteristics.
        builder
                // Set the Dialog's title.
                .setTitle(R.string.dialog_delete_film_title)

                // Add the Dialog's Positive Button.
                .setPositiveButton(R.string.dialog_option_delete_film) { _, _ ->
                    // Call deleteFilm() to delete the Film from the list.
                    deleteFilm()
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
     * Called when the user has approved his decision to delete a single Film in the Dialog.
     */
    private fun deleteFilm() {
        // Create a copy of the film object in case someone clicks
        // on another film before the snackbar closes.
        val deletedFilm = currentFilm
        val deletedFilmPosition = currentFilmPosition

        // Call removeFilm() to delete the selected Film.
        adapter!!.removeFilm(deletedFilmPosition)

        // Create relevant msg.
        val msg = String.format(getString(R.string.snackbar_film_has_been_deleted), currentFilm.title)

        // Call setSortingVisible() to check if the TextView should be visible or not.
        setSortingVisible()

        // Security check if the layout view is available.
        if (view != null) {
            // The view is available;
            // Create a Snackbar.
            Snackbar

                    // Set the Snackbar's Title and Length.
                    .make(view!!, msg, Snackbar.LENGTH_LONG)

                    // Set an Undo Action to the Snackbar.
                    .setAction(R.string.snackbar_undo) {
                        // Call the adapter's undoClearList() to undo the deletion.
                        adapter!!.undoRemoveFilm(deletedFilm, deletedFilmPosition)

                        // Scroll to the restored Film's position.
                        recyclerView!!.scrollToPosition(deletedFilmPosition)

                        // Call setSortingVisible() to check if the TextView should be visible or not.
                        setSortingVisible()
                    }

                    // Add an onDismissed callback to the Snackbar.
                    .addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            super.onDismissed(transientBottomBar, event)

                            // Check if the dismiss event was not from the UNDO action.
                            if (event != BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_ACTION) {
                                // The event wasn't an UNDO action;
                                // Call handler.deleteFilm() to remove the Film from the database.
                                handler!!.deleteFilm(deletedFilm.id)

                                // Call deletePoster() to delete the Film's poster
                                // from the device.
                                deletedFilm.posterInternalUri?.let { deletePoster(it) }
                            }
                        }
                    })

                    // Show the Snackbar.
                    .show()
        }
    }

    /**
     * Called when the user has approved his decision to delete the full list in the Dialog.
     */
    private fun deleteList() {
        // Create a copy of the ArrayList.
        val backupList = createBackupList()

        // Call clearList() to delete the list.
        adapter!!.clearList()

        // Create relevant msg.
        val msg = getString(R.string.snackbar_your_list_has_been_deleted)

        // Call setSortingVisible() to check if the TextView should be visible or not.
        setSortingVisible()

        // Security check if the layout view is available.
        if (view != null) {
            // The view is available;
            // Create a Snackbar.
            Snackbar

                    // Set the Snackbar's Title and Length.
                    .make(view!!, msg, Snackbar.LENGTH_LONG)

                    // Set an Undo Action to the Snackbar.
                    .setAction(R.string.snackbar_undo) {
                        // Call the adapter's undoClearList() to undo the deletion.
                        adapter!!.undoClearList(backupList)

                        // Call setSortingVisible() to check if the TextView should be visible or not.
                        setSortingVisible()
                    }

                    // Add an onDismissed callback to the Snackbar.
                    .addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            super.onDismissed(transientBottomBar, event)

                            // Check if the dismiss event was not from the UNDO action.
                            if (event != BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_ACTION) {
                                // The event wasn't an UNDO action;
                                // Call handler.deleteAllFilms() to remove all of the film data from the database.
                                handler!!.deleteAllFilms()

                                // Call deleteAllPosters() to delete all the film posters
                                // from the device.
                                deleteAllPosters(backupList)
                            }
                        }
                    })

                    // Show the Snackbar.
                    .show()
        }
    }

    /**
     * Called to create a copy of the ArrayList from the current one.
     *
     * @return ArrayList<Film> A copy of the ArrayList.
     */
    private fun createBackupList(): ArrayList<Film> {
        // Create a new ArrayList.
        val backupList = ArrayList<Film>()

        // Get current saved list from adapter.
        val currentFilms = adapter!!.list as ArrayList<Film>

        // Go over list.
        for (i in currentFilms.indices) {
            // Add each item to the new ArrayList.
            backupList.add(i, currentFilms[i])
        }
        // Return the backup ArrayList.
        return backupList
    }

    /**
     * Called when clear list action has been requested.
     * Deletes all film posters from the user's device.
     *
     * @param backupList A backup of the user's deleted Film list.
     */
    private fun deleteAllPosters(backupList: ArrayList<Film>) {
        // Go over the films ArrayList.
        for (i in backupList.indices) {
            // Call deletePoster() to deletePosterFromDevice the current poster.
            backupList[i].posterInternalUri?.let { deletePoster(it) }
        }
    }

    /**
     * Called when a delete film action has been requested, or from deleteAllPosters().
     * Deletes the film's poster from the user's device.
     *
     * @param uri The URI on the device where the poster of the current film is located.
     */
    private fun deletePoster(uri: String) {
        // Create a file from the URI.
        val filmPoster = File(uri)

        // Check if the file exists.
        if (filmPoster.exists()) {
            // File exists;
            // Delete it.
            filmPoster.delete()
        }
    }

    /**
     * Called when a change is detected in the list and a check should be performed on the list to
     * set the sorting text to visible or invisible.
     */
    private fun setSortingVisible() {
        // Check if the amount of items in the list is
        // greater than zero (the list is not empty).
        if (adapter!!.itemCount > 0) {
            // The list is not empty;
            // Set the sorting text to visible.
            textViewSorting!!.visibility = View.VISIBLE
        } else {
            // The list is empty;
            // Set the sorting text to invisible.
            textViewSorting!!.visibility = View.INVISIBLE
        }
    }

    /**
     * Interface to communicate with the hosting Activity.
     */
    interface OnFilmSelectedListener {
        /**
         * Called when an item in the list has been clicked.

         * @param film The Film object of the current entry.
         */
        fun onFilmSelected(film: Film)
    }

    /**
     * Called to retrieve the Film List from the database on another thread.
     */
    private inner class LoadDataFromDatabaseAsyncTask : AsyncTask<String, Void, ArrayList<Film>>() {

        /**
         * Method runs in Second Thread.
         *
         * Uses the OMDB API to search for the film with
         * the requested IMDB ID and returns the search results.
         *
         * @param params The Column Key (0) and the Order Key (1).
         *
         * @return ArrayList<Film> Returns the saved film list from the database.
         */
        override fun doInBackground(vararg params: String): ArrayList<Film> {
            // Get the Column Key.
            val column = params[0]

            // Get the Order Key.
            val order = params[1]

            // Call handler.queryAllFilms() to get the saved film list.
            return handler!!.queryAllFilms(column, order)
        }

        /**
         * Method runs in UI Thread.
         */
        override fun onPostExecute(films: ArrayList<Film>) {
            // Call adapter.swapArray() to swap the ArrayList.
            adapter!!.swapArray(films)

            // Set the Tracking boolean to true.
            hasTaskFinished = true

            // Call setSortingVisible() to check if the TextView should be visible or not.
            setSortingVisible()
        }
    }
}