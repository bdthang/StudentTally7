package com.example.studenttally7.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.studenttally7.R
import com.example.studenttally7.data.MyClass
import com.example.studenttally7.databinding.FragmentAddEditClassBinding
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class AddEditClassFragment : Fragment(R.layout.fragment_add_edit_class) {
    private var _binding: FragmentAddEditClassBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddEditClassBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonConfirmAddClass.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val shortId = binding.etShortId.text.toString().trim()
            val timeLimit = binding.etTimeLimit.text.toString().trim()
            val isArchived = binding.checkboxArchived.isChecked
            val currentContext = context

            if (title.isEmpty() || shortId.isEmpty() || timeLimit.isEmpty()) {
                Toast.makeText(context, "Please enter missing values.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Check if short id already exists.
            val classRef: CollectionReference = FirebaseFirestore.getInstance().collection("Class")

            classRef.whereEqualTo("shortId", shortId)
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        Toast.makeText(currentContext, "Short ID already existed", Toast.LENGTH_SHORT).show()
                    } else {
                        // Save class to Firestore
                        val newClass = MyClass(
                            shortId = shortId,
                            title = title,
                            entryTimeLimit = timeLimit.toInt(),
                            isArchived = isArchived
                        )
                        classRef.add(newClass)
                        findNavController().navigateUp()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("Firestore", "Error getting documents: ", exception)
                }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}