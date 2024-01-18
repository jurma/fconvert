package com.jurma.mintos.fconvert;

import com.jurma.mintos.fconvert.controller.AccountController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
class FConvertApplicationTests {
    @Autowired
    private AccountController controller;
    @Test
    void contextLoads() {
        assertThat(controller).isNotNull();
    }

}
