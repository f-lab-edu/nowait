package com.nowait.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UrlUtilsUnitTest {

    @DisplayName("url에 query를 추가한다.")
    @Test
    void appendQuery() {
        // given
        String url = "http://domain/payment/approve";

        // when
        String result = UrlUtils.appendQuery(url, "key", "value");

        // then
        assertThat(result).isEqualTo("http://domain/payment/approve?key=value");
    }

    @DisplayName("이미 query가 있는 경우, url에 query를 추가한다.")
    @Test
    void appendQueryWithQuery() {
        // given
        String url = "http://domain/payment/approve?query=exists";

        // when
        String result = UrlUtils.appendQuery(url, "key", "value");

        // then
        assertThat(result).isEqualTo("http://domain/payment/approve?query=exists&key=value");
    }

}
