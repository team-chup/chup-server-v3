package team.themoment.thup

import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ThupApplicationTests : FunSpec() {
    init {
        extension(SpringExtension())

        test("애플리케이션 컨텍스트가 정상적으로 로드된다") {}
    }
}
