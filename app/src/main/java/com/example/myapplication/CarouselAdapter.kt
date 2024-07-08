package com.example.myapplication


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import com.squareup.picasso.Picasso

/**
 * Carousel Adapter Class used to manage the carousel recycler view
 *
 * @property dataSet
 */
class CarouselAdapter(private val dataSet: ArrayList<CarouselModel>) :
    RecyclerView.Adapter<CarouselAdapter.ViewHolder>() {

    private var storage = Firebase.storage

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var carouselImage: ShapeableImageView = itemView.findViewById(R.id.carousel_image_view)

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
        val view = inflater.inflate(R.layout.carousel_item, parent, false)

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
        val imageSrc = currentItem.image

        if (imageSrc.isNotBlank()) {
            loadImageIntoView(holder.carouselImage, imageSrc)
        }
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