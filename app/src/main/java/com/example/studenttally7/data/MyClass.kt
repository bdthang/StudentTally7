package com.example.studenttally7.data

import java.text.DateFormat

data class MyClass(
    val shortId: String = "",
    val title: String = "Default_title",
    val entryTimeLimit: Int = 30,
    val isArchived: Boolean = false,
    val authorId: String = "",
    val created: Long = System.currentTimeMillis(),
    val lessons: MutableList<Lesson> = ArrayList()
){
    val createdDateFormatted: String
        get() = DateFormat.getDateTimeInstance().format(created)
}
