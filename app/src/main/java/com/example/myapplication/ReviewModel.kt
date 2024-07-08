package com.example.myapplication


data class ReviewModel(
    var reviewId: String,
    var restaurantId: String,
    var title: String,
    var rating: String,
    var description: String,
    var imageUrl: String,
    var location: String,
)

