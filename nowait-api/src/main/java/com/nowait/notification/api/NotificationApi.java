package com.nowait.notification.api;

import com.nowait.common.api.dto.ApiResult;
import com.nowait.notification.dto.request.SetVacancyNotificationReq;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<ApiResult<Void>> setVacancyNotification(
        @RequestBody @Valid SetVacancyNotificationReq request
    ) {
        // TODO: 빈자리 알림 신청 비즈니스 로직 호출
        ApiResult<Void> result = ApiResult.ok(null);

        return ResponseEntity.ok(result);
    }
}
