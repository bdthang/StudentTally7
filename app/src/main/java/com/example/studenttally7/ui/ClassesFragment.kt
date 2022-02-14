package com.example.studenttally7.ui

import android.content.Context.MODE_PRIVATE
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.studenttally7.FirestoreCollectionName
import com.example.studenttally7.R
import com.example.studenttally7.data.MyClass
import com.example.studenttally7.databinding.FragmentClassesBinding
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
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
            setupRecyclerView()

            binding.fabAddClass.setOnClickListener {
                studentAddClassDialog()
            }
        }
    }

    private fun studentAddClassDialog() {
        val dialog = AlertDialog.Builder(requireContext())
        val etClassShortId = EditText(requireContext())
        etClassShortId.inputType = InputType.TYPE_CLASS_TEXT
        etClassShortId.setSingleLine()

        val container = FrameLayout(requireContext())
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(32,0,32,0)
        etClassShortId.layoutParams = params
        container.addView(etClassShortId)

        dialog.setTitle("Add a new class")
            .setMessage("Get the class code from your teacher.")
            .setView(container)
            .setPositiveButton("OK") {_, _ ->
                studentAddClass(etClassShortId.text.toString())
            }
            .setNegativeButton("Cancel") {_, _ -> }
            .show()
    }

    private fun studentAddClass(classShortId: String) {
        // Check if class exists
        val classPref = FirebaseFirestore.getInstance().collection(FirestoreCollectionName.CLASS_COLLECTION)
        classPref.whereEqualTo("shortId", classShortId)
            .limit(1)
            .get()
            .addOnSuccessListener { classes ->
                if (classes.isEmpty) {
                    Toast.makeText(requireContext(), "Class not exist", Toast.LENGTH_SHORT).show()
                } else {
                    for (_class in classes) {
                        val sharedRefs = requireContext().getSharedPreferences("TallyAppPrefs", MODE_PRIVATE)
                        val editor = sharedRefs.edit()
                        val sharedPrefClasses = sharedRefs.getStringSet("classes", emptySet())!!.toMutableSet()
                        sharedPrefClasses.add(classShortId)
                        editor.putStringSet("classes", sharedPrefClasses)
                        editor.apply()
                        break
                    }
                    requireActivity().finish()
                    requireActivity().overridePendingTransition(0, 0)
                    startActivity(requireActivity().intent)
                    requireActivity().overridePendingTransition(0, 0)
                }
            }
    }

    private fun setupRecyclerView() { // For student, get list from local
        val prefs = requireContext().getSharedPreferences("TallyAppPrefs", MODE_PRIVATE)
        val classes = prefs.getStringSet("classes", emptySet())
        if (classes!!.isEmpty()) {
            return
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