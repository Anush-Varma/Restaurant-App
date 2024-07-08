package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.Firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.storage
import com.squareup.picasso.Picasso

/**
 * Collection Selector Adapter Class used to manage the Collection Selector recycler view
 *
 * @property dataSet
 */
class CollectionsSelectorAdapter(private val dataSet: ArrayList<CollectionsSelectorModel>) :
    RecyclerView.Adapter<CollectionsSelectorAdapter.ViewHolder>() {


    private var storage = Firebase.storage
    private val database = FirebaseFirestore.getInstance()


    /**
     * Holds current data to populate in recyclerView
     *
     * @constructor
     * View
     *
     * @param view
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkbox: MaterialCheckBox = itemView.findViewById(R.id.collections_checkBox)
        val reviewImage: ShapeableImageView = itemView.findViewById(R.id.collections_review_image)
        val locationTextView: MaterialTextView =
            itemView.findViewById(R.id.collections_location_textview)
        val titleTextView: MaterialTextView = itemView.findViewById(R.id.collections_title_textview)
        val ratingTextView: MaterialTextView =
            itemView.findViewById(R.id.collections_rating_textview)
        val descriptionTextView: MaterialTextView =
            itemView.findViewById(R.id.collections_description_textview)
        lateinit var reviewId: String
        lateinit var restaurantId: String


        init {
            checkbox.setOnClickListener {
                database.collection("Restaurants").document(restaurantId).collection("Reviews")
                    .document(reviewId).update(
                    mapOf("isChecked" to checkbox.isChecked)
                )

            }
        }
    }

    /**
     * Inflates current view
     *
     * @param parent
     * @param viewType
     * @return current view
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.collections_selector_row, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    /**
     * Binds recycler view data to dataset
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

        holder.checkbox.isChecked = currentItem.boolean

    }


    /**
     * Loads image into a view using a uri
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