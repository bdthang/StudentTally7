package com.example.studenttally7.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.studenttally7.FirestoreCollectionName
import com.example.studenttally7.R
import com.example.studenttally7.data.TallyEntry
import com.example.studenttally7.databinding.FragmentTallyingBinding
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.io.ByteArrayOutputStream
import java.lang.NullPointerException


class TallyingFragment : Fragment(R.layout.fragment_tallying) {
    private var _binding: FragmentTallyingBinding? = null
    private val binding get() = _binding!!
    private var studentId: Int = 0
    private lateinit var photo: Bitmap
    private val CAMERA_REQUEST_CODE = 101

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTallyingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Int>("studentId")
            ?.observe(viewLifecycleOwner) { studentId ->
                if (studentId in 10000000..99999999) {
                    binding.tvStudentIdResult.text = "OK"
                    this.studentId = studentId
                } else {
                    binding.tvStudentIdResult.text = "Not OK"
                }
            }

        binding.buttonScanCode.setOnClickListener {
            val action = TallyingFragmentDirections.actionTallyingFragmentToScannerFragment()
            findNavController().navigate(action)
        }

        binding.buttonSubmitTally.setOnClickListener {
            saveTallyData()
        }

        binding.buttonTakePhoto.setOnClickListener {
            askForCamera()

        }
    }

    private fun saveTallyData() {
        val shortId = binding.etShortId.text.toString().trim()
        if (studentId == 0) {
            Toast.makeText(context, "Invalid code.", Toast.LENGTH_SHORT).show()
            return
        }
        if (shortId.isEmpty()) {
            Toast.makeText(context, "Fill in short id", Toast.LENGTH_SHORT).show()
            return
        }
        if (!this::photo.isInitialized) {
            Toast.makeText(context, "Photo not found.", Toast.LENGTH_SHORT).show()
            return
        }

        val classRef: CollectionReference = FirebaseFirestore.getInstance().collection(
            FirestoreCollectionName.CLASS_COLLECTION
        )

        classRef.whereEqualTo("shortId", shortId)
            .limit(1)
            .get()
            .addOnSuccessListener { classes ->
                when {
                    classes.isEmpty -> { // Class to be updated is not found
                        Toast.makeText(context, "Class $shortId not found", Toast.LENGTH_SHORT)
                            .show()
                    }
                    else -> {
                        for (myClass in classes) {
                            // Check the latest lesson
                            val lessonRef = classRef.document(myClass.id)
                                .collection(FirestoreCollectionName.LESSON_COLLECTION)
                            lessonRef.orderBy("end", Query.Direction.DESCENDING).limit(1).get()
                                .addOnSuccessListener { lessons ->
                                    if (lessons.isEmpty) {
                                        Toast.makeText(
                                            context,
                                            "No lesson going on.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        for (lesson in lessons) {
                                            if (System.currentTimeMillis() < lesson.data["end"].toString()
                                                    .toLong()
                                            ) { // On going lesson
                                                val photoUrl =
                                                    myClass.id + "/" + lesson.id + "/" + studentId.toString() + ".jpg"
                                                val newEntry = TallyEntry(photoUrl = photoUrl)
                                                saveImageToStorage(photoUrl)

                                                val entryRef = lessonRef.document(lesson.id)
                                                    .collection(FirestoreCollectionName.ENTRY_COLLECTION)
                                                entryRef.document(studentId.toString())
                                                    .set(newEntry)
                                                Toast.makeText(context, "Done", Toast.LENGTH_SHORT)
                                                    .show()
                                            } else {
                                                Toast.makeText(
                                                    context,
                                                    "No lesson going on",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    }
                                }

                        }

                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: ", exception)
            }
    }

    private fun askForCamera() {
        val permission =
            ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            makeRequest()
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        val camera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        camera.putExtra("android.intent.extras.CAMERA_FACING", 1)
//        camera.putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
        startActivityForResult(camera, CAMERA_REQUEST_CODE)
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(android.Manifest.permission.CAMERA),
            CAMERA_REQUEST_CODE
        )
    }

    private fun saveImageToStorage(photoUrl: String) {
        val storage: FirebaseStorage = FirebaseStorage.getInstance()
        val storageRef = storage.reference.child(photoUrl)
        val baos = ByteArrayOutputStream()
        photo.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask: UploadTask = storageRef.putBytes(data)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
            Toast.makeText(context, "Image upload unsuccessful.", Toast.LENGTH_SHORT).show()
        }
            .addOnSuccessListener { taskSnapshot ->
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
//                val downloadUrl: Uri = taskSnapshot.getDownloadUrl()
            }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(
                        context,
                        "You need the camera permission to use this app.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    openCamera()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CAMERA_REQUEST_CODE) {
            try {
                photo = data!!.extras!!.get("data") as Bitmap
                binding.imgViewPhotoResult.setImageBitmap(photo)
//                saveImageToStorage()
            } catch (e: NullPointerException) {
                Log.e("Camera", "Photo not retrieved. $e")
            }
        }
    }
}