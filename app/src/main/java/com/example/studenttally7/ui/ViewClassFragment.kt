package com.example.studenttally7.ui

import android.app.Activity
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.opengl.Visibility
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
    private lateinit var _context: Context

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _context = requireContext()
        _binding = FragmentViewClassBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getData(args.shortId)

        binding.fabAddLesson.setOnClickListener {
//            addLesson()
            val action = ViewClassFragmentDirections.actionViewClassFragmentToAddLessonDialogFragment(args.shortId)
            findNavController().navigate(action)
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

                            val tableHeaderRow = TableRow(_context)
                            tableHeaderRow.layoutParams = TableLayout.LayoutParams(
                                TableLayout.LayoutParams.WRAP_CONTENT,
                                TableLayout.LayoutParams.WRAP_CONTENT
                            )
                            val tvNothing = TextView(_context)
                            tvNothing.layoutParams = TableRow.LayoutParams(
                                TableRow.LayoutParams.WRAP_CONTENT,
                                TableRow.LayoutParams.WRAP_CONTENT
                            )
                            tvNothing.setPadding(8)
                            tvNothing.text = "No."
                            tableHeaderRow.addView(tvNothing)

                            for ((i, lesson) in lessons.withIndex()) {
                                lessonIdList.add(lesson.id)

                                val textView = TextView(_context)
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

                                            val tableDataRow = TableRow(_context)
                                            tableDataRow.layoutParams = TableLayout.LayoutParams(
                                                TableLayout.LayoutParams.WRAP_CONTENT,
                                                TableLayout.LayoutParams.WRAP_CONTENT
                                            )

                                            val studentIdTextView = TextView(_context)
                                            studentIdTextView.layoutParams = TableRow.LayoutParams(
                                                TableRow.LayoutParams.WRAP_CONTENT,
                                                TableRow.LayoutParams.WRAP_CONTENT
                                            )
                                            studentIdTextView.text = entry.id
                                            studentIdTextView.setPadding(8)
                                            tableDataRow.addView(studentIdTextView)

                                            repeat(lessons.size()) {
                                                val defaultTextView = TextView(_context)
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