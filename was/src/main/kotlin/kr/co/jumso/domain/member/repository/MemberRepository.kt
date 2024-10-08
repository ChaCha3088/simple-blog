package kr.co.jumso.domain.member.repository

import kr.co.jumso.domain.auth.entity.RefreshToken
import kr.co.jumso.domain.member.entity.Member
import com.linecorp.kotlinjdsl.query.spec.ExpressionOrderSpec
import com.linecorp.kotlinjdsl.querydsl.expression.column
import com.linecorp.kotlinjdsl.querydsl.from.fetch
import com.linecorp.kotlinjdsl.spring.data.SpringDataQueryFactory
import com.linecorp.kotlinjdsl.spring.data.deleteQuery
import com.linecorp.kotlinjdsl.spring.data.listQuery
import com.linecorp.kotlinjdsl.spring.data.selectQuery
import jakarta.persistence.criteria.JoinType.LEFT
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MemberRepository: JpaRepository<Member, Long>, MemberCustomRepository

interface MemberCustomRepository {
    fun findNotDeletedById(memberId: Long): Member?
    fun findNotDeletedByEmail(email: String): Member?
    fun findNotDeletedByVerificationCode(verificationCode: String): Member?
    fun findNotDeletedMembers(): List<Member>
    fun findNotDeletedByEmailWithRefreshToken(email: String): Member?

    fun findNotDeletedByIdWithRefreshToken(id: Long): Member?
    fun findNotDeletedWithRefreshTokenByIdAndRefreshToken(id: Long, refreshToken: String): Member?

    fun existsByEmail(email: String): Boolean

    fun deleteByMemberId(memberId: Long)
}

class MemberCustomRepositoryImpl(
    private val queryFactory: SpringDataQueryFactory,
): MemberCustomRepository {
    override fun findNotDeletedById(memberId: Long): Member? {
        return queryFactory.selectQuery<Member> {
            select(entity(Member::class))
            from(entity(Member::class))
            where(column(Member::id).equal(memberId))
            where(column(Member::isDeleted).equal(false))
        }.resultList.firstOrNull()
    }

    override fun findNotDeletedByEmail(email: String): Member? {
        return queryFactory.selectQuery<Member> {
            select(entity(Member::class))
            from(entity(Member::class))
            where(column(Member::email).equal(email))
            where(column(Member::isDeleted).equal(false))
        }.resultList.firstOrNull()
    }

    override fun findNotDeletedByVerificationCode(verificationCode: String): Member? {
        return queryFactory.selectQuery<Member> {
            select(entity(Member::class))
            from(entity(Member::class))
            where(column(Member::verificationCode).equal(verificationCode))
            where(column(Member::isDeleted).equal(false))
        }.resultList.firstOrNull()
    }

    override fun findNotDeletedMembers(): List<Member> {
        return queryFactory.listQuery {
            select(entity(Member::class))
            from(entity(Member::class))
            where(column(Member::isDeleted).equal(false))
            orderBy(ExpressionOrderSpec(column(Member::id), true))
        }
    }

    override fun findNotDeletedByEmailWithRefreshToken(email: String): Member? {
        return queryFactory.selectQuery {
            select(entity(Member::class))
            from(entity(Member::class))
            where(column(Member::email).equal(email))
            where(column(Member::isDeleted).equal(false))
            fetch(Member::refreshToken, joinType = LEFT)
        }.resultList.firstOrNull()
    }

    override fun findNotDeletedByIdWithRefreshToken(id: Long): Member? {
        return queryFactory.selectQuery {
            select(entity(Member::class))
            from(entity(Member::class))
            where(column(Member::id).equal(id))
            where(column(Member::isDeleted).equal(false))
            fetch(Member::refreshToken, joinType = LEFT)
        }.resultList.firstOrNull()
    }

    override fun findNotDeletedWithRefreshTokenByIdAndRefreshToken(id: Long, refreshToken: String): Member? {
        return queryFactory.selectQuery {
            select(entity(Member::class))
            from(entity(Member::class))
            where(column(Member::id).equal(id))
            where(column(Member::isDeleted).equal(false))
            where(column(Member::refreshToken).isNotNull().and(column(RefreshToken::token).equal(refreshToken)))
            fetch(Member::refreshToken, joinType = LEFT)
        }.resultList.firstOrNull()
    }

    override fun existsByEmail(email: String): Boolean {
        return queryFactory.selectQuery<Member> {
            select(entity(Member::class))
            from(entity(Member::class))
            where(column(Member::email).equal(email))
        }.resultList.isNotEmpty()
    }

    override fun deleteByMemberId(memberId: Long) {
        queryFactory.deleteQuery<Member> {
            where(column(Member::id).equal(memberId))
        }
    }
}
