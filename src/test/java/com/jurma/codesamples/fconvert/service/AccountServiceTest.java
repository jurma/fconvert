package com.jurma.codesamples.fconvert.service;

import com.jurma.codesamples.fconvert.dto.TransferFundsDto;
import com.jurma.codesamples.fconvert.entity.History;
import com.jurma.codesamples.fconvert.repository.HistoryRepository;
import com.jurma.codesamples.fconvert.entity.Account;
import com.jurma.codesamples.fconvert.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private HistoryRepository historyRepository;
    @InjectMocks
    private AccountService accountService;


    @Test
    public void testSaveHistory() {
        Mockito.doReturn(new History()).when(historyRepository).save(any());
        Account a1 = mock(Account.class);
        Account a2 = mock(Account.class);
        accountService.saveHistory(BigDecimal.valueOf(10), BigDecimal.valueOf(1.125), a1, a2);

        verify(historyRepository).save(argThat((History hSource)
                -> hSource.getAccount() == a1 && hSource.getAmount().compareTo(BigDecimal.valueOf(-1.125)) == 0));
        verify(historyRepository).save(argThat((History hTarget)
                -> hTarget.getAccount() == a2 && hTarget.getAmount().compareTo(BigDecimal.valueOf(10)) == 0));
        verifyNoMoreInteractions(historyRepository);
    }

    @Test
    public void getHistory_noLimit() {
        accountService.getHistory(1L, 10L, -1L);
        verify(historyRepository).findByAccountIdWithOffsetQuery(1L, 10L);
        verifyNoMoreInteractions(historyRepository);
    }

    @Test
    public void getHistory_withLimit() {
        accountService.getHistory(1L, 10L, 1L);

        verify(historyRepository).findByAccountIdWithOffsetLimitQuery(1L, 10L, 1L);
        verifyNoMoreInteractions(historyRepository);
    }

    @Test
    public void updateAmounts_errorInsufficientFunds() {
        var source = new Account().setFunds(BigDecimal.ONE);
        var target = new Account().setFunds(BigDecimal.ZERO);

        var exception = assertThrows(IllegalArgumentException.class,
                () -> accountService.updateAmounts(BigDecimal.TEN, BigDecimal.valueOf(1.1), source, target));
        assertThat(exception.getMessage()).contains("Insufficient funds for the operation.");
    }

    @Test
    public void updateAmounts_ok() {
        var source = new Account().setId(1L).setFunds(BigDecimal.TEN);
        var target = new Account().setId(2L).setFunds(BigDecimal.ZERO);
        accountService.updateAmounts(BigDecimal.TEN, BigDecimal.valueOf(1.1), source, target);

        verify(accountRepository).save(argThat((Account accSource)
                -> accSource.getFunds().compareTo(BigDecimal.valueOf(8.9)) == 0 && accSource.getId() == 1L));
        verify(accountRepository).save(argThat((Account accTarget)
                -> accTarget.getFunds().compareTo(BigDecimal.TEN) == 0 && accTarget.getId() == 2L));
        verifyNoMoreInteractions(accountRepository);
    }

    @Test
    public void transferFunds_errorAccountNotExist() {
        var dto = new TransferFundsDto(1L, 2L, Currency.getInstance("EUR"), BigDecimal.ONE);
        var aTarget = new Account().setId(2L);
        doReturn(Optional.empty()).when(accountRepository).findById(1L);
        doReturn(Optional.of(aTarget)).when(accountRepository).findById(2L);

        var exception = assertThrows(IllegalArgumentException.class, () -> accountService.transferFunds(dto));
        assertThat(exception.getMessage()).contains("Either source or target account does not exist");
    }

    @Test
    public void transferFunds_errorCurrencyNotComply() {
        var dto = new TransferFundsDto(1L, 2L, Currency.getInstance("EUR"), BigDecimal.ONE);
        var aSource = new Account().setId(1L);
        var aTarget = new Account().setId(2L).setCurrency("GBP");
        doReturn(Optional.of(aSource)).when(accountRepository).findById(1L);
        doReturn(Optional.of(aTarget)).when(accountRepository).findById(2L);

        var exception = assertThrows(IllegalArgumentException.class, () -> accountService.transferFunds(dto));
        assertThat(exception.getMessage()).contains("Target account currency is not the same as requested");
    }

    @Test
    public void transferFunds_ok() {
        var dto = new TransferFundsDto(1L, 2L, Currency.getInstance("GBP"), BigDecimal.ONE);
        var aSource = new Account().setId(1L).setCurrency("EUR").setFunds(BigDecimal.TEN);
        var aTarget = new Account().setId(2L).setCurrency("GBP").setFunds(BigDecimal.ZERO);
        doReturn(Optional.of(aSource)).when(accountRepository).findById(1L);
        doReturn(Optional.of(aTarget)).when(accountRepository).findById(2L);

        var spyService = spy(accountService);
        doReturn(BigDecimal.valueOf(1.1)).when(spyService).getConversionRate("EUR", "GBP");
        doNothing().when(spyService).updateAmounts(any(), any(), any(), any());
        doNothing().when(spyService).saveHistory(any(), any(), any(), any());
        spyService.transferFunds(dto);

        verify(spyService).updateAmounts(BigDecimal.ONE, BigDecimal.valueOf(0.909091), aSource, aTarget);
        verify(spyService).saveHistory(BigDecimal.ONE, BigDecimal.valueOf(0.909091), aSource, aTarget);
    }
}