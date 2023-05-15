package com.example.CryptoSpring.service;

import com.example.CryptoSpring.exception.InputErrorException;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class InputValidationServiceTest {
    @InjectMocks
    private InputValidationService service;

    @Spy
    private RestTemplate restTemplate;

    @Value("${TradeApi}")
    private String tradeApi;

    @Test
    public void test_validate_symbol() {
        ReflectionTestUtils.setField(service, "tradeApi", tradeApi);
        Exception ex = assertThrows(InputErrorException.class, () -> {
            service.validateSymbol("BTC");
        });

        assertDoesNotThrow( ()->{ service.validateSymbol("BTCUSD"); } );

    }

    @Test
    public void test_validate_time() {
        assertThrows(InputErrorException.class, () -> {
            service.validateTime(10L, 1L);
        });

        assertThrows(InputErrorException.class, () -> {
            service.validateTime(10L, 10L);
        });

        assertDoesNotThrow( ()->{ service.validateTime(1L, 10L); } );

    }
}
