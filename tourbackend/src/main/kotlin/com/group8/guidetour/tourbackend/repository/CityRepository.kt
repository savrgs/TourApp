package com.group8.guidetour.tourbackend.repository

import com.group8.guidetour.tourbackend.entity.City
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CityRepository : JpaRepository<City, Long>
