package tech.gonzo.filmlist.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import tech.gonzo.filmlist.R
import tech.gonzo.filmlist.models.Film
import tech.gonzo.filmlist.ui.fragments.ImportFilmFragment
import tech.gonzo.filmlist.utils.Constants

class ImportFilmActivity : AppCompatActivity(), ImportFilmFragment.ImportFilmListener {

    companion object {
        // Request Code constants.
        private val REQUEST_CODE_IMPORTED_DATA = 123
    }

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
        setContentView(R.layout.activity_import_film)

        // Register Toolbar.
        val toolbar = findViewById(R.id.toolbar) as Toolbar

        // Set the Toolbar to act as the ActionBar for this Activity window.
        setSupportActionBar(toolbar)

        // Check if the Support Action Bar is not null.
        if (supportActionBar != null) {
            // Support Action Bar is not null; Set the Up button.
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
    }

    /**
     * Called when an item in the search list has been clicked.
     *
     * @param film The Film object created from the current entry.
     */
    override fun getSelectedFilm(film: Film) {
        // Create an Intent for ViewFilmActivity.
        val intent = Intent(this, ViewFilmActivity::class.java)

        // Add the action type to the Intent.
        intent.putExtra(Constants.INTENT_ACTION, Constants.ACTION_IMPORT)

        // Add the film object to the Intent.
        intent.putExtra(Constants.INTENT_ITEM_FILM, film)

        // Start EditFilmActivity activity.
        startActivityForResult(intent, REQUEST_CODE_IMPORTED_DATA)
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
     *
     * @param requestCode The integer request code
     * *                    originally supplied to startActivityForResult(),
     * *                    allowing you to identify who this result came from.
     * @param resultCode  The integer result code
     * *                    returned by the child activity through its setResult().
     * @param data        An Intent, which can return result data to the caller
     * *                    (various data can be attached to Intent "extras").
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        // Here we need to check if the activity that was triggered was the ImportFilmActivity
        // and that a film has been saved.
        // If it is, the requestCode will match the REQUEST_CODE_IMPORTED_DATA value.
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_IMPORTED_DATA) {
            // Return RESULT_OK back to MainActivity onActivityResult,
            // informing the activity that a film was saved.
            setResult(Activity.RESULT_OK, data)

            // Close activity.
            finish()
        }
    }
}