package tech.gonzo.filmlist.models

import android.os.Parcel
import android.os.Parcelable

/**
 * This file represents the attributes that describe the ShortFilm and Film objects.
 */
class Film(
        imdbId: String,
        title: String,
        year: Int,
        var id: Long = -1L,
        var timestamp: Long = 0L,
        var director: String,
        var plot: String?,
        var posterExternalUri: String,
        var posterInternalUri: String? = null,
        var seen: Boolean = false,
        var userRating: Float = 0f,
        var imdbRating: Float
) : ShortFilm(imdbId, title, year), Parcelable {
    companion object {
        @JvmField val CREATOR: Parcelable.Creator<Film> = object : Parcelable.Creator<Film> {
            override fun createFromParcel(source: Parcel): Film = Film(source)
            override fun newArray(size: Int): Array<Film?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readInt(),
            source.readValue(Long::class.java.classLoader) as Long,
            source.readValue(Long::class.java.classLoader) as Long,
            source.readString(),
            source.readString(),
            source.readString(),
            source.readString(),
            1 == source.readInt(),
            source.readFloat(),
            source.readFloat()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(imdbId)
        dest.writeString(title)
        dest.writeInt(year)
        dest.writeValue(id)
        dest.writeValue(timestamp)
        dest.writeString(director)
        dest.writeString(plot)
        dest.writeString(posterExternalUri)
        dest.writeString(posterInternalUri)
        dest.writeInt((if (seen) 1 else 0))
        dest.writeFloat(userRating)
        dest.writeFloat(imdbRating)
    }
}

open class ShortFilm(
        val imdbId: String,
        var title: String,
        var year: Int
) : Parcelable {
    companion object {
        @JvmField val CREATOR: Parcelable.Creator<ShortFilm> = object : Parcelable.Creator<ShortFilm> {
            override fun createFromParcel(source: Parcel): ShortFilm = ShortFilm(source)
            override fun newArray(size: Int): Array<ShortFilm?> = arrayOfNulls(size)
        }
    }

    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(imdbId)
        dest.writeString(title)
        dest.writeInt(year)
    }
}