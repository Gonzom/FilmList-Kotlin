package tech.gonzo.filmlist.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import tech.gonzo.filmlist.R
import tech.gonzo.filmlist.models.Film
import tech.gonzo.filmlist.ui.fragments.ViewFilmFragment
import tech.gonzo.filmlist.utils.Constants

class ViewFilmActivity : AppCompatActivity(), ViewFilmFragment.OnFilmSavedListener {

    /**
     * Called when the activity is starting.
     * This is where most initialization should go:
     * - calling setContentView(int) to inflate the activity's UI,
     * - using findViewById(int) to programmatically interact with widgets in the UI,
     * - calling managedQuery(android.net.Uri, String[], String, String[], String)
     * to retrieve cursors for data being displayed, etc.
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
        setContentView(R.layout.activity_view_film)

        // Call setupToolbar() to setup the Toolbar.
        setupToolbar()

        // Check if the activity is new or was recreated by checking if savedInstanceState is null.
        if (savedInstanceState == null) {
            // The activity is new;
            // Get the calling Intent.
            val callingIntent = intent

            // Get the action.
            val action = callingIntent.getIntExtra(Constants.INTENT_ACTION, 0)

            // Throws a non-crashing error Class not found when unmarshalling: tech.gonzo.filmlist.model.Film.
            // Get the film object.
            val film = callingIntent.getParcelableExtra<Film>(Constants.INTENT_ITEM_FILM)

            // Register fragment.
            val viewFilmFragment = supportFragmentManager.findFragmentById(R.id.fragmentViewFilm) as ViewFilmFragment

            // Call viewFilmFragment.initializeFragmentData() and pass it the film information.
            viewFilmFragment.initializeFragmentData(action, film)
        }
    }

    /**
     * Called to setup the activity's Toolbar.
     */
    private fun setupToolbar() {
        // Get the Toolbar.
        val toolbar = findViewById(R.id.toolbar) as Toolbar

        // Call the supportActionBar() and pass it the Toolbar.
        setSupportActionBar(toolbar)

        // Check if the getSupportActionBar() is null.
        if (supportActionBar != null) {
            // getSupportActionBar() is not null;
            // Call setDisplayHomeAsUpEnabled() to display the Home/Up Button.
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onFilmSaved(film: Film, action: Int) {
        // Create a new returning Intent.
        val returnIntent = Intent()

        // Add the film title to the Intent.
        returnIntent.putExtra(Constants.INTENT_ITEM_FILM, film.title)

        // Add the User Action to the Intent.
        returnIntent.putExtra(Constants.INTENT_ACTION, action)

        // Return RESULT_OK and the returningIntent back to ImportFilmActivity onActivityResult,
        // informing the activity that a film was saved and that it should finish itself.
        setResult(Activity.RESULT_OK, returnIntent)

        // Exit current activity and return to Main activity.
        finish()
    }
}