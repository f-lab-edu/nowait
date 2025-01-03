package com.nowait.controller.api;

import com.nowait.controller.api.dto.request.SetVacancyNotificationReq;
import com.nowait.controller.api.dto.response.ApiResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationApi {

    /**
     * 빈자리 알림 신청 API
     *
     * @param request 빈자리 알림 신청 요청
     * @return 빈자리 알림 신청 결과
     */
    @PostMapping("/vacancy")
    public ApiResult<Void> setVacancyNotification(
        @RequestBody @Valid SetVacancyNotificationReq request
    ) {
        // TODO: 빈자리 알림 신청 비즈니스 로직 호출

        return ApiResult.ok(null);
    }
}
