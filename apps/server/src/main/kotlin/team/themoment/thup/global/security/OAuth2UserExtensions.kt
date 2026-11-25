package team.themoment.thup.global.security

import org.springframework.security.oauth2.core.user.OAuth2User

/**
 * 로그인 시 세션에 저장한 OAuth2User의 "id" 속성(DB 사용자 ID)을 꺼낸다.
 */
val OAuth2User.userId: Long
    get() = (getAttribute<Any>("id") as Number).toLong()
