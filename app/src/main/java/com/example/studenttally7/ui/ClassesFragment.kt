package com.example.studenttally7.ui

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_options_classes, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.option_signout) {
            FirebaseAuth.getInstance().signOut()
            val action = ClassesFragmentDirections.actionClassesFragmentToLoginFragment()
            findNavController().navigate(action)
            return true
        } else {
            return super.onOptionsItemSelected(item)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            val action = ClassesFragmentDirections.actionClassesFragmentToLoginFragment()
            findNavController().navigate(action)
        } else {
            binding.tvUserName.text = currentUser.displayName.toString()
            setupRecyclerView(currentUser.uid)
            Log.d("Auth", currentUser.uid)
        }

        binding.fabAddClass.setOnClickListener {
            val action = ClassesFragmentDirections.actionClassesFragmentToAddEditClassFragment()
            findNavController().navigate(action)
        }

    }

    private fun setupRecyclerView(authorId: String) {
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