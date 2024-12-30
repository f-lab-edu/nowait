package com.nowait.util;

import lombok.NoArgsConstructor;
import org.springframework.web.util.UriComponentsBuilder;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class UrlUtils {

    public static String appendQuery(String url, String key, String value) {
        return UriComponentsBuilder.fromUriString(url)
            .queryParam(key, value)
            .build()
            .toUriString();
    }
}
