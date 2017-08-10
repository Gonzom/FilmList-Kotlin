package tech.gonzo.filmlist.utils

/**
 * Created by Gonny on 31/1/2017.
 * -
 */
object Constants {

    /**
     * Intent Constants.
     */
    val INTENT_ITEM_FILM = "intentItemFilm"
    val INTENT_ACTION = "action"

    /**
     * The ViewFilmFragment was started from an Edit film action.
     */
    val ACTION_EDIT = 1

    /**
     * The ViewFilmFragment was started from an Import film action.
     */
    val ACTION_IMPORT = 2

    /**
     * The ViewFilmFragment was started from an Updated film action.
     */
    val ACTION_UPDATE = 3

    /**
     * API Error Constants.
     */
    val ERROR_MOVIE_NOT_FOUND = "Movie not found!"

    /**
     * Async Task Constants.
     */
    val TASK_DOWNLOAD_IMAGE = 3
    val TASK_SAVE_IMAGE = 4
}