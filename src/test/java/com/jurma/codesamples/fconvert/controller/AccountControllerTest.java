package com.jurma.codesamples.fconvert.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jurma.codesamples.fconvert.configuration.AllowedCurrencyCodes;
import com.jurma.codesamples.fconvert.entity.History;
import com.jurma.codesamples.fconvert.dto.TransferFundsDto;
import com.jurma.codesamples.fconvert.entity.Account;
import com.jurma.codesamples.fconvert.repository.AccountRepository;
import com.jurma.codesamples.fconvert.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AccountService service;
    @MockBean
    private AccountRepository accountRepository;

    @Test
    void getAccountsByClientId_ok() throws Exception {
        Account a1 = new Account();
        a1.setId(1L);
        Account a2 = new Account();
        a2.setId(2L);
        when(accountRepository.findByClientId(1L)).thenReturn(List.of(a1, a2));
        mockMvc.perform(get("/client/1/accounts"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        [{"id":1,"currency":null,"funds":null},
                        {"id":2,"currency":null,"funds":null}]
                        """));
    }

    @Test
    void getHistoryByAccountId_ok() throws Exception {
        History h1 = new History();
        h1.setId(1L);
        var dateString = "2007-25-06";
        var formatter = new SimpleDateFormat("yyyy-dd-MM");
        var date = formatter.parse(dateString);
        var account = new Account();
        account.setId(1L);
        h1.setAccount(account);
        h1.setDate(date);
        var h2 = new History();
        h2.setId(2L);
        h2.setAccount(account);
        h2.setDate(date);

        when(service.getHistory(1L, 0L, -1L)).thenReturn(List.of(h1, h2));
        mockMvc.perform(get("/account/1/history"))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                                [{"id":1,"amount":null,"date":"2007-06-24 21:00:00"},
                                {"id":2,"amount":null,"date":"2007-06-24 21:00:00"}]
                        """));
    }

    @Test
    void performOperationOnFunds_illegalArgument() throws Exception {
        var dto = new TransferFundsDto(1L, 2L, Currency.getInstance(AllowedCurrencyCodes.EUR.name()), BigDecimal.valueOf(100.0));

        var mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        var ow = mapper.writer().withDefaultPrettyPrinter();
        var requestJson = ow.writeValueAsString(dto);

        doThrow(new IllegalArgumentException("Error to test")).when(service).transferFunds(dto);

        mockMvc.perform(post("/account/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Error to test"));
    }

    @Test
    void performOperationOnFunds_invalidAccountId() throws Exception {
        var dto = new TransferFundsDto(null, 2L, Currency.getInstance(AllowedCurrencyCodes.EUR.name()), BigDecimal.valueOf(100.0));

        var mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        var ow = mapper.writer().withDefaultPrettyPrinter();
        var requestJson = ow.writeValueAsString(dto);

        mockMvc.perform(post("/account/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().is4xxClientError())
                .andExpect(status().reason(containsString("Invalid request content.")));
    }

    @Test
    void performOperationOnFunds_invalidTargetAccountId() throws Exception {
        var dto = new TransferFundsDto(1L, null, Currency.getInstance(AllowedCurrencyCodes.EUR.name()), BigDecimal.valueOf(100.0));

        var mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        var ow = mapper.writer().withDefaultPrettyPrinter();
        var requestJson = ow.writeValueAsString(dto);

        mockMvc.perform(post("/account/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().is4xxClientError())
                .andExpect(status().reason(containsString("Invalid request content.")));
    }

    @Test
    void performOperationOnFunds_ok() throws Exception {
        var dto = new TransferFundsDto(1L, 2L, Currency.getInstance(AllowedCurrencyCodes.EUR.name()), BigDecimal.valueOf(100.0));

        var mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        var ow = mapper.writer().withDefaultPrettyPrinter();
        var requestJson = ow.writeValueAsString(dto);

        doNothing().when(service).transferFunds(dto);

        mockMvc.perform(post("/account/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());
    }
}