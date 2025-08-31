package com.group8.guidetour.tourbackend.service

import com.group8.guidetour.tourbackend.entity.User
import com.group8.guidetour.tourbackend.repository.UserRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import org.slf4j.LoggerFactory

@Service
class UserService(private val userRepository: UserRepository) {
    private val passwordEncoder = BCryptPasswordEncoder()
    private val logger = LoggerFactory.getLogger(UserService::class.java)

    fun registerUser(username: String, email: String, password: String): User? {
        logger.info("Attempting to register user: username=$username, email=$email")
        if (userRepository.findByUsername(username) != null) {
            logger.warn("Username already exists: $username")
            return null
        }
        if (userRepository.findByEmail(email) != null) {
            logger.warn("Email already exists: $email")
            return null
        }
        val passwordHash = passwordEncoder.encode(password)
        val user = User(
            username = username,
            email = email,
            passwordHash = passwordHash,
            createdAt = LocalDateTime.now()
        )
        val savedUser = userRepository.save(user)
        logger.info("User registered successfully: username=$username, email=$email, id=${savedUser.id}")
        return savedUser
    }

    fun authenticateUser(usernameOrEmail: String, password: String): User? {
        val user = userRepository.findByUsername(usernameOrEmail) ?: userRepository.findByEmail(usernameOrEmail)
        return if (user != null && passwordEncoder.matches(password, user.passwordHash)) user else null
    }
}
