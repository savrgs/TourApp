package com.group8.guidetour.tourbackend.entity

import jakarta.persistence.*

@Entity
@Table(name = "place_ratings")
data class PlaceRating(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id")
    val place: Place,
    val rating: Double
)
