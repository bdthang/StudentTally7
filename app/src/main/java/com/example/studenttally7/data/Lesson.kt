package com.example.studenttally7.data

import java.text.DateFormat

data class Lesson(
    val created: Long = System.currentTimeMillis(),
    val tallies: MutableList<TallyEntry> = ArrayList()
){
}
