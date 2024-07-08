package com.example.myapplication

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import com.squareup.picasso.Picasso


/**
 * Restaurant view Adapter Class used to manage the Restaurant view recycler view
 *
 * @property dataSet
 */
class RestaurantViewAdapter(private val dataSet: ArrayList<ReviewModel>) :
    RecyclerView.Adapter<RestaurantViewAdapter.ViewHolder>() {

    private var storage = Firebase.storage

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val reviewImage: ShapeableImageView = itemView.findViewById(R.id.review_image)
        val locationTextView: MaterialTextView = itemView.findViewById(R.id.location_textview)
        val titleTextView: MaterialTextView = itemView.findViewById(R.id.title_textview)
        val ratingTextView: MaterialTextView = itemView.findViewById(R.id.rating_textview)
        val descriptionTextView: MaterialTextView = itemView.findViewById(R.id.description_textview)


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
     * Binds recycler view data to dataset
     *
     * @param holder
     * @param position
     */
    override fun onBindViewHolder(holder: RestaurantViewAdapter.ViewHolder, position: Int) {

        val currentItem = dataSet[position]


        val imageSrc = currentItem.imageUrl
        if (imageSrc.isNotBlank()) {
            loadImageIntoView(holder.reviewImage, imageSrc)
        }

        holder.locationTextView.text = currentItem.location
        holder.titleTextView.text = currentItem.title
        holder.ratingTextView.text =
            holder.itemView.context.getString(R.string.rating, currentItem.rating) + "/5"
        holder.descriptionTextView.text = currentItem.description


    }


    /**
     * Loads image into a view using a uri
     *
     * @param view
     * @param src
     */
    private fun loadImageIntoView(view: ImageView, src: String) {
        storage.reference.child(src).downloadUrl.addOnSuccessListener { uri ->
            Log.d("uriImage", uri.toString())
            Picasso.get()
                .load(uri.toString())
                .resize(179, 179)
                .centerCrop()
                .into(view)

        }
    }


}