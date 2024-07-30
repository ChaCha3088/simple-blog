package com.example.simple_blog.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType.IDENTITY
import jakarta.persistence.Id
import jakarta.validation.constraints.NotBlank

@Entity
class Post (
    @Id @GeneratedValue(strategy = IDENTITY)
    var id: Long,

    @Column(nullable = false)
    @NotBlank
    var title: String,

    @Column(nullable = false)
    @NotBlank
    var content: String
)