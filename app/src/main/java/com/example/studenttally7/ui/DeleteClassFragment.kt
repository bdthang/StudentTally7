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
import com.example.studenttally7.databinding.FragmentDeleteClassBinding
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class DeleteClassFragment : Fragment() {
    private val args: DeleteClassFragmentArgs by navArgs()

    private var _binding: FragmentDeleteClassBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDeleteClassBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonCancel.setOnClickListener{
            val action = DeleteClassFragmentDirections.actionDeleteClassFragmentToClassesFragment()
            findNavController().navigate(action)
        }

        binding.buttonConfirmDeletion.setOnClickListener{
            deleteClass()
            val action = DeleteClassFragmentDirections.actionDeleteClassFragmentToClassesFragment()
            findNavController().navigate(action)
        }
    }

    private fun deleteClass() {
        val classRef: CollectionReference = FirebaseFirestore.getInstance().collection("Class")

        classRef.whereEqualTo("shortId", args.classIdToDelete)
            .get()
            .addOnSuccessListener { documents ->
                when {
                    documents.isEmpty -> { // Class to be updated is not found
                        Log.e("Firestore", "Class to be deleted not found, shortID = ${args.classIdToDelete}")
                    }
                    documents.size() > 1 -> { // 2 Classes with same short id
                        Log.e("Firestore", "More than one class with this same uid???")
                    }
                    else -> {
                        for (document in documents) {
                            classRef.document(document.id).delete()
                                .addOnSuccessListener { Log.d("Firestore", "DocumentSnapshot successfully deleted!") }
                                .addOnFailureListener { e -> Log.w("Firestore", "Error deleting document", e) }
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: ", exception)
            }
    }

}
