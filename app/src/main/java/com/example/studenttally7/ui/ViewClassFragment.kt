package com.example.studenttally7.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.studenttally7.FirestoreCollectionName
import com.example.studenttally7.R
import com.example.studenttally7.databinding.FragmentViewClassBinding
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ViewClassFragment : Fragment(R.layout.fragment_view_class) {
    private var _binding: FragmentViewClassBinding? = null
    private val binding get() = _binding!!

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

        val classRef: CollectionReference =
            FirebaseFirestore.getInstance().collection(FirestoreCollectionName.CLASS_COLLECTION)

        classRef.whereEqualTo("shortId", "whto")
            .limit(1)
            .get()
            .addOnSuccessListener { classes ->
                for (myClass in classes) {

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
                            tableHeaderRow.addView(tvNothing)

                            for ((i,lesson) in lessons.withIndex()) {
                                lessonIdList.add(lesson.id)

                                val textView = TextView(context)
                                textView.layoutParams = TableRow.LayoutParams(
                                    TableRow.LayoutParams.WRAP_CONTENT,
                                    TableRow.LayoutParams.WRAP_CONTENT
                                )
                                textView.text = (i+1).toString()

                                tableHeaderRow.addView(textView)
                            }
                            binding.tableLayout.addView(tableHeaderRow)

                            for (lesson in lessons) {
                                Log.d("Firestore", "${lesson.id} => ${lesson.data}")

                                val entryRef: CollectionReference = lessonRef.document(lesson.id)
                                    .collection(FirestoreCollectionName.ENTRY_COLLECTION)
                                entryRef.get().addOnSuccessListener { entries ->
                                    for (entry in entries) {
                                        Log.d("Firestore", "${entry.id} => ${entry.data}")
                                        if (studentIdList.contains(entry.id)) { // This student row is already on the table
                                            val tableRow: TableRow = binding.tableLayout
                                                .getChildAt(studentIdList.indexOf(entry.id)+1) as TableRow
                                            val tvvv = tableRow.getChildAt(lessonIdList.indexOf(lesson.id)+1) as TextView
                                            tvvv.text="X"

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
                                            tableDataRow.addView(studentIdTextView)

                                            repeat(lessons.size()) {
                                                val defaultTextView = TextView(context)
                                                defaultTextView.layoutParams = TableRow.LayoutParams(
                                                    TableRow.LayoutParams.WRAP_CONTENT,
                                                    TableRow.LayoutParams.WRAP_CONTENT
                                                )
                                                defaultTextView.text = "O"
                                                tableDataRow.addView(defaultTextView)
                                            }

                                            binding.tableLayout.addView(tableDataRow)

                                            // Change the status now.
                                            // Get the row of the student
                                            val tableRow: TableRow = binding.tableLayout
                                                .getChildAt(studentIdList.indexOf(entry.id)+1) as TableRow
                                            val tvvv = tableRow.getChildAt(lessonIdList.indexOf(lesson.id)+1) as TextView
                                            tvvv.text="X"
                                        }
                                    }
                                }
                            }


                        }

                }
            }

    }
}