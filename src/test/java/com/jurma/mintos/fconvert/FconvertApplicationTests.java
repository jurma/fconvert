package com.jurma.mintos.fconvert;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class FConvertApplicationTests {
    @Test
    void contextLoads(ApplicationContext context) {
        assertThat(context).isNotNull();
    }

}
