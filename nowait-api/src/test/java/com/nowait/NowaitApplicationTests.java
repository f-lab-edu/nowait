package com.nowait;

import com.nowait.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestConfig.class)
class NowaitApplicationTests {

    @Test
    void contextLoads() {
    }

}
