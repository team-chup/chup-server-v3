package team.themoment.thup.global.storage

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class ZipBuilderTest :
    DescribeSpec({
        describe("uniqueName") {
            it("처음 쓰이는 이름은 그대로 반환한다") {
                ZipBuilder.uniqueName("이력서.pdf", mutableMapOf()) shouldBe "이력서.pdf"
            }

            it("중복된 이름은 확장자 앞에 (n)을 붙인다") {
                val usedNames = mutableMapOf<String, Int>()

                ZipBuilder.uniqueName("이력서.pdf", usedNames) shouldBe "이력서.pdf"
                ZipBuilder.uniqueName("이력서.pdf", usedNames) shouldBe "이력서(1).pdf"
                ZipBuilder.uniqueName("이력서.pdf", usedNames) shouldBe "이력서(2).pdf"
            }

            it("확장자가 없으면 이름 끝에 (n)을 붙인다") {
                val usedNames = mutableMapOf<String, Int>()

                ZipBuilder.uniqueName("홍길동_3101_백엔드", usedNames)
                ZipBuilder.uniqueName("홍길동_3101_백엔드", usedNames) shouldBe "홍길동_3101_백엔드(1)"
            }
        }
    })
