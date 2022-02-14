package com.example.studenttally7.ui

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
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
                if (myClass.description == "") {
                    tvDescription.visibility = View.GONE
                }
                tvDescription.text = myClass.description

                val sharedRefs = binding.root.context.getSharedPreferences("TallyAppPrefs", Context.MODE_PRIVATE)
                val role = sharedRefs.getString("role", "none")
                if (role == "teacher") {
                    buttonEditClass.setOnClickListener {
                        val action = ClassesFragmentDirections.actionClassesFragmentToAddEditClassFragment(myClass)
                        val navController = Navigation.findNavController(binding.root)
                        navController.navigate(action)
                    }
                    buttonRemoveClass.visibility = View.GONE
                    buttonCheckinClass.visibility = View.GONE
                } else {
                    buttonEditClass.visibility = View.GONE
                    buttonRemoveClass.setOnClickListener {
                        studentRemoveClassDialog(myClass.shortId)
                    }

                    buttonCheckinClass.setOnClickListener {
                        val action = ClassesFragmentDirections.actionClassesFragmentToTallyingFragment(myClass.shortId)
                        val navController = Navigation.findNavController(binding.root)
                        navController.navigate(action)
                    }

                }

                root.setOnClickListener {
                    val action = ClassesFragmentDirections.actionClassesFragmentToViewClassFragment(myClass.shortId)
                    val navController = Navigation.findNavController(binding.root)
                    navController.navigate(action)
                }
            }
        }

        private fun studentRemoveClassDialog(shortId: String) {
            val dialog = AlertDialog.Builder(binding.root.context)
            dialog.setTitle("Remove this class")
                .setPositiveButton("OK") {_, _ ->
                    val sharedRefs = binding.root.context.getSharedPreferences("TallyAppPrefs", Context.MODE_PRIVATE)
                    val editor = sharedRefs.edit()
                    val sharedPrefClasses = sharedRefs.getStringSet("classes", emptySet())!!.toMutableSet()
                    sharedPrefClasses.remove(shortId)
                    editor.putStringSet("classes", sharedPrefClasses)
                    editor.apply()

                    val activity = binding.root.context as Activity
                    activity.finish()
                    activity.overridePendingTransition(0, 0)
                    activity.startActivity(activity.intent)
                    activity.overridePendingTransition(0, 0)
                }
                .setNegativeButton("Cancel") {_, _ -> }
                .show()
        }
    }

}