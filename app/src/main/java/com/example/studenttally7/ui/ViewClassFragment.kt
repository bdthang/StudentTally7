package com.example.studenttally7.ui

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.studenttally7.FirestoreCollectionName
import com.example.studenttally7.R
import com.example.studenttally7.data.Lesson
import com.example.studenttally7.databinding.FragmentViewClassBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ViewClassFragment : Fragment(R.layout.fragment_view_class) {
    private var _binding: FragmentViewClassBinding? = null
    private val binding get() = _binding!!
    private val args: ViewClassFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewClassBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (args.shortId != "default") {
            binding.etShortId.setText(args.shortId!!)
            getData(args.shortId!!)
        }

        binding.fabAddLesson.setOnClickListener {
//            addLesson()
            val shortId = binding.etShortId.text.toString()
            val action = ViewClassFragmentDirections.actionViewClassFragmentToAddLessonDialogFragment(shortId)
            findNavController().navigate(action)
        }

        binding.buttonConfirmClass.setOnClickListener {
            getData(binding.etShortId.text.toString())

            val imm: InputMethodManager =
                context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

    }

    private fun addLesson() {
        val shortId = binding.etShortId.text.toString()

        val classRef: CollectionReference = FirebaseFirestore.getInstance().collection(
            FirestoreCollectionName.CLASS_COLLECTION
        )

        classRef.whereEqualTo("shortId", shortId)
            .limit(1)
            .get()
            .addOnSuccessListener { classes ->
                when {
                    classes.isEmpty -> { // Class to be updated is not found
                        Toast.makeText(context, "Class $shortId not found", Toast.LENGTH_SHORT)
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
                                                    context,
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
                                                            context,
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

    private fun getData(shortId: String) {

        val classRef: CollectionReference =
            FirebaseFirestore.getInstance().collection(FirestoreCollectionName.CLASS_COLLECTION)

        classRef.whereEqualTo("shortId", shortId)
            .limit(1)
            .get()
            .addOnSuccessListener { classes ->
                for (myClass in classes) {
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    if (currentUser != null && currentUser.uid == myClass.data["authorId"]) {
                        binding.fabAddLesson.visibility = View.VISIBLE
                    } else {
                        binding.fabAddLesson.visibility = View.GONE
                    }

                    val lessonRef: CollectionReference = classRef.document(myClass.id)
                        .collection(FirestoreCollectionName.LESSON_COLLECTION)
                    lessonRef
                        .orderBy("created", Query.Direction.ASCENDING)
                        .get()
                        .addOnSuccessListener { lessons ->
                            val lessonIdList = ArrayList<String>()
                            val studentIdList = ArrayList<String>()

                            val tableHeaderRow = TableRow(context)
                            tableHeaderRow.layoutParams = TableLayout.LayoutParams(
                                TableLayout.LayoutParams.WRAP_CONTENT,
                                TableLayout.LayoutParams.WRAP_CONTENT
                            )
                            val tvNothing = TextView(context)
                            tvNothing.layoutParams = TableRow.LayoutParams(
                                TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT
                            )
                            tvNothing.setPadding(8)
                            tvNothing.text = "No."
                            tableHeaderRow.addView(tvNothing)

                            for ((i, lesson) in lessons.withIndex()) {
                                lessonIdList.add(lesson.id)

                                val textView = TextView(context)
                                textView.layoutParams = TableRow.LayoutParams(
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                                )
                                textView.text = (i + 1).toString()
                                textView.setPadding(8)

                                tableHeaderRow.addView(textView)
                            }
                            binding.tableLayout.addView(tableHeaderRow)

                            for (lesson in lessons) {

                                val entryRef: CollectionReference = lessonRef.document(lesson.id)
                                    .collection(FirestoreCollectionName.ENTRY_COLLECTION)
                                entryRef.get().addOnSuccessListener { entries ->
                                    for (entry in entries) {
                                        if (studentIdList.contains(entry.id)) { // This student row is already on the table
                                            val tableRow: TableRow = binding.tableLayout
                                                .getChildAt(studentIdList.indexOf(entry.id) + 1) as TableRow
                                            val tvvv =
                                                tableRow.getChildAt(lessonIdList.indexOf(lesson.id) + 1) as TextView
                                            tvvv.text = "✓"
                                            tvvv.setPadding(8)
                                            tvvv.setOnClickListener {
                                                val action = ViewClassFragmentDirections.actionViewClassFragmentToViewPhotoFragment(entry.data["photoUrl"].toString())
                                                findNavController().navigate(action)
                                            }

                                        } else { // Make new table row if not.
                                            studentIdList.add(entry.id)

                                            val tableDataRow = TableRow(context)
                                            tableDataRow.layoutParams = TableLayout.LayoutParams(
                                                TableLayout.LayoutParams.WRAP_CONTENT,
                                                TableLayout.LayoutParams.WRAP_CONTENT
                                            )

                                            val studentIdTextView = TextView(context)
                                            studentIdTextView.layoutParams = TableRow.LayoutParams(
                                                TableRow.LayoutParams.WRAP_CONTENT,
                                                TableRow.LayoutParams.WRAP_CONTENT
                                            )
                                            studentIdTextView.text = entry.id
                                            studentIdTextView.setPadding(8)
                                            tableDataRow.addView(studentIdTextView)

                                            repeat(lessons.size()) {
                                                val defaultTextView = TextView(context)
                                                defaultTextView.layoutParams =
                                                    TableRow.LayoutParams(
                                                        TableRow.LayoutParams.WRAP_CONTENT,
                                                        TableRow.LayoutParams.WRAP_CONTENT
                                                    )
                                                defaultTextView.text = ""
                                                defaultTextView.setPadding(8)
                                                tableDataRow.addView(defaultTextView)

//                                                val checkImg = ImageView(context)
//                                                val checkImg = ImageView(ContextThemeWrapper(context, R.style.Widget_AppCompat_ActionButton), null, 0)
//                                                checkImg.setImageResource(R.drawable.ic_baseline_check_circle_24)
//                                                checkImg.setPadding(8)
//                                                tableDataRow.addView(checkImg)
                                            }

                                            binding.tableLayout.addView(tableDataRow)

                                            // Change the status now.
                                            // Get the row of the student
                                            val tableRow: TableRow = binding.tableLayout
                                                .getChildAt(studentIdList.indexOf(entry.id) + 1) as TableRow
                                            val tvvv =
                                                tableRow.getChildAt(lessonIdList.indexOf(lesson.id) + 1) as TextView
                                            tvvv.text = "✓"
                                            tvvv.setPadding(8)
                                            tvvv.setOnClickListener {
                                                val action = ViewClassFragmentDirections.actionViewClassFragmentToViewPhotoFragment(entry.data["photoUrl"].toString())
                                                findNavController().navigate(action)
                                            }
                                        }
                                    }
                                }
                            }


                        }

                }
            }

    }
}