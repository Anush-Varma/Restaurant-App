package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.carousel.CarouselLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Collection Fragment layout
 *
 */
class CollectionsFragment : Fragment() {
    private var mAuth = FirebaseAuth.getInstance()
    private var currentUser = mAuth.currentUser
    private val database = FirebaseFirestore.getInstance()
    private val listOfCarouselImages = ArrayList<CarouselModel>()

    private val listOfCollectionReviews = ArrayList<CollectionsSelectorModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    /**
     * Infaltes Collections current fragment
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return View
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_collections, container, false)
    }

    /**
     * Populates fragment with respective widgets
     *
     * @param view
     * @param savedInstanceState
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val collectionsSelectorRecyclerView =
            view.findViewById<RecyclerView>(R.id.collection_selector_recycler_view)
        val collectionsSelectorLayoutManager = GridLayoutManager(view.context, 1)
        collectionsSelectorRecyclerView.layoutManager = collectionsSelectorLayoutManager



        collectionsSelectorRecyclerView.addItemDecoration(
            DividerItemDecoration(
                view.context,
                collectionsSelectorLayoutManager.orientation
            )
        )

        val carouselRecyclerView = view.findViewById<RecyclerView>(R.id.carousel_recycler_view)
        val carouselLayoutManger = CarouselLayoutManager()
        carouselRecyclerView.layoutManager = carouselLayoutManger


        //setup carousel and user selector recycler views
        database.collectionGroup("Reviews").whereEqualTo("userID", currentUser?.uid.toString())
            .get()
            .addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    val documents = task.result

                    for (document in documents) {
                        val title = document.get("Title").toString()
                        val rating = document.get("Rating").toString()
                        val description = document.get("Description").toString()
                        val imageUrl = document.get("Image").toString()
                        val location = document.get("Location").toString()
                        val reviewId = document.id
                        val restaurantId = document.get("restaurantId").toString()
                        val isChecked = document.get("isChecked")
                        val collectionReview = CollectionsSelectorModel(
                            reviewId,
                            restaurantId,
                            title,
                            rating,
                            description,
                            imageUrl,
                            location,
                            isChecked.toString().toBoolean()
                        )
                        listOfCollectionReviews.add(collectionReview)
                    }


                    for (item in listOfCollectionReviews) {
                        if (item.boolean) {
                            val itemToAdd = CarouselModel(item.imageUrl)
                            listOfCarouselImages.add(itemToAdd)
                        }
                    }


                    val adapter = CollectionsSelectorAdapter(listOfCollectionReviews)
                    collectionsSelectorRecyclerView.adapter = adapter


                    val carouselAdapter = CarouselAdapter(listOfCarouselImages)
                    carouselRecyclerView.adapter = carouselAdapter


                }
            }
    }


}