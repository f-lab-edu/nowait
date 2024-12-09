package com.nowait.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nowait.domain.repository.PlaceRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlaceServiceUnitTest {

    @InjectMocks
    PlaceService placeService;

    @Mock
    PlaceRepository placeRepository;

    @Nested
    @DisplayName("플레이스 존재 여부 검증 테스트")
    class ValidatePlaceExistTest {

        long placeId;
        String errorMessage;

        @BeforeEach
        void setUp() {
            placeId = 1L;
            errorMessage = "장소가 존재하지 않습니다.";
        }

        @DisplayName("플레이스가 존재하는 경우, 검증에 성공한다.")
        @Test
        void validatePlaceExist() {
            // given
            when(placeRepository.existsById(placeId)).thenReturn(true);

            // when & then
            assertDoesNotThrow(() -> placeService.validatePlaceExist(placeId, errorMessage));
            verify(placeRepository).existsById(placeId);
        }

        @DisplayName("플레이스가 존재하지 않는 경우, 입력받은 예외 메시지를 포함한 예외가 발생한다.")
        @Test
        void validatePlaceNotExist() {
            // given
            when(placeRepository.existsById(placeId)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> placeService.validatePlaceExist(placeId, errorMessage))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(errorMessage);

            verify(placeRepository).existsById(placeId);
        }
    }
}
