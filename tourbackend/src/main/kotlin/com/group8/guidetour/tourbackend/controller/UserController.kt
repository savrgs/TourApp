package com.group8.guidetour.tourbackend.controller

import com.group8.guidetour.tourbackend.entity.User
import com.group8.guidetour.tourbackend.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    data class RegisterRequest(val username: String, val email: String, val password: String)
    data class LoginRequest(val usernameOrEmail: String, val password: String)

    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<User?> {
        val user = userService.registerUser(request.username, request.email, request.password)
        return if (user != null) ResponseEntity.ok(user) else ResponseEntity.badRequest().build()
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<User?> {
        val user = userService.authenticateUser(request.usernameOrEmail, request.password)
        return if (user != null) ResponseEntity.ok(user) else ResponseEntity.status(401).build()
    }
}
