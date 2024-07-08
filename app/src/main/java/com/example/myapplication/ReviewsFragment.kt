package com.example.myapplication

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlin.random.Random

/**
 * review Fragment used to display current fragment
 *
 */
class ReviewsFragment : Fragment() {

    private var mAuth = FirebaseAuth.getInstance()
    private var currentUser = mAuth.currentUser
    private val database = FirebaseFirestore.getInstance()
    private val firestore = FirebaseStorage.getInstance()
    private var storageRef = firestore.reference

    private lateinit var adapter: ReviewAdapter
    private var fileName = generateRandomId()

    /**
     * enables the user to select an image
     */
    private val pickMedia =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {

                storageRef = firestore.reference.child(fileName)
                storageRef.putFile(uri)
            } else {

                storageRef = firestore.reference.child(fileName)
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


        return inflater.inflate(R.layout.fragment_reviews, container, false)
    }

    /**
     * populates reviews in reviews recycler view
     *
     * @param view
     * @param savedInstanceState
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val reviewsRecyclerview = view.findViewById<RecyclerView>(R.id.reviews_recycler_view)
        val layoutManger = GridLayoutManager(view.context, 1)
        reviewsRecyclerview.layoutManager = layoutManger

        database.collectionGroup("Reviews").whereEqualTo("userID", currentUser?.uid.toString())
            .get()
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    val documents = task.result

                    val listOfReviews = ArrayList<ReviewModel>()

                    for (document in documents) {
                        val title = document.get("Title").toString()
                        val rating = document.get("Rating").toString()
                        val description = document.get("Description").toString()
                        val imageUrl = document.get("Image").toString()
                        val location = document.get("Location").toString()
                        val reviewId = document.id
                        val restaurantId = document.get("restaurantId").toString()
                        val review = ReviewModel(
                            reviewId,
                            restaurantId,
                            title,
                            rating,
                            description,
                            imageUrl,
                            location
                        )
                        listOfReviews.add(review)
                    }

                    adapter = ReviewAdapter(listOfReviews, pickMedia, fileName)

                    reviewsRecyclerview.adapter = adapter
                }
            }

    }


    /**
     * Generates a random ID for user selected Image
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


}