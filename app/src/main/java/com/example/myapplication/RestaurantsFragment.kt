package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore


/**
 * Restaurant Fragment
 *
 */
class RestaurantsFragment : Fragment() {


    private val database = FirebaseFirestore.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_restaurants, container, false)
    }


    /**
     * Populates restaurant fragment with required data
     *
     * @param view
     * @param savedInstanceState
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val restaurantsRecyclerview =
            view.findViewById<RecyclerView>(R.id.restaurants_recycler_view)
        val layoutManger = GridLayoutManager(view.context, 1)
        restaurantsRecyclerview.layoutManager = layoutManger

        restaurantsRecyclerview.addItemDecoration(
            DividerItemDecoration(
                view.context,
                layoutManger.orientation
            )
        )


        database.collection("Restaurants").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val documents = task.result

                val listOfRestaurants = ArrayList<RestaurantsModel>()
                for (document in documents) {
                    val name = document.get("Name").toString()

                    val location = document.get("Location").toString()
                    val imageUrl = document.get("Image").toString()

                    database.collection("Restaurants").document(document.id).collection("Reviews")
                        .get().addOnSuccessListener { query ->
                        var reviewAverage = 0.0
                        var counter = 0
                        for (i in query.documents) {
                            if (i.get("Rating") == ""){
                                reviewAverage += 0
                                counter++
                            }
                            else {
                                reviewAverage += i.get("Rating").toString().toDouble()
                                counter++
                            }
                        }
                        reviewAverage = reviewAverage.div(counter)

                        database.collection("Restaurants").document(document.id)
                            .update("Rating", reviewAverage)

                        val restaurant =
                            RestaurantsModel(document.id, name, reviewAverage, location, imageUrl)
                        listOfRestaurants.add(restaurant)
                        if (listOfRestaurants.size == documents.size()) {
                            val adapter = RestaurantAdapter(listOfRestaurants)
                            restaurantsRecyclerview.adapter = adapter
                        }

                    }


                }

            }
        }


    }


}