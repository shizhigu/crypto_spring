package com.example.CryptoSpring.service;

import com.example.CryptoSpring.exception.InputErrorException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class InputValidationService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${TradeApi}")
    private String tradeApi;

    public void validateSymbol(@NotBlank String symbol) {
        try {
            String url = String.format(tradeApi, symbol);
            restTemplate.getForEntity(url, Object[].class);
        } catch (HttpClientErrorException ex) {
            String error = String.format("input: %s, error message:%s", symbol, ex.getMessage());
            throw new InputErrorException(error);
        }
    }

    public void validateTime(@NotNull Long startTime, @NotNull Long endTime) {
        if (endTime <= startTime) {
            throw new InputErrorException("Start time must be before end time. startTime = " + startTime + ", endTime = " + endTime);
        }
    }
}
