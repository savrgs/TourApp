package com.group8.guidetour.tourbackend.repository

import com.group8.guidetour.tourbackend.entity.Place
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PlaceRepository : JpaRepository<Place, Long> {
    fun findByCity_Id(cityId: Long): List<Place>
}
