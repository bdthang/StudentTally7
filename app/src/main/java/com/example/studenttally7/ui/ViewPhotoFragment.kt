package com.example.studenttally7.ui

import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.studenttally7.R
import com.example.studenttally7.databinding.FragmentViewPhotoBinding
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.lang.Exception

class ViewPhotoFragment : Fragment(R.layout.fragment_view_photo) {
    private var _binding: FragmentViewPhotoBinding? = null
    private val binding get() = _binding!!

    private val args: ViewPhotoFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewPhotoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val photoUrl = args.photoUrl

        val photo: StorageReference = FirebaseStorage.getInstance().reference.child(photoUrl)
        photo.downloadUrl.addOnSuccessListener {
            val width = Resources.getSystem().displayMetrics.widthPixels
            Picasso.get().load(it.toString())
                .resize(width, 0)
                .into(binding.imgViewPhoto)

        }
    }

}