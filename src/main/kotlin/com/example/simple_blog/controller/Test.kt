package com.example.simple_blog.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class Test {
    @GetMapping("/health")
    fun health(): String = "OK"
}