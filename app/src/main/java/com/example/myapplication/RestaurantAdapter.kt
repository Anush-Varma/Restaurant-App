package com.example.myapplication


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.Firebase

import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import com.squareup.picasso.Picasso

class RestaurantAdapter(private val dataSet: ArrayList<RestaurantsModel>) :
    RecyclerView.Adapter<RestaurantAdapter.ViewHolder>() {


    private val database = Firebase.firestore


    private var storage = Firebase.storage


    /**
     * Holds current data to populate in recyclerView
     *
     * @constructor
     * View
     *
     * @param view
     */
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val restaurantImage: ShapeableImageView = itemView.findViewById(R.id.restaurant_image)
        val restaurantName: MaterialTextView = itemView.findViewById(R.id.restaurant_name)
        val restaurantRating: RatingBar = itemView.findViewById(R.id.restaurant_rating_bar)
        val restaurantLocation: MaterialTextView =
            itemView.findViewById(R.id.restaurant_location_textview)
        lateinit var restaurantId: String

        init {
            itemView.setOnClickListener(this)
        }

        /**
         * Manages Clicks made to the each recycler view row
         *
         * @param v
         */
        override fun onClick(v: View?) {
            val activity = v!!.context as AppCompatActivity
            val fragment = RestaurantView()

            database.collection("Restaurants").document(restaurantId).get().addOnSuccessListener {
                fragment.setCurrentRestaurant(restaurantId)
                activity.supportFragmentManager.beginTransaction()
                    .replace(R.id.main_frame_layout, fragment).addToBackStack(null).commit()
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
        val view = inflater.inflate(R.layout.restaruants_row, parent, false)

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

        database.collection("Restaurants").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val imageSrc = "/RestaurantImages/" + currentItem.imageUrl
                loadImageIntoView(holder.restaurantImage, imageSrc)

            }
            holder.restaurantId = currentItem.restaurantId
            holder.restaurantLocation.text = currentItem.location
            holder.restaurantName.text = currentItem.name
            holder.restaurantRating.rating = currentItem.rating.toFloat()

        }

    }

    /**
     * Laods an image as a ImageView from a uri
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