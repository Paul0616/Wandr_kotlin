package com.encorsa.wandr.database


import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class ListMediaDatabaseModel: ArrayList<MediaDatabaseModel>(), Parcelable

