package com.example.studenttally7.data

import com.google.firebase.firestore.Exclude

data class Lesson(
    val created: Long = System.currentTimeMillis()
){
    @get:Exclude
    var tallies: MutableList<TallyEntry> = ArrayList()
}
