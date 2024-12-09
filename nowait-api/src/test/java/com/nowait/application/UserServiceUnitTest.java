package com.nowait.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nowait.domain.repository.UserRepository;
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
class UserServiceUnitTest {

    @InjectMocks
    UserService userService;

    @Mock
    UserRepository userRepository;

    @Nested
    @DisplayName("사용자 존재 여부 검증 테스트")
    class ValidateUserExistTest {

        long userId;
        String errorMessage;

        @BeforeEach
        void setUp() {
            userId = 1L;
            errorMessage = "해당 식별자를 가진 사용자가 존재하지 않습니다.";
        }

        @DisplayName("사용자가 존재하는 경우, 검증에 성공한다.")
        @Test
        void validateUserExist() {
            // given
            when(userRepository.existsById(userId)).thenReturn(true);

            // when & then
            assertDoesNotThrow(() -> userService.validateUserExist(userId, errorMessage));
            verify(userRepository).existsById(userId);
        }

        @DisplayName("사용자가 존재하지 않는 경우, 입력받은 예외 메시지를 포함한 예외가 발생한다.")
        @Test
        void validateUserNotExist() {
            // given
            when(userRepository.existsById(userId)).thenReturn(false);

            // when & then
            assertThatThrownBy(() -> userService.validateUserExist(userId, errorMessage))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage(errorMessage);

            verify(userRepository).existsById(userId);
        }
    }

}
