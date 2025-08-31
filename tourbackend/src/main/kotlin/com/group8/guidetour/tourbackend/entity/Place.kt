package com.group8.guidetour.tourbackend.entity

import jakarta.persistence.*

@Entity
@Table(name = "places")
data class Place(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val name: String,
    val type: String,
    val latitude: Double,
    val longitude: Double,
    val isFree: Boolean,
    var description: String = "",
    var photoUrl: String? = null,
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "city_id")
    val city: City,
    var rating: Double = 0.0
) 