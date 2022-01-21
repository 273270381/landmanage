package com.suchness.landmanage.data.been

import android.annotation.SuppressLint
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * @author: hejunfeng
 * @date: 2021/10/9 0009
 */
@SuppressLint("ParcelCreator")
@Parcelize
data class LoginParams(var username: String, var password: String): Parcelable