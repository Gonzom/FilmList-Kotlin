package tech.gonzo.filmlist.ui.fragments

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.view.*
import android.widget.TextView
import tech.gonzo.filmlist.R
import tech.gonzo.filmlist.asynctasks.APIImportTask
import tech.gonzo.filmlist.asynctasks.APISearchTask
import tech.gonzo.filmlist.controller.OnShortFilmSelectedListener
import tech.gonzo.filmlist.controller.SearchFilmAdapter
import tech.gonzo.filmlist.database.FilmDBHandler
import tech.gonzo.filmlist.models.Film
import tech.gonzo.filmlist.models.ShortFilm
import tech.gonzo.filmlist.ui.views.DividerItemDecoration
import tech.gonzo.filmlist.ui.views.EmptyRecyclerView
import tech.gonzo.filmlist.utils.AppUtil
import tech.gonzo.filmlist.utils.Constants
import java.util.*

class ImportFilmFragment : Fragment(), APISearchTask.APISearchTaskListener, APIImportTask.APIImportTaskListener, SearchView.OnQueryTextListener, OnShortFilmSelectedListener {

    companion object {
        // Saved Instance State constants.
        private val SAVED_INSTANCE_STATE_ARRAY_LIST = "saved_instance_state_array_list"
        private val SAVED_INSTANCE_STATE_RESULTS = "saved_instance_state_results"
        private val SAVED_INSTANCE_STATE_EMPTY_LIST = "saved_instance_state_empty_list"
        private val SAVED_STATE_LAST_SEARCH_QUERY = "last_search_query"
        private val SAVED_STATE_API_TRACKING_BOOLEAN = "api_tracking"
    }

    // UI references.
    private var textViewEmptyList: TextView? = null
    private var textViewSearchResults: TextView? = null
    private var progressDialog: ProgressDialog? = null

    // The Fragment Listeners.
    private var listener: ImportFilmListener? = null

    // RecyclerView Adapter.
    private var adapter: SearchFilmAdapter? = null

    // ArrayList holding the adapter data.
    private var shortFilms: ArrayList<ShortFilm>? = null

    // Database Handler.
    private var handler: FilmDBHandler? = null

    // The Search Results string.
    private var searchResults: String? = null

    // The current search query.
    private var searchQuery: String = ""

    // Tracking boolean that checks if the initial API request has returned before the fragment was recreated.
    private var hasApiReturned: Boolean = false

    /**
     * Called when a fragment is first attached to its context.
     * [.onCreate] will be called after this.
     *
     * @param context The context hosting the fragment.
     */
    override fun onAttach(context: Context?) {
        super.onAttach(context)

        // Check if the context has implemented the ImportFilmListener listener.
        if (context is ImportFilmListener) {
            // The context has implemented the listener;
            // Initialize the listener.
            listener = context

        } else {
            // The context has not implemented the listener;
            // Throw an exception.
            throw RuntimeException(context!!.toString() + " must implement ImportFilmListener")
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

        // Call dismissDialog() to dismiss the ProgressDialog.
        dismissDialog()
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

     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Preform default onCreate operations;
        // and get previous savedInstanceState if any are saved and reload them.
        super.onCreate(savedInstanceState)

        // Create a database handler.
        handler = FilmDBHandler(activity)

        // Create an Adapter.
        adapter = SearchFilmAdapter(this)

        // Check if the fragment is new or was recreated by checking if savedInstanceState is null.
        if (savedInstanceState == null) {
            // The fragment is new;
            // Create the ArrayList.
            shortFilms = ArrayList<ShortFilm>()
        } else {
            // The fragment was recreated;
            // Get the ArrayList from the bundle.
            shortFilms = savedInstanceState.getParcelableArrayList<ShortFilm>(SAVED_INSTANCE_STATE_ARRAY_LIST)
        }
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
        val rootView = inflater!!.inflate(R.layout.fragment_import_film, container, false)

        // Enable fragment OptionsMenu.
        setHasOptionsMenu(true)

        // Call initViews() to initialize views.
        initViews(rootView)

        // Call setupRecyclerView() to setup the RecyclerView.
        setupRecyclerView(rootView)

        // Check if the fragment is new or was recreated by checking if savedInstanceState is null.
        if (savedInstanceState == null) {
            // The fragment is new;
            // Initialize the tracking boolean to true;
            // No Search is needed at the start.
            hasApiReturned = true

            // Initialize the Search Query to an empty string.
            searchQuery = ""
        } else {
            // The fragment was recreated;
            // Call useDataFromBundle() to re-add the data from the bundle.
            useDataFromBundle(savedInstanceState)
        }

        return rootView
    }

    /**
     * Helper method to initialize views at startup.
     *
     * @param rootView The root view of the fragment.
     */
    private fun initViews(rootView: View) {
        // Get the Search Results TextView.
        textViewSearchResults = rootView.findViewById(R.id.fragment_import_text_view_search_results) as TextView
    }

    /**
     * Helper method used to setup the RecyclerView.
     *
     * @param rootView The root view of the fragment.
     */
    private fun setupRecyclerView(rootView: View) {
        // Create an EmptyRecyclerView.
        val recyclerView = rootView.findViewById(R.id.fragment_import_empty_recycler_view) as EmptyRecyclerView

        // Set LayoutManager.
        recyclerView.layoutManager = LinearLayoutManager(activity)

        // Set Item Decorations.
        recyclerView.addItemDecoration(DividerItemDecoration(activity, null, true, false))

        // Set Adapter.
        recyclerView.adapter = adapter

        // Register EmptyView.
        textViewEmptyList = rootView.findViewById(R.id.fragment_import_text_view_empty_list) as TextView

        // Set TextView as the EmptyView.
        recyclerView.setEmptyView(textViewEmptyList)
    }

    /**
     * Called when a fragment is recreated.
     * Extracts all saved data, re-saves it globally and re-sets it.
     *
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    private fun useDataFromBundle(savedInstanceState: Bundle) {
        // Get the Search Results string from the bundle.
        searchResults = savedInstanceState.getString(SAVED_INSTANCE_STATE_RESULTS)

        // Set the Search Results to the Search Results TextView.
        textViewSearchResults!!.text = searchResults

        // Get the Empty List string from the bundle.
        textViewEmptyList!!.text = savedInstanceState.getString(SAVED_INSTANCE_STATE_EMPTY_LIST)

        // Get the Last Search Query from the bundle.
        searchQuery = savedInstanceState.getString(SAVED_STATE_LAST_SEARCH_QUERY)

        // Get the API returned tracking boolean from the bundle.
        hasApiReturned = savedInstanceState.getBoolean(SAVED_STATE_API_TRACKING_BOOLEAN)

        // Check if the initial API was returned.
        if (hasApiReturned) {
            // Initial API was returned;
            // Check if there was a previous search query or not.
            if (searchQuery.isNotEmpty()) {
                // There was a previous search query;
                // Add the Film search result array to the adapter.
                adapter!!.swapArray(shortFilms!!)
            }
        } else {
            // Initial API was not returned;
            // Call startSearch() to start the search.
            startSearch()
        }
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
    override fun onSaveInstanceState(outState: Bundle?) {
        // Add the ArrayList to the saved state bundle.
        outState!!.putParcelableArrayList(SAVED_INSTANCE_STATE_ARRAY_LIST, shortFilms)

        // Add the Search Results string to the saved state bundle.
        outState.putString(SAVED_INSTANCE_STATE_RESULTS, searchResults)

        // Add the Empty List string to the saved state bundle.
        outState.putString(SAVED_INSTANCE_STATE_EMPTY_LIST, textViewEmptyList!!.text.toString())

        // Add the Last Search Query to the saved state bundle.
        outState.putString(SAVED_STATE_LAST_SEARCH_QUERY, searchQuery)

        // Add the API returned tracking boolean to the saved state bundle.
        outState.putBoolean(SAVED_STATE_API_TRACKING_BOOLEAN, hasApiReturned)

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
        inflater!!.inflate(R.menu.menu_import_film, menu)

        // Get the Search MenuItem.
        val searchMenuItem = menu!!.findItem(R.id.action_search)

        // Call setupSearchView() to setup the SearchView.
        setupSearchView(searchMenuItem)
    }

    /**
     * Called to setup the SearchView.
     *
     * @param searchMenuItem The Search MenuItem.
     */
    private fun setupSearchView(searchMenuItem: MenuItem) {
        // Get the SearchView.
        val searchView = searchMenuItem.actionView as SearchView

        // Set the SearchView's hint.
        searchView.queryHint = getString(R.string.hint_search_enter_film_title)

        // Set the SearchView's Max Width.
        searchView.maxWidth = Integer.MAX_VALUE

        // Sets the SearchView to be expended.
        searchView.setIconifiedByDefault(false)

        // Set an OnQueryTextListener to the SearchView.
        searchView.setOnQueryTextListener(this)

        // Check if there was a previous search query or not.
        if (searchQuery.isNotEmpty()) {
            // There was a previous search query;
            // Set the search query to the SearchView.
            searchView.setQuery(searchQuery, false)
        }
    }

    /**
     * Called when an item in the list has been clicked.
     *
     * @param shortFilm The ShortFilm object selected.
     */
    override fun onItemClick(shortFilm: ShortFilm) {
        // Create a search key with the unique IMDB ID.
        val imdbId = shortFilm.imdbId

        // Query the database to check if a Film with the specific IMDB ID exists in the database.
        val queriedFilm = handler!!.queryFilmWithImdbId(imdbId)

        // Check if a film was found.
        if (queriedFilm != null) {
            // A film was found.
            // Security check if the layout view is available.
            if (view != null) {
                // The view is available;
                // Create error msg.
                val msg = String.format(getString(R.string.snackbar_error_film_already_added_to_list), shortFilm.title, shortFilm.year.toString())

                // Create a Snackbar.
                Snackbar

                        // Set the Snackbar's Title and Length.
                        .make(view!!, msg, Snackbar.LENGTH_LONG)

                        // Display the Snackbar.
                        .show()
            }
        } else {
            // A film was not found;
            // Create a search key with the unique IMDB ID.
            val imdbID = shortFilm.imdbId

            // Start the APIImportTask AsyncTask;
            // Use the imdbID as the parameter.
            APIImportTask(this).execute(imdbID)
        }
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        return false
    }

    /**
     * Called when the query text is changed by the user.
     *
     * @param newText The new content of the query text field.
     * @return Boolean Returns false if the SearchView should perform the default action
     * * of showing any suggestions if available, true if the action was handled by the listener.
     */
    override fun onQueryTextChange(newText: String): Boolean {
        // Trim leading and trailing space characters.
        val newSearch = newText.trim { it <= ' ' }

        // Check if the new string is the same as the previous one.
        if (searchQuery.equals(newSearch, ignoreCase = true)) {
            // The new string is the same as the previous one (fragment restored);
            // Do nothing.
            return false
        }

        // Save the new string globally.
        this.searchQuery = newSearch

        // Check if the search text is not empty.
        if (searchQuery.isEmpty()) {
            // The search text is empty;
            // Check if the Adapter is not null.
            if (adapter != null) {
                // The adapter is not null;
                // Call clearList() to clear the adapter.
                adapter!!.clearList()

                // Clear the ArrayList.
                shortFilms!!.clear()
            }

            // Change the EmptyList text to the initial Empty View.
            textViewEmptyList!!.setText(R.string.empty_list_start_search)

            // Clear the Search Results string.
            searchResults = ""

            // Clear the Search Results TextView.
            textViewSearchResults!!.text = null
        } else {
            // The search text is not empty;
            // Set the tracking boolean to false;
            // An API call is being made.
            hasApiReturned = false

            // Call startSearch() to start the search.
            startSearch()
        }

        // The SearchView should perform the default action of showing any suggestions if available.
        return false
    }

    /**
     * Called when the search should start.
     */
    private fun startSearch() {
        // Call AppUtil.isAppConnectedToNetwork() and check if there is a working network connection.
        if (AppUtil.isAppConnectedToNetwork(context)) {
            // There is a working network connection;
            // Start the APISearchTask AsyncTask;
            // Use the search key as a parameter.
            APISearchTask(this).execute(searchQuery)

        } else {
            // There is no working network connection;
            // Call AppUtil.showNoNetworkConnectionDialog() to open a Confirmation Alert Dialog to the user.
            AppUtil.showNoNetworkConnectionDialog(context)
        }
    }

    /**
     * Called when the task starts.
     */
    override fun onAPIImportTaskStarted() {
        // Call createProgressDialog() to create a ProgressDialog.
        createProgressDialog()
    }

    /**
     * Called when a Progress Dialog should be created.
     */
    private fun createProgressDialog() {
        // Create a new Progress Dialog.
        progressDialog = ProgressDialog(activity)

        // Set the dialog's message.
        progressDialog!!.setMessage(getString(R.string.dialog_progress_generating_film))

        // Show the ProgressDialog.
        progressDialog!!.show()
    }

    /**
     * Called when the task finished with an error message.

     * @param errorMessage The error message returned from the server.
     */
    override fun onAPITaskFinishedWithError(errorMessage: String) {
        // Check which error message returned.
        when (errorMessage) {

            Constants.ERROR_MOVIE_NOT_FOUND ->
                // The Error message is ERROR_MOVIE_NOT_FOUND;
                // No results received;
                // Call onNoResults() to handle the event.
                onNoResults()

            else ->
                // The Error message is not ERROR_MOVIE_NOT_FOUND;
                // Security check if the layout view is available.
                if (view != null) {
                    // The view is available;
                    // Create a Snackbar informing the user of the error received.
                    Snackbar.make(view!!, errorMessage, Snackbar.LENGTH_LONG)

                            // Display the Snackbar.
                            .show()
                }
        }

        // Call onTaskFinished() to finish task operations.
        onTaskFinished()
    }

    /**
     * Called when search results have been received.
     *
     * @param films          The search results in an ArrayList of Film objects.
     * @param searchQuery The search query used.
     */
    override fun getFilms(films: ArrayList<ShortFilm>?, searchQuery: String) {
        // Check if the ArrayList is not null.
        if (films != null) {
            // The ArrayList is not null;
            // Get the total results returned.
            val resultsShown = films.size

            // Check if there were no results received.
            if (resultsShown < 1) {
                // No results received;
                // Call onNoResults() to handle the event.
                onNoResults()

            } else {
                // There were results received;
                // Check if the returning response is of the latest Search Query.
                if (searchQuery.equals(this.searchQuery, ignoreCase = true)) {
                    // The returning response is of the latest Search Query;
                    // Save the ArrayList.
                    this.shortFilms = films

                    // Set the tracking boolean to true.
                    hasApiReturned = true

                    // Change the current ArrayList with the Array from the results.
                    adapter!!.swapArray(films)

                    // Check if the fragment is still connected to the activity.
                    if (activity != null) {
                        // The fragment is still connected to the activity;
                        // Get the Search Results string with the amount of search results returned.
                        searchResults = activity.resources.getQuantityString(R.plurals.number_of_search_results, resultsShown, resultsShown)

                        // Set the Search Results to the TextView.
                        textViewSearchResults!!.text = searchResults
                    }
                }
            }
        }

        // Call onTaskFinished() to finish task operations.
        onTaskFinished()
    }

    /**
     * Called when no results returned.
     */
    private fun onNoResults() {
        // Set the tracking boolean to true.
        hasApiReturned = true

        // Call clearList() to clear the adapter.
        adapter!!.clearList()

        // Clear the ArrayList.
        shortFilms!!.clear()

        // Clear the Search Results string.
        searchResults = ""

        // Change the EmptyList text to reflect that.
        textViewEmptyList!!.setText(R.string.empty_list_no_search_results_found)

        // Clear the Search Results TextView.
        textViewSearchResults!!.text = null
    }

    /**
     * Called after a film result has been received.
     *
     * @param requestedFilm The requestedFilm object created by the search.
     */
    override fun getSelectedFilm(requestedFilm: Film?) {
        // Check if Film object is not null.
        if (requestedFilm != null) {
            // Film object is not null;
            // Check if the listener is not null.
            if (listener != null) {
                // The listener is not null;
                // Call getSelectedFilm() to handle click events.
                listener!!.getSelectedFilm(requestedFilm)
            }
        }

        // Call onTaskFinished() to finish task operations.
        onTaskFinished()
    }

    /**
     * Called when the task finishes.
     * Dismiss dialogs and re-enable views here.
     */
    private fun onTaskFinished() {
        // Call dismissDialog() to dismiss the ProgressDialog when the Async Task is done.
        dismissDialog()
    }

    /**
     * Called when the ProgressDialog should be closed.
     */
    private fun dismissDialog() {
        // Check if the ProgressDialog is not null.
        if (progressDialog != null) {
            // The ProgressDialog is not null;
            // Dismiss it.
            progressDialog!!.dismiss()

            // Set dialog to null
            progressDialog = null
        }
    }

    /**
     * Interface to communicate with the hosting Activity.
     */
    interface ImportFilmListener {
        /**
         * Called when an item in the search list has been clicked.
         *
         * @param film The Film object created from the current entry.
         */
        fun getSelectedFilm(film: Film)
    }
}