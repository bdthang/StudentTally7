package com.example.studenttally7.data

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class MyClass(
    val shortId: String = "",
    val title: String = "Default_title",
    val entryTimeLimit: Int = 30,
    val isArchived: Boolean = false,
    val authorId: String = "",
    val created: Long = System.currentTimeMillis(),
): Parcelable {
    @IgnoredOnParcel
    val lessons: MutableList<Lesson> = ArrayList()
}
