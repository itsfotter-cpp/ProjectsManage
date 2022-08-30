package it.itsfotter.projectsmanage.models

import android.os.Parcel
import android.os.Parcelable

data class User (
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val image: String = "",
    /*
        We want to store the location of the image on the database.
        It is not really the database, but it is the Storage functionality of
        FireStore
     */
    val mobile: Long = 0,
    val fcmToken: String = "",
    var selected: Boolean = false
    /*
        fcmToken is a token of the users so that we know that it is the specific user
        that is logged in.
     */
        ): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readString()!!
    ) {
    }

    override fun describeContents() = 0

    override fun writeToParcel(p0: Parcel?, p1: Int): Unit = with(p0) {
        this?.writeString(id)
        this?.writeString(name)
        this?.writeString(email)
        this?.writeString(image)
        this?.writeLong(mobile)
        this?.writeString(fcmToken)

    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}

