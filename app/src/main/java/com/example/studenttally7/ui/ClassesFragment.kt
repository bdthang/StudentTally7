package com.example.studenttally7.ui

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.studenttally7.R
import com.example.studenttally7.data.MyClass
import com.example.studenttally7.databinding.FragmentClassesBinding
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ClassesFragment : Fragment(R.layout.fragment_classes) {
    private val classRef: CollectionReference = FirebaseFirestore.getInstance().collection("Class")

    private var _binding: FragmentClassesBinding? = null
    private val binding get() = _binding!!

    private lateinit var classAdapter: MyClassAdapter
    private lateinit var auth: FirebaseAuth

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

        val role = requireContext().getSharedPreferences("TallyAppPrefs", MODE_PRIVATE).getString("role", "none")
        if (role == "teacher") {
            auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            if (currentUser == null) {
                val action = ClassesFragmentDirections.actionClassesFragmentToLoginFragment()
                findNavController().navigate(action)
            } else {
                setupRecyclerView(currentUser.uid)
                Log.d("Auth", currentUser.uid)
            }

            binding.fabAddClass.setOnClickListener {
                val action = ClassesFragmentDirections.actionClassesFragmentToAddEditClassFragment()
                findNavController().navigate(action)
            }

        } else {
            binding.fabAddClass.visibility = View.GONE
            setupRecyclerView()
        }
    }

    private fun setupRecyclerView() { // For student, get list from local
        val prefs = requireContext().getSharedPreferences("TallyAppPrefs", MODE_PRIVATE)
        var classes = prefs.getStringSet("classes", emptySet())
        if (classes!!.isEmpty()) {
            classes = setOf("example")
        }

        val query: Query = classRef
            .whereIn("shortId", classes.take(10).toList())
            .orderBy("created", Query.Direction.DESCENDING)

        val options: FirestoreRecyclerOptions<MyClass> = FirestoreRecyclerOptions.Builder<MyClass>()
            .setQuery(query, MyClass::class.java)
            .build()

        classAdapter = MyClassAdapter(options)
        binding.recyclerViewClasses.setHasFixedSize(true)
        binding.recyclerViewClasses.adapter = classAdapter
    }

    private fun setupRecyclerView(authorId: String) { // For teacher, get list from online database
        val query: Query = classRef
            .whereEqualTo("authorId", authorId)
            .orderBy("created", Query.Direction.DESCENDING)

        val options: FirestoreRecyclerOptions<MyClass> = FirestoreRecyclerOptions.Builder<MyClass>()
            .setQuery(query, MyClass::class.java)
            .build()

        classAdapter = MyClassAdapter(options)
        binding.recyclerViewClasses.setHasFixedSize(true)
        binding.recyclerViewClasses.adapter = classAdapter
    }

    override fun onStart() {
        super.onStart()
        if (this::classAdapter.isInitialized) {
            classAdapter.startListening()
        }
    }

    override fun onStop() {
        if (this::classAdapter.isInitialized) {
            classAdapter.stopListening()
        }
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}