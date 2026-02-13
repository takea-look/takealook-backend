package com.takealook.push

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Push", description = "푸시 알림 API")
@RestController
@RequestMapping("/push")
class PushController(
    private val pushService: PushService,
) {

    @Operation(summary = "푸시 발송", description = "현재는 noop provider로 accepted만 반환합니다.")
    @PostMapping("/send")
    suspend fun send(@RequestBody request: PushSendRequest) = pushService.send(request)
}
