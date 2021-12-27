package com.example.studenttally7.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.studenttally7.data.MyClass
import com.example.studenttally7.databinding.ItemClassBinding
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class MyClassAdapter(options: FirestoreRecyclerOptions<MyClass>) :
    FirestoreRecyclerAdapter<MyClass, MyClassAdapter.MyClassHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyClassHolder {
        val binding = ItemClassBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyClassHolder(binding)
    }

    override fun onBindViewHolder(holder: MyClassHolder, position: Int, model: MyClass) {
        holder.bind(model)
    }

    class MyClassHolder(private val binding: ItemClassBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(myClass: MyClass) {
            binding.apply {
                tvTitle.text = myClass.title
            }
        }
    }

}