package com.group8.guidetour.tourbackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.boot.autoconfigure.domain.EntityScan

@SpringBootApplication
@EnableJpaRepositories("com.group8.guidetour.tourbackend.repository")
@EntityScan("com.group8.guidetour.tourbackend.entity")
class TourbackendApplication

fun main(args: Array<String>) {
	runApplication<TourbackendApplication>(*args)
}