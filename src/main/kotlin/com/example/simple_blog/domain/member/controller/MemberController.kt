package com.example.simple_blog.domain.member.controller

import com.example.simple_blog.domain.member.dto.MemberRequest
import com.example.simple_blog.domain.member.service.MemberService
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(value = ["/api/members"], consumes = [APPLICATION_JSON_VALUE])
class MemberController(
    private val memberService: MemberService
) {
    @GetMapping
    fun findAll() = memberService.findAll()

    @PostMapping
    fun create(@RequestBody memberRequest: MemberRequest) {
        memberService.create(memberRequest)
    }
}