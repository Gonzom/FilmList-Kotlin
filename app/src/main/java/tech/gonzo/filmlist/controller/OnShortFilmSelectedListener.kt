package tech.gonzo.filmlist.controller

import tech.gonzo.filmlist.models.ShortFilm

/**
 * Created by Gonny on 2/2/2017.
 * -
 */
interface OnShortFilmSelectedListener {

    /**
     * Called when an item in the list has been clicked.
     *
     * @param shortFilm The ShortFilm object selected.
     */
    fun onItemClick(shortFilm: ShortFilm)
}