package com.group8.guidetour.tourbackend.controller

import com.group8.guidetour.tourbackend.dto.CityDTO
import com.group8.guidetour.tourbackend.dto.PlaceDTO
import com.group8.guidetour.tourbackend.repository.CityRepository
import com.group8.guidetour.tourbackend.repository.PlaceRepository
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class CityController(
    private val cityRepository: CityRepository,
    private val placeRepository: PlaceRepository
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
        placeRepository.findByCityId(id).map { place ->
            PlaceDTO(
                id = place.id,
                name = place.name,
                type = place.type,
                latitude = place.latitude,
                longitude = place.longitude,
                isFree = place.isFree
            )
        }
}