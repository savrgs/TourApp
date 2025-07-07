package com.group8.guidetour.tourbackend.entity

import jakarta.persistence.*

@Entity
@Table(name = "cities")
data class City(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double
) 