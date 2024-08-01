package com.example.simple_blog.domain.member.service

import com.example.simple_blog.domain.member.dto.MemberDto
import com.example.simple_blog.domain.member.dto.MemberRequest
import com.example.simple_blog.domain.member.repository.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated

@Service
@Transactional
class MemberService(
    private val memberRepository: MemberRepository
) {
    @Transactional(readOnly = true)
    fun findAll(): List<MemberDto> = memberRepository.findAll().map { MemberDto(it) }.toList()

    @Transactional
    fun create(@Validated memberRequest: MemberRequest) {
        memberRepository.save(memberRequest.toEntity())
    }
}