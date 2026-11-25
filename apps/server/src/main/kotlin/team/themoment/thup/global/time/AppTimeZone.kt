package team.themoment.thup.global.time

import java.time.ZoneId

/**
 * 앱 컨테이너에 TZ가 지정되어 있지 않아 JVM 기본 시간대가 UTC다.
 * 모집 기간처럼 날짜 경계가 결과를 바꾸는 로직은 이 상수로 한국 시간을 명시한다.
 */
object AppTimeZone {
    const val KST_ID = "Asia/Seoul"
    val KST: ZoneId = ZoneId.of(KST_ID)
}
