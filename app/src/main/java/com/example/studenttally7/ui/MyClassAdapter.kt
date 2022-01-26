package com.example.studenttally7.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.Navigation
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
                val sharedRefs = binding.root.context.getSharedPreferences("TallyAppPrefs", Context.MODE_PRIVATE)
                val role = sharedRefs.getString("role", "none")
                if (role == "teacher") {
                    buttonEditClass.setOnClickListener {
                        val action = ClassesFragmentDirections.actionClassesFragmentToAddEditClassFragment(myClass)
                        val navController = Navigation.findNavController(binding.root)
                        navController.navigate(action)
                    }
                    buttonRemoveClass.visibility = View.GONE
                } else {
                    buttonEditClass.visibility = View.GONE
                    buttonRemoveClass.setOnClickListener {
                        val editor = sharedRefs.edit()
                        val sharedPrefClasses = sharedRefs.getStringSet("classes", emptySet())!!.toMutableSet()
                        sharedPrefClasses.remove(myClass.shortId)
                        editor.putStringSet("classes", sharedPrefClasses)
                        editor.apply()
                    }

                }

                root.setOnClickListener {
                    val action = ClassesFragmentDirections.actionClassesFragmentToViewClassFragment(myClass.shortId)
                    val navController = Navigation.findNavController(binding.root)
                    navController.navigate(action)
                }
            }
        }
    }

}