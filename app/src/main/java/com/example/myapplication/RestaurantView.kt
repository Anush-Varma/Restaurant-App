package com.example.myapplication

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.squareup.picasso.Picasso
import kotlin.random.Random


/**
 * Restaurant View Fragment
 *
 */
class RestaurantView : Fragment() {
    private lateinit var createNewReview: FloatingActionButton
    private lateinit var currentRestaurant: String
    private val database = Firebase.firestore
    private var storage = Firebase.storage

    private var mAuth = FirebaseAuth.getInstance()
    private var currentUser = mAuth.currentUser
    private val firestore = FirebaseStorage.getInstance()
    private var storageRef = firestore.reference
    private val fileName = generateRandomId()


    /**
     * enables user selection of images
     */
    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {

                storageRef = storageRef.child(fileName)
                storageRef.putFile(uri)

            } else {

                storageRef = storageRef.child(fileName)
                storageRef.putFile(Uri.EMPTY)
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_restaurant_view, container, false)
    }


    /**
     * populates the restaurant view to display reviews relating to current restaurant
     *
     * @param view
     * @param savedInstanceState
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val restaurantImage = view.findViewById<ShapeableImageView>(R.id.restaurant_view_image)
        val restaurantName = view.findViewById<MaterialTextView>(R.id.restaurant_view_name)
        val ratingBar = view.findViewById<RatingBar>(R.id.restaurant_view_rating)
        val restaurantLocation = view.findViewById<MaterialTextView>(R.id.restaurant_view_location)

        val restaurantViewRecyclerView =
            view.findViewById<RecyclerView>(R.id.restaurant_view_recycler_view)
        val layoutManager = GridLayoutManager(view.context, 1)
        restaurantViewRecyclerView.layoutManager = layoutManager

        restaurantViewRecyclerView.addItemDecoration(
            DividerItemDecoration(
                view.context,
                layoutManager.orientation
            )
        )


        database.collection("Restaurants").document(currentRestaurant).get()
            .addOnSuccessListener { document ->
                var src = "/RestaurantImages/" + document.get("Image").toString()
                loadImageIntoView(restaurantImage, src)
                restaurantName.text = document.get("Name").toString()
                restaurantLocation.text = document.get("Location").toString()
                ratingBar.rating = document.get("Rating").toString().toFloat()
            }


        database.collection("Restaurants").document(currentRestaurant).collection("Reviews").get()
            .addOnSuccessListener { query ->
                val listOfReviews = ArrayList<ReviewModel>()
                for (document in query.documents) {
                    val title = document.get("Title").toString()
                    val rating = document.get("Rating").toString()
                    val description = document.get("Description").toString()
                    val imageUrl = document.get("Image").toString()
                    val location = document.get("Location").toString()
                    val reviewId = document.id
                    val review = ReviewModel(
                        reviewId,
                        currentRestaurant,
                        title,
                        rating,
                        description,
                        imageUrl,
                        location
                    )
                    listOfReviews.add(review)
                }
                val adapter = RestaurantViewAdapter(listOfReviews)

                restaurantViewRecyclerView.adapter = adapter
            }

        createNewReview = view.findViewById(R.id.new_review_button)
        //disables the ability to create reviews if user is a guest
        if (currentUser?.isAnonymous == true) {
            createNewReview.isEnabled = false
        }
        createNewReview.setOnClickListener {

            createReview()
        }

    }

    /**
     * Creates a new review relating to current restaurant
     *
     */
    private fun createReview() {

        val inflater = LayoutInflater.from(this.context)
        val dialogView = inflater.inflate(R.layout.dialog_new_review, null)


        val reviewTitle = dialogView.findViewById<TextInputEditText>(R.id.create_review_title_input)
        val reviewLocation =
            dialogView.findViewById<TextInputEditText>(R.id.create_review_location_input)
        val reviewRating =
            dialogView.findViewById<TextInputEditText>(R.id.create_review_rating_input)
        val reviewDescription =
            dialogView.findViewById<TextInputEditText>(R.id.create_review_description_input)


        val dialog = this.context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle("Create a new review")
                .setView(dialogView)
                .setPositiveButton("Submit") { _, _ ->
                    val newReview = hashMapOf(
                        "Description" to reviewDescription.text.toString(),
                        "Image" to fileName,
                        "Location" to reviewLocation.text.toString(),
                        "Rating" to reviewRating.text.toString(),
                        "Title" to reviewTitle.text.toString(),
                        "userID" to currentUser?.uid.toString(),
                        "restaurantId" to currentRestaurant
                    )

                    database.collection("Restaurants").document(currentRestaurant)
                        .collection("Reviews").add(newReview)

                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()

                }.create()
        }

        dialog?.show()

        dialog?.findViewById<MaterialButton>(R.id.add_review_image_button)?.setOnClickListener {

            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))

        }

    }


    /**
     * generate a random id to save image if selected
     *
     * @return
     */
    private fun generateRandomId(): String {
        val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..20)
            .map { Random.nextInt(0, charPool.size) }
            .map(charPool::get)
            .joinToString("")
    }


    fun setCurrentRestaurant(restaurantName: String) {
        currentRestaurant = restaurantName

    }


    /**
     * Laods an image as a ImageView from a uri
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