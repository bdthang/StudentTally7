package com.example.studenttally7.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.studenttally7.R
import com.example.studenttally7.data.MyClass
import com.example.studenttally7.databinding.FragmentAddEditClassBinding
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class AddEditClassFragment : Fragment(R.layout.fragment_add_edit_class) {
    private var _binding: FragmentAddEditClassBinding? = null
    private val binding get() = _binding!!
    private val args: AddEditClassFragmentArgs by navArgs()
    private var currentClass: MyClass = MyClass()
    private var oldShortId: String = ""

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

        if (args.classToEdit != null) { // Edit class
            val classToEdit = args.classToEdit!!
            binding.etTitle.setText(classToEdit.title)
            binding.etShortId.setText(classToEdit.shortId)
            binding.etTimeLimit.setText(classToEdit.entryTimeLimit.toString())
            binding.checkboxArchived.isChecked = classToEdit.isArchived
            oldShortId = classToEdit.shortId

            binding.buttonConfirmClass.setOnClickListener {
                getClassFromView()
                updateClass()
            }

            binding.buttonDeleteClass.visibility = View.VISIBLE
            binding.buttonDeleteClass.setOnClickListener {
                val action = AddEditClassFragmentDirections.actionAddEditClassFragmentToDeleteClassFragment(oldShortId);
                findNavController().navigate(action)
            }
        } else {
            binding.buttonConfirmClass.setOnClickListener {
                getClassFromView()
                addClass()
            }
        }
    }

    private fun getClassFromView() {
        val title = binding.etTitle.text.toString().trim()
        val shortId = binding.etShortId.text.toString().trim()
        val timeLimit = binding.etTimeLimit.text.toString().trim()
        val isArchived = binding.checkboxArchived.isChecked

        if (title.isEmpty() || shortId.isEmpty() || timeLimit.isEmpty()) {
            Toast.makeText(context, "Please enter missing values.", Toast.LENGTH_SHORT).show()
            return
        }

        currentClass = MyClass(
            shortId = shortId,
            title = title,
            entryTimeLimit = timeLimit.toInt(),
            isArchived = isArchived
        )

    }

    private fun updateClass() {
        val classRef: CollectionReference = FirebaseFirestore.getInstance().collection("Class")

        classRef.whereEqualTo("shortId", oldShortId)
            .get()
            .addOnSuccessListener { documents ->
                when {
                    documents.isEmpty -> { // Class to be updated is not found
                        Log.e("Firestore", "Class to be updated not found, shortID = ${oldShortId}")
                    }
                    documents.size() > 1 -> { // 2 Classes with same short id
                        Log.e("Firestore", "More than one class with this same uid???")
                    }
                    else -> {
                        for (document in documents) {
                            classRef.document(document.id).update(
                                "isArchived", currentClass.isArchived,
                                "title", currentClass.title,
                                "entryTimeLimit", currentClass.entryTimeLimit,
                                "shortId", currentClass.shortId
                            )
                                .addOnSuccessListener { Log.d("Firestore", "DocumentSnapshot successfully updated!") }
                                .addOnFailureListener { e -> Log.w("Firestore", "Error updating document", e) }
                        }

                        findNavController().navigateUp()
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: ", exception)
                findNavController().navigateUp()
            }
    }

    private fun addClass() {
        val currentContext = context
        val classRef: CollectionReference = FirebaseFirestore.getInstance().collection("Class")

        classRef.whereEqualTo("shortId", currentClass.shortId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // Correct situation
                    classRef.add(currentClass)
                        .addOnSuccessListener { documentReference ->
                            Log.d("Firestore", "DocumentSnapshot written with ID: ${documentReference.id}")
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error adding document", e)
                        }
                    findNavController().navigateUp()
                } else {
                    // Short id existed
                    Toast.makeText(currentContext, "Short ID already existed", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: ", exception)
                findNavController().navigateUp()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}