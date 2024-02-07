package com.jurma.codesamples.fconvert.controller;

import com.jurma.codesamples.fconvert.entity.History;
import com.jurma.codesamples.fconvert.dto.TransferFundsDto;
import com.jurma.codesamples.fconvert.entity.Account;
import com.jurma.codesamples.fconvert.repository.AccountRepository;
import com.jurma.codesamples.fconvert.service.AccountService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
public class AccountController {

    public static final String NO_LIMIT = "-1";
    AccountRepository accountRepository;
    AccountService accountService;

    @GetMapping("/client/{id}/accounts")
    ResponseEntity<List<Account>> getClientAccounts(@PathVariable Long id) {
        List<Account> byClientId = accountRepository.findByClientId(id);
        return new ResponseEntity<>(byClientId, HttpStatus.OK);
    }

    @GetMapping("/account/{id}/history")
    ResponseEntity<List<History>> getAccountHistory(@PathVariable Long id,
                                                    @RequestParam(defaultValue = "0") Long offset,
                                                    @RequestParam(defaultValue = NO_LIMIT) Long limit) {
        List<History> history = accountService.getHistory(id, offset, limit);
        return new ResponseEntity<>(history, HttpStatus.OK);
    }

    @PostMapping("/account/transfer")
    ResponseEntity<Object> transferFunds(@Valid @RequestBody TransferFundsDto request) {
        try {
            accountService.transferFunds(request);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
        return ResponseEntity.ok().build();
    }
}
