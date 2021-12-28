package com.example.studenttally7.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.studenttally7.R
import com.example.studenttally7.data.MyClass
import com.example.studenttally7.databinding.FragmentClassesBinding
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ClassesFragment : Fragment(R.layout.fragment_classes) {
    private val classRef: CollectionReference = FirebaseFirestore.getInstance().collection("Class")

    private var _binding: FragmentClassesBinding? = null
    private val binding get() = _binding!!

    private lateinit var classAdapter: MyClassAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentClassesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fabAddClass.setOnClickListener {
            val action = ClassesFragmentDirections.actionClassesFragmentToAddEditClassFragment()
            findNavController().navigate(action)
        }
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val query: Query = classRef.orderBy("created", Query.Direction.DESCENDING)

        val options: FirestoreRecyclerOptions<MyClass> = FirestoreRecyclerOptions.Builder<MyClass>()
            .setQuery(query, MyClass::class.java)
            .build()

        classAdapter = MyClassAdapter(options)
        binding.recyclerViewClasses.setHasFixedSize(true)
        binding.recyclerViewClasses.adapter = classAdapter
    }

    override fun onStart() {
        super.onStart()
        classAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        classAdapter.stopListening()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}