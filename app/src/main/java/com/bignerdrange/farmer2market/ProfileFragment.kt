package com.bignerdrange.farmer2market

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var llDisplay: LinearLayout
    private lateinit var llEdit: LinearLayout
    private lateinit var tvName: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvLocation: TextView
    private lateinit var tvFarmName: TextView
    private lateinit var tvRole: TextView
    private lateinit var etName: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var etLocation: TextInputEditText
    private lateinit var etFarmName: TextInputEditText
    private lateinit var etRole: TextInputEditText
    private var currentRole: String? = null
    private var selectedImageUri: android.net.Uri? = null

    private val pickImage = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val data = result.data
            if (data?.data != null) {
                selectedImageUri = data.data
            } else if (data?.extras?.get("data") != null) {
                val bitmap = data.extras!!["data"] as android.graphics.Bitmap
                selectedImageUri = getImageUriFromBitmap(bitmap)
            }
            view?.findViewById<android.widget.ImageView>(R.id.ivProfilePicture)?.setImageURI(selectedImageUri)
            uploadProfilePicture()
        }
    }

    private fun getImageUriFromBitmap(bitmap: android.graphics.Bitmap): android.net.Uri {
        val file = java.io.File(requireContext().cacheDir, "camera_profile_${System.currentTimeMillis()}.jpg")
        val outputStream = java.io.FileOutputStream(file)
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.close()
        return android.net.Uri.fromFile(file)
    }

    private fun getFileFromUri(uri: android.net.Uri): java.io.File {
        val inputStream = requireContext().contentResolver.openInputStream(uri)
        val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
        val file = java.io.File(requireContext().cacheDir, "upload_profile_${System.currentTimeMillis()}.jpg")
        val outputStream = java.io.FileOutputStream(file)
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
        outputStream.close()
        return file
    }
    
    private fun uploadProfilePicture() {
        selectedImageUri?.let { uri ->
            try {
                val file = getFileFromUri(uri)
                val requestFile = file.asRequestBody("image/*".toMediaType())
                val imagePart = MultipartBody.Part.createFormData("profile_picture", file.name, requestFile)
                RetrofitClient.instance.updateProfilePicture(imagePart).enqueue(object : Callback<UserTokenData> {
                    override fun onResponse(call: Call<UserTokenData>, response: Response<UserTokenData>) {
                        if (response.isSuccessful) {
                            Toast.makeText(requireContext(), "Profile picture updated", Toast.LENGTH_SHORT).show()
                            fetchProfile()
                        }
                    }
                    override fun onFailure(call: Call<UserTokenData>, t: Throwable) {
                        Toast.makeText(requireContext(), "Failed to upload picture", Toast.LENGTH_SHORT).show()
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        llDisplay = view.findViewById(R.id.llProfileDisplay)
        llEdit = view.findViewById(R.id.llProfileEdit)
        tvName = view.findViewById(R.id.tvProfileName)
        tvPhone = view.findViewById(R.id.tvProfilePhone)
        tvLocation = view.findViewById(R.id.tvProfileLocation)
        tvFarmName = view.findViewById(R.id.tvProfileFarmName)
        etName = view.findViewById(R.id.etEditName)
        etPhone = view.findViewById(R.id.etEditPhone)
        etLocation = view.findViewById(R.id.etEditLocation)
        etFarmName = view.findViewById(R.id.etEditFarmName)

        val btnEdit = view.findViewById<MaterialButton>(R.id.btnEditProfile)
        val btnSave = view.findViewById<MaterialButton>(R.id.btnSaveProfile)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancelEdit)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)
        val btnDeleteAccount = view.findViewById<Button>(R.id.btnDeleteAccount)
        val btnAdmin = view.findViewById<MaterialButton>(R.id.btnAdminDashboard)
        val btnMyProducts = view.findViewById<MaterialButton>(R.id.btnMyProducts)

        tvRole = view.findViewById(R.id.tvProfileRole)
        etRole = view.findViewById(R.id.etEditRole)
        val fabEditPicture = view.findViewById<View>(R.id.fabEditPicture)

        fabEditPicture.setOnClickListener {
            val options = arrayOf("Take Photo", "Choose from Gallery")
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Update Profile Picture")
                .setItems(options) { _, which ->
                    if (which == 0) {
                        val intent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                        pickImage.launch(intent)
                    } else {
                        val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        pickImage.launch(intent)
                    }
                }.show()
        }

        val prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        currentRole = prefs.getString("role", "Buyer")
        val username = prefs.getString("username", "")

        if (username == "!admin") {
            btnAdmin.visibility = View.VISIBLE
            btnAdmin.setOnClickListener {
                startActivity(Intent(requireContext(), AdminActivity::class.java))
            }
        }

        if (currentRole == "Farmer") {
            btnMyProducts.visibility = View.VISIBLE
            btnMyProducts.setOnClickListener {
                startActivity(Intent(requireContext(), MyProductsActivity::class.java))
            }
        }

        fetchProfile()

        btnEdit.setOnClickListener {
            toggleEditMode(true)
        }

        btnCancel.setOnClickListener {
            toggleEditMode(false)
        }

        btnSave.setOnClickListener {
            saveProfile()
        }

        btnLogout.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Logout") { _, _ -> logout() }
                .setNegativeButton("Cancel", null)
                .show()
        }

        btnDeleteAccount.setOnClickListener {
            deleteAccount()
        }
    }

    private fun toggleEditMode(isEdit: Boolean) {
        llDisplay.visibility = if (isEdit) View.GONE else View.VISIBLE
        llEdit.visibility = if (isEdit) View.VISIBLE else View.GONE
        view?.findViewById<View>(R.id.btnEditProfile)?.visibility = if (isEdit) View.GONE else View.VISIBLE
        
        if (isEdit) {
            etName.setText(tvName.text)
            etPhone.setText(tvPhone.text)
            etLocation.setText(tvLocation.text)
            etFarmName.setText(tvFarmName.text)
            etRole.setText(currentRole)
            
            // Re-check farm name visibility
                if (currentRole == "Farmer") View.VISIBLE else View.GONE
        }
    }

    private fun fetchProfile() {
        RetrofitClient.instance.getProfile().enqueue(object : Callback<UserTokenData> {
            override fun onResponse(call: Call<UserTokenData>, response: Response<UserTokenData>) {
                if (response.isSuccessful) {
                    val user = response.body() ?: return
                    tvName.text = user.full_name
                    tvPhone.text = user.phone_number
                    tvLocation.text = user.location ?: "Zimbabwe"
                    tvRole.text = user.role
                    currentRole = user.role
                    
                    // Update shared prefs
                    val editor = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE).edit()
                    editor.putString("role", user.role)
                    editor.putString("full_name", user.full_name)
                    if (user.role == "Farmer") {
                        editor.putString("farm_name", user.farm_name)
                    }
                    editor.apply()

                    if (currentRole == "Farmer") {
                        view?.findViewById<View>(R.id.llFarmNameContainer)?.visibility = View.VISIBLE
                        tvFarmName.text = user.farm_name ?: "N/A"
                    } else {
                        view?.findViewById<View>(R.id.llFarmNameContainer)?.visibility = View.GONE
                    }
                    
                    val avatarUrl = user.profile_picture
                    if (!avatarUrl.isNullOrEmpty()) {
                        val fullUrl = if (avatarUrl.startsWith("http")) avatarUrl else "${RetrofitClient.BASE_URL.dropLast(1)}$avatarUrl"
                        com.squareup.picasso.Picasso.get().load(fullUrl).placeholder(R.drawable.ic_person).into(view?.findViewById<android.widget.ImageView>(R.id.ivProfilePicture))
                    }
                }
            }
            override fun onFailure(call: Call<UserTokenData>, t: Throwable) {
                Toast.makeText(requireContext(), "Error fetching profile", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun saveProfile() {
        val data = mutableMapOf(
            "full_name" to etName.text.toString(),
            "phone_number" to etPhone.text.toString(),
            "location" to etLocation.text.toString(),
            "role" to etRole.text.toString()
        )
        if (currentRole == "Farmer") {
            data["farm_name"] = etFarmName.text.toString()
        }

        RetrofitClient.instance.updateProfile(data).enqueue(object : Callback<UserTokenData> {
            override fun onResponse(call: Call<UserTokenData>, response: Response<UserTokenData>) {
                if (response.isSuccessful) {
                    toggleEditMode(false)
                    fetchProfile()
                    Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Update failed: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<UserTokenData>, t: Throwable) {
                Toast.makeText(requireContext(), "Network error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun deleteAccount() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                RetrofitClient.instance.deleteProfile().enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.isSuccessful) logout()
                    }
                    override fun onFailure(call: Call<Void>, t: Throwable) {}
                })
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun logout() {
        val prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        RetrofitClient.setToken(null)
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}
