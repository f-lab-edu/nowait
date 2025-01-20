package com.nowait.utils;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class ShortUUIDGenerator {

    public static String generate() {
        UUID uuid = UUID.randomUUID();
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]); // UUID가 128비트 (16 바이트)
        byteBuffer.putLong(uuid.getMostSignificantBits()); // 상위 64비트
        byteBuffer.putLong(uuid.getLeastSignificantBits()); // 하위 64비트
        return Base64.getUrlEncoder().withoutPadding().encodeToString(byteBuffer.array());
    }
}
