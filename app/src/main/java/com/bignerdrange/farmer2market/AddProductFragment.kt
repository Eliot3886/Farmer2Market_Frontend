package com.bignerdrange.farmer2market

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.snackbar.Snackbar
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class AddProductFragment : Fragment(R.layout.fragment_add_product) {

    private var selectedImageUri: Uri? = null
    private lateinit var ivPreview: ImageView
    private lateinit var llPlaceholder: View
    private lateinit var btnPost: MaterialButton

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            if (data?.data != null) {
                // From Gallery
                selectedImageUri = data.data
            } else if (data?.extras?.get("data") != null) {
                // From Camera (thumbnail)
                val bitmap = data.extras!!["data"] as android.graphics.Bitmap
                selectedImageUri = getImageUriFromBitmap(bitmap)
            }
            ivPreview.setImageURI(selectedImageUri)
            ivPreview.visibility = View.VISIBLE
            llPlaceholder.visibility = View.GONE
        }
    }

    private fun getImageUriFromBitmap(bitmap: android.graphics.Bitmap): Uri {
        val file = File(requireContext().cacheDir, "camera_image_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.close()
        return Uri.fromFile(file)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val etName = view.findViewById<TextInputEditText>(R.id.etProductName)
        val etPrice = view.findViewById<TextInputEditText>(R.id.etPrice)
        val etDescription = view.findViewById<TextInputEditText>(R.id.etDescription)
        val spCategory = view.findViewById<AutoCompleteTextView>(R.id.spCategory)
        val tvAutoLocation = view.findViewById<TextView>(R.id.tvAutoLocation)
        btnPost = view.findViewById(R.id.btnPostProduct)
        val cardPhoto = view.findViewById<View>(R.id.cardAddPhoto)
        ivPreview = view.findViewById(R.id.ivProductPreview)
        llPlaceholder = view.findViewById(R.id.llPhotoPlaceholder)
        val topAppBar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.topAppBar)

        topAppBar.setNavigationOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, HomeFragment())
                .commit()
        }

        val categories = arrayOf("Vegetables", "Fruits", "Grains", "Livestock", "Dairy", "Fodder", "Others")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        spCategory.setAdapter(adapter)

        var detectedLocation = "Harare, Zimbabwe"
        LocationHelper.detectLocation(requireContext()) { loc ->
            detectedLocation = loc
            tvAutoLocation.text = "📍 Location: $loc"
        }

        cardPhoto.setOnClickListener {
            val options = arrayOf("Take Photo", "Choose from Gallery")
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Add Product Photo")
                .setItems(options) { _, which ->
                    if (which == 0) {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        pickImage.launch(intent)
                    } else {
                        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        pickImage.launch(intent)
                    }
                }.show()
        }

        btnPost.setOnClickListener {
            val name = etName.text.toString().trim()
            val category = spCategory.text.toString().trim()
            val price = etPrice.text.toString().trim()

            if (name.isEmpty() || category.isEmpty() || price.isEmpty()) {
                Snackbar.make(view, "Please fill all required fields", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }

            btnPost.isEnabled = false
            val description = etDescription.text.toString().trim()
            uploadProduct(view, name, category, price, detectedLocation, description)
        }
    }

    private fun uploadProduct(view: View, name: String, category: String, price: String, location: String, description: String) {
        val mediaType = "text/plain".toMediaType()
        val nameBody = name.toRequestBody(mediaType)
        val categoryBody = category.toRequestBody(mediaType)
        val priceBody = price.toRequestBody(mediaType)
        val quantityBody = "1 Unit".toRequestBody(mediaType) 
        val locationBody = location.toRequestBody(mediaType)
        val descriptionBody = description.toRequestBody(mediaType)

        var imagePart: MultipartBody.Part? = null
        selectedImageUri?.let { uri ->
            try {
                val file = getFileFromUri(uri)
                val imageMediaType = "image/*".toMediaType()
                val requestFile = file.asRequestBody(imageMediaType)
                imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)
            } catch (e: Exception) {
                Snackbar.make(view, "Image error: ${e.message}", Snackbar.LENGTH_LONG).show()
            }
        }

        RetrofitClient.instance.addProduct(nameBody, categoryBody, priceBody, quantityBody, locationBody, null, descriptionBody, imagePart)
            .enqueue(object : Callback<Product> {
                override fun onResponse(call: Call<Product>, response: Response<Product>) {
                    if (response.isSuccessful) {
                        Snackbar.make(view, "Product Added Successfully!", Snackbar.LENGTH_LONG).show()
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.frameLayout, HomeFragment())
                            .commit()
                    } else {
                        btnPost.isEnabled = true
                        Snackbar.make(view, "Failed: ${response.code()}", Snackbar.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<Product>, t: Throwable) {
                    btnPost.isEnabled = true
                    Snackbar.make(view, "Connection Error", Snackbar.LENGTH_LONG).show()
                }
            })
    }

    private fun getFileFromUri(uri: Uri): File {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
        val file = File(requireContext().cacheDir, "upload_image_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
        outputStream.close()
        return file
    }
}
