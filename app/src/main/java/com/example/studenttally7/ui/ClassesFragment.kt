package com.example.studenttally7.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.studenttally7.R
import com.example.studenttally7.data.MyClass
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ClassesFragment : Fragment(R.layout.fragment_classes) {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val classRef: CollectionReference = db.collection("Class")

    private lateinit var classAdapter: MyClassAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view_classes)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val query: Query = classRef.orderBy("created", Query.Direction.DESCENDING)

        val options: FirestoreRecyclerOptions<MyClass> = FirestoreRecyclerOptions.Builder<MyClass>()
            .setQuery(query, MyClass::class.java)
            .build()

        classAdapter = MyClassAdapter(options)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = classAdapter
    }

    override fun onStart() {
        super.onStart()
        classAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        classAdapter.stopListening()
    }
}