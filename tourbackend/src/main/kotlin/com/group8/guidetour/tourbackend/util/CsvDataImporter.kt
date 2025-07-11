package com.group8.guidetour.tourbackend.util

import com.group8.guidetour.tourbackend.entity.City
import com.group8.guidetour.tourbackend.entity.Place
import com.group8.guidetour.tourbackend.repository.CityRepository
import com.group8.guidetour.tourbackend.repository.PlaceRepository
import com.opencsv.CSVReader
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.InputStreamReader

@Component
class CsvDataImporter(
    private val cityRepository: CityRepository,
    private val placeRepository: PlaceRepository
) : CommandLineRunner {
    private val logger = LoggerFactory.getLogger(CsvDataImporter::class.java)

    override fun run(vararg args: String?) {
        if (cityRepository.count() > 0) {
            logger.info("Cities already imported, skipping import.")
            return
        }
        val cityMap = mutableMapOf<String, City>()
        // Read and save cities
        try {
            val cityResource = ClassPathResource("data/cities.csv")
            BufferedReader(InputStreamReader(cityResource.inputStream)).use { reader ->
                reader.readLine() // skip header
                reader.lineSequence().forEach { line ->
                    val parts = line.split(",")
                    if (parts.size >= 4) {
                        val city = City(
                            name = parts[0].trim(),
                            country = parts[1].trim(),
                            latitude = parts[2].toDoubleOrNull() ?: 0.0,
                            longitude = parts[3].toDoubleOrNull() ?: 0.0
                        )
                        val saved = cityRepository.save(city)
                        cityMap[saved.name] = saved
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Error reading cities.csv", e)
        }

        // Read and save places
        try {
            val placeResource = ClassPathResource("data/places.csv")
            CSVReader(InputStreamReader(placeResource.inputStream)).use { reader ->
                reader.readNext() // skip header
                val places = reader.readAll().mapNotNull { parts ->
                    if (parts.size >= 7) {
                        val cityName = parts[5].trim().removeSurrounding("\"")
                        val city = cityMap[cityName]
                        if (city == null) {
                            logger.warn("City not found for place: {}", parts.joinToString(","))
                            null
                        } else {
                            Place(
                                name = parts[0].trim().removeSurrounding("\""),
                                type = parts[1].trim().removeSurrounding("\""),
                                latitude = parts[2].toDoubleOrNull() ?: 0.0,
                                longitude = parts[3].toDoubleOrNull() ?: 0.0,
                                isFree = parts[4].trim().toBooleanStrictOrNull() ?: false,
                                description = parts[6].trim().removeSurrounding("\""),
                                city = city
                            )
                        }
                    } else null
                }
                placeRepository.saveAll(places)
            }
        } catch (e: Exception) {
            logger.error("Error reading places.csv", e)
        }
    }
} 