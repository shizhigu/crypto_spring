package com.example.CryptoSpring.service;

import com.example.CryptoSpring.exception.InputErrorException;
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

    public void validateSymbol(String symbol) {
        try {
            String url = String.format(tradeApi, symbol);
            restTemplate.getForEntity(url, Object[].class);
        } catch (HttpClientErrorException ex) {
            throw new InputErrorException(ex.getMessage());
        }
    }

    public void validateTime(Long startTime, Long endTime) {
        if (endTime <= startTime) {
            throw new InputErrorException("Start time must be before end time. startTime = " + startTime + ", endTime = " + endTime);
        }
    }
}
