package com.jurma.codesamples.fconvert.service;

import com.jurma.codesamples.fconvert.controller.AccountController;
import com.jurma.codesamples.fconvert.dto.TransferFundsDto;
import com.jurma.codesamples.fconvert.entity.Account;
import com.jurma.codesamples.fconvert.entity.History;
import com.jurma.codesamples.fconvert.repository.AccountRepository;
import com.jurma.codesamples.fconvert.repository.HistoryRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class AccountService {
    private AccountRepository accountRepository;
    private HistoryRepository historyRepository;
    private RestClient restClient;
    private RetryTemplate retryTemplate;

    @Transactional
    public void transferFunds(TransferFundsDto dto) {
        var sourceAccount = accountRepository.findById(dto.sourceAccountId());
        var targetAccount = accountRepository.findById(dto.targetAccountId());
        if (sourceAccount.isEmpty() || targetAccount.isEmpty()) {
            throw new IllegalArgumentException("Either source or target account does not exist");
        }

        var source = sourceAccount.get();
        var target = targetAccount.get();

        if (!target.getCurrency().equals(dto.targetCurrency().getCurrencyCode())) {
            throw new IllegalArgumentException("Target account currency is not the same as requested");
        }

        var rate = getConversionRate(source.getCurrency(), target.getCurrency());
        log.info("Found conversion rate {}->{} = {}", source.getCurrency(), target.getCurrency(), rate);
        var reserveSourceAmount = dto.amount().divide(rate, 6, RoundingMode.HALF_EVEN);

        updateAmounts(dto.amount(), reserveSourceAmount, source, target);
        saveHistory(dto.amount(), reserveSourceAmount, source, target);
    }

    void updateAmounts(BigDecimal amount, BigDecimal reserveSourceAmount, Account source, Account target) {
        if (source.getFunds().compareTo(reserveSourceAmount) < 0) {
            throw new IllegalArgumentException("Insufficient funds for the operation.");
        }
        source.setFunds(source.getFunds().subtract(reserveSourceAmount));
        target.setFunds(target.getFunds().add(amount));
        accountRepository.save(source);
        accountRepository.save(target);
    }

    void saveHistory(BigDecimal transferAmount, BigDecimal reserveSourceAmount, Account source, Account target) {
        var date = new Date();
        var historyEntrySource = new History()
                .setAmount(reserveSourceAmount.negate())
                .setDate(date)
                .setAccount(source);

        var historyEntryTarget = new History()
                .setAmount(transferAmount)
                .setDate(date)
                .setAccount(target);

        historyRepository.save(historyEntrySource);
        historyRepository.save(historyEntryTarget);
    }

    BigDecimal getConversionRate(String sourceCurrency, String targetCurrency) {

        var conversionRate = retryTemplate.execute(retryContext -> requestApi(sourceCurrency, targetCurrency));
        var body = conversionRate.getBody();
        assert body != null;

        if (!body.success()) {
            throw new IllegalStateException("Conversion rate service request is unsuccessful.");
        }
        return body.quotes().entrySet().stream()
                .findFirst().map(Map.Entry::getValue)
                .orElseThrow(() -> new IllegalStateException("No conversion rate found for requested operation."));
    }

    private ResponseEntity<ConversionRateDto> requestApi(String sourceCurrency, String targetCurrency) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("source", sourceCurrency)
                        .queryParam("currencies", targetCurrency)
                        .build())
                .retrieve()
                .toEntity(ConversionRateDto.class);
    }

    public List<History> getHistory(Long accountId, Long offset, Long limit) {
        if (Long.valueOf(AccountController.NO_LIMIT).equals(limit)) {
            return historyRepository.findByAccountIdWithOffsetQuery(accountId, offset);
        } else {
            return historyRepository.findByAccountIdWithOffsetLimitQuery(accountId, offset, limit);
        }
    }

    record ConversionRateDto(boolean success, Map<String, BigDecimal> quotes) {
    }
}
