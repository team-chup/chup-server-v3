package team.themoment.thup.domain.job.util

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockMultipartFile
import team.themoment.sdk.exception.ExpectedException
import team.themoment.thup.domain.job.entity.constant.AttachmentFileType

class AttachmentFileTypeResolverTest :
    DescribeSpec({
        fun file(
            fileName: String,
            contentType: String?,
        ) = MockMultipartFile("attachments", fileName, contentType, byteArrayOf(1))

        describe("resolve") {
            it("확장자 또는 Content-Type이 PDF면 PDF로 판별한다") {
                AttachmentFileTypeResolver.resolve(file("공고.PDF", "application/octet-stream")) shouldBe AttachmentFileType.PDF
                AttachmentFileTypeResolver.resolve(file("공고", "application/pdf")) shouldBe AttachmentFileType.PDF
            }

            it("hwpx와 hwp 확장자를 구분한다") {
                AttachmentFileTypeResolver.resolve(file("공고.hwpx", null)) shouldBe AttachmentFileType.HWPX
                AttachmentFileTypeResolver.resolve(file("공고.hwp", null)) shouldBe AttachmentFileType.HWP
            }

            it("image/ Content-Type이면 IMAGE로 판별한다") {
                AttachmentFileTypeResolver.resolve(file("포스터.png", "image/png")) shouldBe AttachmentFileType.IMAGE
            }

            it("지원하지 않는 형식이면 400을 던진다") {
                val exception =
                    shouldThrow<ExpectedException> {
                        AttachmentFileTypeResolver.resolve(file("공고.zip", "application/zip"))
                    }

                exception.statusCode shouldBe HttpStatus.BAD_REQUEST
            }
        }
    })
