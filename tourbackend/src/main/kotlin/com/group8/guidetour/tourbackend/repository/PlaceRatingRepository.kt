package com.group8.guidetour.tourbackend.repository

import com.group8.guidetour.tourbackend.entity.PlaceRating
import org.springframework.data.jpa.repository.JpaRepository

interface PlaceRatingRepository : JpaRepository<PlaceRating, Long> {
    fun findByPlace_Id(placeId: Long): List<PlaceRating>
}
