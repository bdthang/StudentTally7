package com.example.studenttally7.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.studenttally7.FirestoreCollectionName
import com.example.studenttally7.R
import com.example.studenttally7.data.TallyEntry
import com.example.studenttally7.databinding.FragmentTallyingBinding
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class TallyingFragment : Fragment(R.layout.fragment_tallying) {
    private var _binding: FragmentTallyingBinding? = null
    private val binding get() = _binding!!
    private var studentId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTallyingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Int>("studentId")
            ?.observe(viewLifecycleOwner) { studentId ->
                if (studentId in 10000000..99999999) {
                    binding.tvStudentIdResult.text = "OK"
                    this.studentId = studentId
                } else {
                    binding.tvStudentIdResult.text = "Not OK"
                }
            }

        binding.buttonScanCode.setOnClickListener {
            val action = TallyingFragmentDirections.actionTallyingFragmentToScannerFragment()
            findNavController().navigate(action)
        }

        binding.buttonSubmitTally.setOnClickListener {
            saveTallyData()
        }
    }

    private fun saveTallyData() {
        val shortId = binding.etShortId.text.toString().trim()
        if (studentId == 0) {
            Toast.makeText(context, "Invalid code.", Toast.LENGTH_SHORT).show()
            return
        }
        if (shortId.isEmpty()) {
            Toast.makeText(context, "Fill in short id", Toast.LENGTH_SHORT).show()
            return
        }

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
                            // Check the latest lesson
                            val lessonRef = classRef.document(myClass.id)
                                .collection(FirestoreCollectionName.LESSON_COLLECTION)
                            lessonRef.orderBy("end", Query.Direction.DESCENDING).limit(1).get()
                                .addOnSuccessListener { lessons ->
                                    if (lessons.isEmpty) {
                                        Toast.makeText(
                                            context,
                                            "No lesson going on.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        for (lesson in lessons) {
                                            if (System.currentTimeMillis() < lesson.data["end"].toString()
                                                    .toLong()
                                            ) { // On going lesson
                                                val newEntry = TallyEntry()
                                                val entryRef = lessonRef.document(lesson.id)
                                                    .collection(FirestoreCollectionName.ENTRY_COLLECTION)
                                                entryRef.document(studentId.toString())
                                                    .set(newEntry)
                                                Toast.makeText(context, "Done", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "No lesson going on", Toast.LENGTH_SHORT).show()
                                            }
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