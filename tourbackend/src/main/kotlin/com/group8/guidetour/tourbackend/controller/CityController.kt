package com.group8.guidetour.tourbackend.controller

import com.group8.guidetour.tourbackend.dto.CityDTO
import com.group8.guidetour.tourbackend.dto.PlaceDTO
import com.group8.guidetour.tourbackend.repository.CityRepository
import com.group8.guidetour.tourbackend.repository.PlaceRepository
import com.group8.guidetour.tourbackend.repository.PlaceRatingRepository
import org.springframework.web.bind.annotation.*
import org.springframework.transaction.annotation.Transactional

@RestController
@RequestMapping("/api")
class CityController(
    private val cityRepository: CityRepository,
    private val placeRepository: PlaceRepository,
    private val placeRatingRepository: PlaceRatingRepository
) {
    @GetMapping("/cities")
    fun getAllCities(): List<CityDTO> =
        cityRepository.findAll().map { city ->
            CityDTO(
                id = city.id,
                name = city.name,
                country = city.country
            )
        }

    @GetMapping("/cities/{id}/places")
    fun getPlacesByCityId(@PathVariable id: Long): List<PlaceDTO> =
        placeRepository.findByCity_Id(id).map { place ->
            PlaceDTO(
                id = place.id,
                name = place.name,
                type = place.type,
                latitude = place.latitude,
                longitude = place.longitude,
                isFree = place.isFree,
                cityName = place.city?.name ?: "Unknown",
                description = place.description,
                photoUrl = place.photoUrl,
                rating = place.rating
            )
        }
    @PostMapping("/places/{id}/rate")
    @Transactional
    fun ratePlace(@PathVariable id: Long, @RequestParam rating: Double): PlaceDTO {
        val place = placeRepository.findById(id).orElseThrow { RuntimeException("Place not found") }
        // Save individual rating
        val placeRating = com.group8.guidetour.tourbackend.entity.PlaceRating(place = place, rating = rating)
        placeRatingRepository.save(placeRating)

        // Calculate new average rating
        val ratings = placeRatingRepository.findByPlace_Id(id)
        val avgRating = if (ratings.isNotEmpty()) ratings.map { it.rating }.average() else rating
        place.rating = avgRating
        placeRepository.save(place)

        return PlaceDTO(
            id = place.id,
            name = place.name,
            type = place.type,
            latitude = place.latitude,
            longitude = place.longitude,
            isFree = place.isFree,
            cityName = place.city?.name ?: "Unknown",
            description = place.description,
            photoUrl = place.photoUrl,
            rating = place.rating
        )
    }
}