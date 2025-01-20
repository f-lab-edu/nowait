package com.nowait.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ShortUUIDGeneratorUnitTest {

    @DisplayName("길이가 22인 UUID를 생성한다.")
    @Test
    void generate() {
        // when
        String uuid = ShortUUIDGenerator.generate();

        // then
        assertThat(uuid.length()).isEqualTo(22);
    }

}
