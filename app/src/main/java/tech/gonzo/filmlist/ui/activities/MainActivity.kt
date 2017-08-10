package tech.gonzo.filmlist.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import tech.gonzo.filmlist.R
import tech.gonzo.filmlist.models.Film
import tech.gonzo.filmlist.ui.fragments.MainFragment
import tech.gonzo.filmlist.utils.Constants

class MainActivity : AppCompatActivity(), View.OnClickListener, MainFragment.OnFilmSelectedListener {

    companion object {
        // Request Code constants.
        private val REQUEST_CODE_EDIT_FILM = 108
    }

    // UI references.
    private var mainFragment: MainFragment? = null

    /**
     * Called when the activity is starting.
     * This is where most initialization should go:
     * - calling setContentView(int) to inflate the activity's UI,
     * - using findViewById(int) to programmatically interact with widgets in the UI,
     * - creating SQL database helper and ArrayAdapter objects,
     * - connecting the adapter to the ListView,
     * - and registering click listeners.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down
     * *                           then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     * *                           Note: Otherwise it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Preform default onCreate operations;
        // and get previous savedInstanceState if any are saved and reload them.
        super.onCreate(savedInstanceState)

        // Inflate the layout xml.
        setContentView(R.layout.activity_main)

        // Register Toolbar.
        val toolbar = findViewById(R.id.toolbar) as Toolbar

        // Set the Toolbar to act as the ActionBar for this Activity window.
        setSupportActionBar(toolbar)

        // Register FloatingActionButton.
        val fab = findViewById(R.id.fab) as FloatingActionButton

        // Set OnClickListener.
        fab.setOnClickListener(this)

        // Create a support FragmentManager;
        val fragmentManager = supportFragmentManager

        // Register fragment.
        mainFragment = fragmentManager.findFragmentById(R.id.fragmentList_Main) as MainFragment
    }

    /**
     * Called when a view has been clicked.
     *
     * @param view The view that was clicked.
     */
    override fun onClick(view: View) {
        // Check which view was click on.
        when (view.id) {
        // The Floating Action Button was clicked.
            R.id.fab -> {
                // Create an Intent for ImportFilmActivity activity.
                val intent = Intent(this@MainActivity, ImportFilmActivity::class.java)

                // Open ImportFilmActivity activity.
                startActivityForResult(intent, REQUEST_CODE_EDIT_FILM)
            }
        }
    }

    /**
     * Called when a film is clicked in the List,
     * or from the dialog (alertDialogListLongClick) that opens
     * from a long click on an item in the List.
     *
     * Starts the ViewFilmActivity activity.
     *
     * @param film The Film selected.
     */
    override fun onFilmSelected(film: Film) {
        // Create an Intent for ViewFilmActivity.
        val intent = Intent(this, ViewFilmActivity::class.java)

        // Add information to the Intent to flag it as an already existing entry in the database.
        intent.putExtra(Constants.INTENT_ACTION, Constants.ACTION_EDIT)

        // Add the Film object to the Intent.
        intent.putExtra(Constants.INTENT_ITEM_FILM, film)

        // Start ViewFilmActivity activity.
        startActivityForResult(intent, REQUEST_CODE_EDIT_FILM)
    }

    /**
     * Called when an activity you launched exits,
     * giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     *
     * The resultCode will be RESULT_CANCELED if
     * - the activity explicitly returned that,
     * - didn't return any result,
     * - or crashed during its operation.

     * @param requestCode The integer request code
     * *                    originally supplied to startActivityForResult(),
     * *                    allowing you to identify who this result came from.
     * @param resultCode  The integer result code
     * *                    returned by the child activity through its setResult().
     * @param data        An Intent, which can return result data to the caller
     * *                    (various data can be attached to Intent "extras").
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Here we need to check if a film has been saved or updated.
        // If it was, the requestCode will match the REQUEST_CODE_EDIT_FILM value.
        if (requestCode == REQUEST_CODE_EDIT_FILM) {
            // requestCode was REQUEST_CODE_EDIT_FILM;
            // Check if the resultCode was RESULT_OK.
            if (resultCode == Activity.RESULT_OK) {
                // resultCode was RESULT_OK;
                // Check if the Intent is not null;
                if (data != null) {
                    // The Intent is not null;
                    // Get the Film's title from the Intent.
                    val filmTitle = data.getStringExtra(Constants.INTENT_ITEM_FILM)

                    // Get the user-action from the Intent.
                    val action = data.getIntExtra(Constants.INTENT_ACTION, 0)

                    // Call mainFragment.filmCreatedOrUpdated() to refresh the Adapter's ArrayList.
                    mainFragment!!.filmCreatedOrUpdated(filmTitle, action)
                }
            }
        }
    }
}