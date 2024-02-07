package com.jurma.codesamples.fconvert.entity;

import com.jurma.codesamples.fconvert.repository.ClientRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class ClientTest {
    @Autowired
    ClientRepository clientRepository;

    @Test
    public void testEntity_name_null() {
        var client = new Client();
        var exception = assertThrows(ConstraintViolationException.class, () -> clientRepository.save(client));
        assertThat(exception.getMessage()).contains("must not be null");
    }

}