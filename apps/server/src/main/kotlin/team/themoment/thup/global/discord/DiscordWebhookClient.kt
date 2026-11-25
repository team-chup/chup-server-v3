package team.themoment.thup.global.discord

import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.time.Duration

@Service
class DiscordWebhookClient {
    private val restClient: RestClient

    init {
        val requestFactory = SimpleClientHttpRequestFactory()
        // 타임아웃이 없으면 디스코드 응답이 늦어질 때 비동기 스레드 풀이 묶일 수 있다
        requestFactory.setConnectTimeout(Duration.ofSeconds(3))
        requestFactory.setReadTimeout(Duration.ofSeconds(5))
        restClient = RestClient.builder().requestFactory(requestFactory).build()
    }

    /**
     * 알림 발송은 부가 기능이므로 실패해도 원래 요청에 영향을 주지 않는다.
     * 웹훅 URL이 비어 있으면(로컬 등) 발송을 건너뛴다.
     */
    @Async
    fun send(
        webhookUrl: String?,
        embed: DiscordEmbed,
    ) {
        if (webhookUrl.isNullOrBlank()) {
            log.warn("Skipped Discord notification because webhook URL is not configured. title={}", embed.title)
            return
        }

        try {
            restClient
                .post()
                .uri(webhookUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Payload(listOf(embed)))
                .retrieve()
                .toBodilessEntity()
        } catch (e: Exception) {
            log.warn("Failed to send Discord notification. title={}, error={}", embed.title, e.message)
        }
    }

    data class Payload(
        val embeds: List<DiscordEmbed>,
    )

    companion object {
        private val log = LoggerFactory.getLogger(DiscordWebhookClient::class.java)
    }
}
