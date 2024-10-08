package kr.co.jumso.domain.member.annotation

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER


@Target(VALUE_PARAMETER)
@Retention(RUNTIME)
annotation class TemporaryMemberId
