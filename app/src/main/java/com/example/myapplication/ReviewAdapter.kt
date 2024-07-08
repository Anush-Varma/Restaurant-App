package com.example.myapplication

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.squareup.picasso.Picasso


/**
 * Manages Reviews for Reviews recycler view
 *
 * @property dataSet
 * @property pickMedia
 * @property imageUrl
 */
class ReviewAdapter(
    private val dataSet: ArrayList<ReviewModel>,
    private val pickMedia: ActivityResultLauncher<PickVisualMediaRequest>,
    private var imageUrl: String
) : RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {


    private var mAuth = FirebaseAuth.getInstance()
    private var currentUser = mAuth.currentUser
    private val database = Firebase.firestore
    private val firestore = FirebaseStorage.getInstance()
    private var storageRef = firestore.reference
    private var storage = Firebase.storage

    private lateinit var selectedImage: Uri


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        val reviewImage: ShapeableImageView = itemView.findViewById(R.id.review_image)
        val locationTextView: MaterialTextView = itemView.findViewById(R.id.location_textview)
        val titleTextView: MaterialTextView = itemView.findViewById(R.id.title_textview)
        val ratingTextView: MaterialTextView = itemView.findViewById(R.id.rating_textview)
        val descriptionTextView: MaterialTextView = itemView.findViewById(R.id.description_textview)
        lateinit var reviewId: String
        lateinit var restaurantId: String

        init {
            itemView.setOnClickListener(this)
        }

        /**
         * Enables the ability to edit reviews and populates current data
         *
         * @param v
         */
        override fun onClick(v: View?) {
            val inflater = LayoutInflater.from(v?.context)
            val dialogView = inflater.inflate(R.layout.dialog_new_review, null)
            val reviewTitle =
                dialogView.findViewById<TextInputEditText>(R.id.create_review_title_input)
            val reviewLocation =
                dialogView.findViewById<TextInputEditText>(R.id.create_review_location_input)
            val reviewRating =
                dialogView.findViewById<TextInputEditText>(R.id.create_review_rating_input)
            val reviewDescription =
                dialogView.findViewById<TextInputEditText>(R.id.create_review_description_input)

            val addImageButton =
                dialogView.findViewById<MaterialButton>(R.id.add_review_image_button)

            getEditReviewData(
                reviewId,
                reviewTitle,
                reviewLocation,
                reviewRating,
                reviewDescription
            )

            val dialog = v?.context?.let {
                MaterialAlertDialogBuilder(it)
                    .setTitle("Edit Review")
                    .setView(dialogView).setPositiveButton("Submit") { _, _ ->
                        database.collection("Restaurants").document(restaurantId)
                            .get().addOnSuccessListener { _ ->
                                val reviewDocumentRef =
                                    database.collection("Restaurants").document(restaurantId)
                                        .collection("Reviews").document(reviewId)

                                reviewDocumentRef.update(
                                    "Description", reviewDescription.text.toString(),
                                    "Location", reviewLocation.text.toString(),
                                    "Rating", reviewRating.text.toString(),
                                    "Title", reviewTitle.text.toString(),
                                    "userID", currentUser?.uid.toString(),
                                    "restaurantId", restaurantId,
                                    "Image", imageUrl
                                ).addOnCompleteListener { _ ->

                                    updateReviewImage(
                                        reviewDocumentRef,
                                        imageUrl
                                    )
                                    val successSnackbar = Snackbar.make(
                                        v,
                                        "Review updated successfully",
                                        Snackbar.LENGTH_LONG
                                    )
                                    successSnackbar.show()
                                }.addOnFailureListener { _ ->
                                }

                            }
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->

                        dialog.dismiss()
                    }

            }?.create()
            addImageButton.setOnClickListener {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
            dialog?.show()
        }


        /**
         * updates changes made to a review after edditing
         *
         * @param reviewDocumentRef
         * @param imageUrl
         */
        private fun updateReviewImage(reviewDocumentRef: DocumentReference, imageUrl: String) {

            if (::selectedImage.isInitialized) {


                reviewDocumentRef.update("Image", imageUrl).addOnSuccessListener {

                    storageRef.child(imageUrl).putFile(selectedImage)
                        .addOnSuccessListener {
                            val successSnackbar = Snackbar.make(
                                itemView,
                                "Review updated successfully",
                                Snackbar.LENGTH_LONG
                            )
                            successSnackbar.show()
                        }
                        .addOnFailureListener { exception ->

                            val failureSnackbar = Snackbar.make(
                                itemView,
                                "Failed to update image: ${exception.message}",
                                Snackbar.LENGTH_LONG
                            )
                            failureSnackbar.show()
                        }
                }
            } else {

                val successSnackbar = Snackbar.make(
                    itemView,
                    "Review details updated successfully",
                    Snackbar.LENGTH_LONG
                )
                successSnackbar.show()
            }
        }


        /**
         * fills pre existing data for review to be edited
         *
         * @param reviewId
         * @param reviewTitle
         * @param reviewLocation
         * @param reviewRating
         * @param reviewDescription
         */
        private fun getEditReviewData(
            reviewId: String,
            reviewTitle: TextInputEditText,
            reviewLocation: TextInputEditText,
            reviewRating: TextInputEditText,
            reviewDescription: TextInputEditText
        ) {

            database.collectionGroup("Reviews")
                .whereEqualTo(
                    "userID",
                    currentUser?.uid.toString()
                )
                .get()
                .addOnCompleteListener { task ->
                    val documents = task.result.documents

                    for (document in documents) {
                        if (document.id == reviewId) {
                            reviewTitle.setText(document.getString("Title"))
                            reviewLocation.setText(document.getString("Location"))
                            reviewRating.setText(document.getString("Rating"))
                            reviewDescription.setText(document.getString("Description"))
                        }
                    }
                }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.reviews_row, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    /**
     * manages current review data
     *
     * @param holder
     * @param position
     */

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = dataSet[position]


        val imageSrc = currentItem.imageUrl
        if (imageSrc.isNotBlank()) {
            loadImageIntoView(holder.reviewImage, imageSrc)
        }
        holder.reviewId = currentItem.reviewId
        holder.locationTextView.text = currentItem.location
        holder.titleTextView.text = currentItem.title
        holder.ratingTextView.text =
            holder.itemView.context.getString(R.string.rating, currentItem.rating) + "/5"
        holder.descriptionTextView.text = currentItem.description
        holder.restaurantId = currentItem.restaurantId
    }


    /**
     * loads and ImageView from a Uri
     *
     * @param view
     * @param src
     */
    private fun loadImageIntoView(view: ImageView, src: String) {
        storage.reference.child(src).downloadUrl.addOnSuccessListener { uri ->

            Picasso.get()
                .load(uri.toString())
                .resize(179, 179)
                .centerCrop()
                .into(view)

        }
    }


}