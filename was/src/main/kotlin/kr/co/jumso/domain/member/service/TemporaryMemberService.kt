package kr.co.jumso.domain.member.service

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletResponse
import kr.co.jumso.domain.auth.exception.CompanyEmailNotFoundException
import kr.co.jumso.domain.auth.repository.CompanyEmailRepository
import kr.co.jumso.domain.auth.service.JwtService
import kr.co.jumso.domain.kafka.dto.KafkaEmailRequest
import kr.co.jumso.domain.kafka.enumstorage.EmailType.SIGN_UP
import kr.co.jumso.domain.kafka.enumstorage.KafkaEmail.KAFKA_EMAIL_SERVER
import kr.co.jumso.domain.member.dto.request.EnrollRequest
import kr.co.jumso.domain.member.dto.request.SignUpRequest
import kr.co.jumso.domain.member.dto.response.MemberResponse
import kr.co.jumso.domain.member.entity.Member
import kr.co.jumso.domain.member.entity.MemberNotTheseCompany
import kr.co.jumso.domain.member.entity.MemberProperty
import kr.co.jumso.domain.member.entity.TemporaryMember
import kr.co.jumso.domain.member.exception.InvalidMemberPropertyIdsException
import kr.co.jumso.domain.member.exception.InvalidVerificationCodeException
import kr.co.jumso.domain.member.exception.MemberExistsException
import kr.co.jumso.domain.member.exception.TemporaryMemberExistsException
import kr.co.jumso.domain.member.repository.MemberNotTheseCompanyRepository
import kr.co.jumso.domain.member.repository.MemberPropertyRepository
import kr.co.jumso.domain.member.repository.MemberRepository
import kr.co.jumso.domain.member.repository.TemporaryMemberRepository
import kr.co.jumso.util.PasswordValidator
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class TemporaryMemberService(
    private val jwtService: JwtService,

    private val temporaryMemberRepository: TemporaryMemberRepository,
    private val memberRepository: MemberRepository,
    private val companyEmailRepository: CompanyEmailRepository,
    private val memberPropertyRepository: MemberPropertyRepository,
    private val memberNotTheseCompanyRepository: MemberNotTheseCompanyRepository,

    private val kafkaTemplate: KafkaTemplate<String, String>,

    private val passwordValidator: PasswordValidator,
    private val objectMapper: ObjectMapper,
) {
    @Value("\${spring.kafka.email-server-port}")
    private lateinit var emailServerPort: String

    @Transactional
    fun create(
        signUpRequest: SignUpRequest,
        response: HttpServletResponse
    ) {
        signUpRequest.username = signUpRequest.username.trim()
        signUpRequest.password = signUpRequest.password.trim()
        signUpRequest.passwordConfirm = signUpRequest.passwordConfirm.trim()
        signUpRequest.nickname = signUpRequest.nickname.trim()

        // password 검증
        val validatedAndEncodedPassword = passwordValidator.validate(
            signUpRequest.username,
            signUpRequest.password,
            signUpRequest.passwordConfirm
        )

        // companyEmail 가져오기
        val companyEmail = companyEmailRepository.findById(signUpRequest.companyEmailId)
            .orElseThrow { throw CompanyEmailNotFoundException() }

        // 이미 존재하는 temporaryMember인지 확인
        if (temporaryMemberRepository.existsByEmail("${signUpRequest.username}@${companyEmail.address}")) {
            throw TemporaryMemberExistsException()
        }

        // 이미 존재하는 member인지 확인
        if (memberRepository.existsByEmail("${signUpRequest.username}@${companyEmail.address}")) {
            throw MemberExistsException()
        }

        val newTemporaryMember = temporaryMemberRepository.save(
            TemporaryMember(
                email = "${signUpRequest.username}@${companyEmail.address}",
                password = validatedAndEncodedPassword,
                nickname = signUpRequest.nickname,
                companyEmailId = companyEmail.id!!,
            )
        )

        // 새로운 토큰을 발급한다.
        val newAccessToken = jwtService.issueTemporaryMemberJwts(newTemporaryMember)

        // 토큰을 Header에 설정
        setAccessToken(response, newAccessToken)

        // Kafka에 이메일 전송 요청을 보내고, Kafka Consumer가 이메일을 보낸다.
        kafkaTemplate.send(
            "$KAFKA_EMAIL_SERVER-$emailServerPort",
            objectMapper.writeValueAsString(
                KafkaEmailRequest(
                    emailType = SIGN_UP,
                    memberUsername = signUpRequest.username,
                    domain = companyEmail.address,
                    verificationCode = newTemporaryMember.verificationCode
                )
            )
        )
    }

    @Transactional
    fun verify(
        temporaryMemberId: Long,
        verificationCode: String,
    ) {
        val verificationCode = verificationCode.trim()

        val temporaryMember: TemporaryMember = temporaryMemberRepository.findById(temporaryMemberId)
            .orElseThrow { throw InvalidVerificationCodeException() }

        // temporaryMember 인증
        temporaryMember.verify(verificationCode)

        temporaryMemberRepository.save(temporaryMember)
    }

    @Transactional
    fun enroll(
        temporaryMemberId: Long,
        enrollRequest: EnrollRequest,
        response: HttpServletResponse,
    ): MemberResponse {
        val temporaryMember: TemporaryMember = temporaryMemberRepository.findById(temporaryMemberId)
            .orElseThrow { throw InvalidVerificationCodeException() }

        // enrollRequest의 memberPropertyIds가 1000번대, 2000번대, 3000번대가 하나 이상 포함되어 있는지 확인
        enrollRequest.validateMemberPropertyIds()
            // false일 경우 throw
            .takeUnless { it }
            ?.let { throw InvalidMemberPropertyIdsException() }

        // member 생성
        val newMember = Member(
            email = temporaryMember.email,
            password = temporaryMember.password,
            nickname = temporaryMember.nickname,
            companyId = temporaryMember.companyEmailId,
            bornAt = enrollRequest.bornAt,
            sex = enrollRequest.sex,
            height = enrollRequest.height,
            bodyType = enrollRequest.bodyType,
            job = enrollRequest.job,
            relationshipStatus = enrollRequest.relationshipStatus,
            religion = enrollRequest.religion,
            smoke = enrollRequest.smoke,
            drink = enrollRequest.drink,
            latitude = enrollRequest.latitude,
            longitude = enrollRequest.longitude,
            introduction = enrollRequest.introduction,
            whatSexDoYouWant = enrollRequest.whatSexDoYouWant,
            howOldDoYouWantMin = enrollRequest.howOldDoYouWantMin,
            howOldDoYouWantMax = enrollRequest.howOldDoYouWantMax,
            howFarCanYouGo = enrollRequest.howFarCanYouGo,
            whatKindOfBodyTypeDoYouWant = enrollRequest.whatKindOfBodyTypeDoYouWant,
            whatKindOfRelationshipStatusDoYouWant = enrollRequest.whatKindOfRelationshipStatusDoYouWant,
            whatKindOfReligionDoYouWant = enrollRequest.whatKindOfReligionDoYouWant,
            whatKindOfSmokeDoYouWant = enrollRequest.whatKindOfSmokeDoYouWant,
            whatKindOfDrinkDoYouWant = enrollRequest.whatKindOfDrinkDoYouWant,
        )

        val newMemberEntity = memberRepository.save(newMember)

        // memberProperty 생성
        memberPropertyRepository.saveAll(
            enrollRequest.propertyIds.map { memberPropertyId ->
                val newMemberProperty = MemberProperty(
                    memberId = newMemberEntity.id!!,
                    propertyId = memberPropertyId,
                )

                newMember.addMemberProperty(newMemberProperty)

                newMemberProperty
            }
        )

        // memberNotTheseCompany 생성
        memberNotTheseCompanyRepository.saveAll(
            enrollRequest.notTheseCompanyIds.map { notTheseCompanyId ->
                val memberNotTheseCompany = MemberNotTheseCompany(
                    memberId = newMemberEntity.id!!,
                    companyId = notTheseCompanyId,
                )

                newMember.addNotTheseCompany(memberNotTheseCompany)

                memberNotTheseCompany
            }
        )

        // jwt 발급
        val newMemberJwts = jwtService.issueMemberJwts(newMember)

        setJwts(response, newMemberJwts)

        return MemberResponse(newMemberEntity)
    }

    private fun setJwts(response: HttpServletResponse, jwts: Array<String>) {
        jwtService.setAccessTokenOnHeader(response, jwts[0])
        jwtService.setRefreshTokenOnHeader(response, jwts[1])
    }

    private fun setAccessToken(response: HttpServletResponse, accessToken: String) {
        jwtService.setAccessTokenOnHeader(response, accessToken)
    }
}
