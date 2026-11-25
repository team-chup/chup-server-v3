package team.themoment.thup.global

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import team.themoment.thup.domain.application.controller.ApplicationController
import team.themoment.thup.domain.application.dto.AdminApplicantResponse
import team.themoment.thup.domain.application.entity.constant.ApplicationSource
import team.themoment.thup.domain.application.entity.constant.ApplicationStatus
import team.themoment.thup.domain.dashboard.dto.response.RecommendedJobResDto
import team.themoment.thup.domain.user.controller.AdminUserController
import team.themoment.thup.domain.user.entity.UserJpaEntity
import team.themoment.thup.domain.user.entity.constant.Role
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule
import tools.jackson.module.kotlin.readValue

/**
 * v2(Java record/Lombok)와 JSON 키가 동일하게 유지되는지 검증한다.
 * Kotlin은 is- 접두사 Boolean, 한 글자 소문자 뒤 대문자(dDay) 같은 이름에서 Jackson 키가 달라질 수 있다.
 */
class JsonContractTest :
    DescribeSpec({
        val mapper = JsonMapper.builder().addModule(KotlinModule.Builder().build()).build()

        fun toMap(value: Any): Map<String, Any?> = mapper.readValue(mapper.writeValueAsString(value))

        describe("응답 DTO 직렬화") {
            it("RecommendedJobResDto는 dDay 키를 사용한다") {
                val json = toMap(RecommendedJobResDto(id = 1L, companyName = "더모먼트", dDay = 3L))

                json.keys shouldContain "dDay"
                json.keys shouldNotContain "dday"
                json["dDay"] shouldBe 3
            }

            it("AdminApplicantResponse는 isExternal 키를 사용한다") {
                val json =
                    toMap(
                        AdminApplicantResponse(
                            id = 1L,
                            name = "홍길동",
                            studentId = "3101",
                            email = "s@gsm.hs.kr",
                            phoneNumber = null,
                            companyName = "더모먼트",
                            positionName = "백엔드",
                            applicationSource = ApplicationSource.EXTERNAL,
                            sourcePlatform = "원티드",
                            isExternal = true,
                            status = ApplicationStatus.APPLIED,
                            appliedAt = null,
                        ),
                    )

                json.keys shouldContain "isExternal"
                json.keys shouldNotContain "external"
                json["isExternal"] shouldBe true
            }

            it("UserJpaEntity는 v2와 같은 키(approved, version 등)로 직렬화된다") {
                val user =
                    UserJpaEntity(
                        role = Role.STUDENT,
                        studentId = "3101",
                        grade = 3,
                        name = "홍길동",
                        email = "s@gsm.hs.kr",
                        approved = true,
                    )

                toMap(user).keys shouldBe
                    setOf("id", "role", "studentId", "grade", "name", "email", "phoneNumber", "approved", "version", "createdAt")
            }
        }

        describe("요청 DTO 역직렬화") {
            it("누락된 필드는 null로 바인딩되어 서비스의 400 검증으로 이어진다") {
                val request = mapper.readValue<ApplicationController.ManualApplicantRequest>("{}")

                request.userId.shouldBeNull()
                request.status.shouldBeNull()
            }

            it("단일 필드 요청도 객체 형태로 바인딩된다") {
                val request = mapper.readValue<AdminUserController.SwitchRoleRequest>("""{"role":"ADMIN"}""")

                request.role shouldBe Role.ADMIN
            }
        }
    })
