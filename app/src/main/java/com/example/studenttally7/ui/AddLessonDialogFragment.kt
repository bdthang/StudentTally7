package com.example.studenttally7.ui

import android.app.Dialog
import android.content.Context
import androidx.fragment.app.DialogFragment

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.navArgs
import com.example.studenttally7.FirestoreCollectionName
import com.example.studenttally7.data.Lesson
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class AddLessonDialogFragment : DialogFragment() {
    private val args: AddLessonDialogFragmentArgs by navArgs()
    private lateinit var currentContext: Context

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val shortId = args.shortId
        currentContext = requireContext()

        return AlertDialog.Builder(requireContext())
            .setMessage("Add a new lesson?")
            .setPositiveButton("OK") { _, _ ->
                addLesson(shortId)
            }
            .setNegativeButton("Cancel") { _, _ -> }
            .create()
    }

    companion object {
        const val TAG = "AddLessonDialog"
    }

    private fun addLesson(shortId: String) {
        val classRef: CollectionReference = FirebaseFirestore.getInstance().collection(
            FirestoreCollectionName.CLASS_COLLECTION
        )

        classRef.whereEqualTo("shortId", shortId)
            .limit(1)
            .get()
            .addOnSuccessListener { classes ->
                when {
                    classes.isEmpty -> { // Class to be updated is not found
                        Toast.makeText(currentContext, "Class $shortId not found", Toast.LENGTH_SHORT)
                            .show()
                    }
                    else -> {
                        for (myClass in classes) {
                            val lessonRef = classRef.document(myClass.id)
                                .collection(FirestoreCollectionName.LESSON_COLLECTION)
                            lessonRef
                                .orderBy("end", Query.Direction.DESCENDING)
                                .limit(1)
                                .get()
                                .addOnSuccessListener { lessons ->
                                    if (lessons.isEmpty) { // No lesson created yet
                                        val lesson = Lesson()
                                        lesson.end =
                                            lesson.created + myClass.data["entryTimeLimit"].toString()
                                                .toLong() * 60000

                                        lessonRef.add(lesson)
                                    } else {
                                        for (lesson in lessons) {
                                            if (System.currentTimeMillis() < lesson.data["end"].toString()
                                                    .toLong()
                                            ) { // Still lesson going on
                                                Toast.makeText(
                                                    currentContext,
                                                    "Last lesson hasn't ended.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            } else {
                                                val newLesson = Lesson()
                                                newLesson.end =
                                                    newLesson.created + myClass.data["entryTimeLimit"].toString()
                                                        .toLong() * 60000

                                                lessonRef.add(newLesson)
                                                    .addOnSuccessListener {
                                                        Toast.makeText(
                                                            currentContext,
                                                            "Done.",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                            }

                                            break
                                        }
                                    }

                                }


                        }

                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: ", exception)
            }
    }

}