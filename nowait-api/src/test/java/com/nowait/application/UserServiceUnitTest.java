package com.nowait.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nowait.domain.repository.UserRepository;
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

        @DisplayName("사용자가 존재하는 경우, true를 반환한다.")
        @Test
        void existsById() {
            // given
            when(userRepository.existsById(userId)).thenReturn(true);

            // when
            boolean exist = userService.existsById(userId);

            // then
            assertThat(exist).isTrue();
            verify(userRepository).existsById(userId);
        }

        @DisplayName("사용자가 존재하지 않는 경우, false를 반환한다.")
        @Test
        void existsByIdWhenUserNotExist() {
            // given
            when(userRepository.existsById(userId)).thenReturn(false);

            // when
            boolean exist = userService.existsById(userId);

            // then
            assertThat(exist).isFalse();
            verify(userRepository).existsById(userId);
        }
    }

}
