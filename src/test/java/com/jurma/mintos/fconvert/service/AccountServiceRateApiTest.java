package com.jurma.mintos.fconvert.service;

import com.jurma.mintos.fconvert.repository.AccountRepository;
import com.jurma.mintos.fconvert.repository.HistoryRepository;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountServiceRateApiTest {
    private static MockWebServer webServer;
    private final RestClient restClient = RestClient.create(String.format("http://%s:8080", webServer.getHostName()));
    private final RetryTemplate retryTemplate = mock(RetryTemplate.class);
    private final AccountService service = new AccountService(mock(AccountRepository.class), mock(HistoryRepository.class), restClient, retryTemplate);

    /*Todo: add tests for retries ()
    another test class with
    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(
              classes = RetryConfig.class,
              loader = AnnotationConfigContextLoader.class)
     */
    @BeforeAll
    static void setUp() throws IOException {
        webServer = new MockWebServer();
        webServer.start(8080);
    }

    @SuppressWarnings("rawtypes")
    @BeforeEach
    void generalMocking() {
        when(retryTemplate.execute(any())).thenAnswer(invocation -> {
            RetryCallback retry = invocation.getArgument(0);
            return retry.doWithRetry(null);
        });
    }

    @AfterAll
    static void tearDown() throws IOException {
        webServer.shutdown();
    }

    @Test
    public void getRate_ok() throws Exception {
        webServer.enqueue(new MockResponse().setBody("""
                        {
                          "success": true,
                          "terms": "https:\\/\\/currencylayer.com\\/terms",
                          "privacy": "https:\\/\\/currencylayer.com\\/privacy",
                          "timestamp": 1705497723,
                          "source": "EUR",
                          "quotes": {
                            "EURUSD": 1.086844
                          }
                        }
                        """)
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json; charset=utf-8"));

        var rate = service.getConversionRate("EUR", "USD");
        RecordedRequest request = webServer.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals(rate, BigDecimal.valueOf(1.086844));
    }

    @Test
    public void getRate_noRateFound() {
        webServer.enqueue(new MockResponse().setBody("""
                        {
                          "success": true,
                          "terms": "https:\\/\\/currencylayer.com\\/terms",
                          "privacy": "https:\\/\\/currencylayer.com\\/privacy",
                          "timestamp": 1705497723,
                          "source": "EUR",
                          "quotes": {}
                        }
                        """)
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json; charset=utf-8"));
        var exception = assertThrows(IllegalStateException.class, () -> service.getConversionRate("EUR", "USD"));
        assertThat(exception.getMessage()).contains("No conversion rate found for requested operation.");
    }

    @Test
    public void getRate_nok() {
        webServer.enqueue(new MockResponse().setBody("any error")
                .setResponseCode(400)
                .addHeader("Content-Type", "application/json; charset=utf-8"));
        var exception = assertThrows(HttpClientErrorException.class, () -> service.getConversionRate("EUR", "USD"));
        assertThat(exception.getMessage()).contains("any error");
    }

    @Test
    public void getRate_unsuccessful() {
        webServer.enqueue(new MockResponse().setBody("""
                        {
                          "success": false,
                          "error": {
                            "code": 101,
                            "type": "missing_access_key",
                            "info": "You have not supplied an API Access Key. [Required format: access_key=YOUR_ACCESS_KEY]"
                          }
                        }
                                                """)
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json; charset=utf-8"));

        var exception = assertThrows(IllegalStateException.class, () -> service.getConversionRate("EUR", "USD"));
        assertThat(exception.getMessage()).contains("Conversion rate service request is unsuccessful.");
    }
}
