package com.example.CryptoSpring.controller;

import com.example.CryptoSpring.exception.InputErrorException;
import com.example.CryptoSpring.model.BinanceKline;
import com.example.CryptoSpring.repository.BinanceklineMyBatisRepository;
import com.example.CryptoSpring.service.InputValidationService;
import com.example.CryptoSpring.service.KlineLoadService;
import com.example.CryptoSpring.service.KlineSearchService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
public class CryptoController {

    @Autowired
    private KlineLoadService service;

    @Autowired
    private KlineSearchService searchService;

    @Autowired
    private InputValidationService validationService;

    @GetMapping("/findbytime")
    public List<BinanceKline> getSymbolKlineByTimeRange(@RequestParam(value = "symbol") @NotBlank String symbol,
                                                        @RequestParam(value = "startTime") @NotNull Long startTime,
                                                        @RequestParam(value = "endTime") @NotNull Long endTime,
                                                        @RequestParam(value = "frequency") @NotNull Integer frequency) {
        //check symbol
        validationService.validateSymbol(symbol);
        validationService.validateTime(startTime, endTime);

        // check frequency
        if (frequency <= 0) {
            throw new InputErrorException("Frequency must be greater than 0.");
        }

        return searchService.getKlinesFromTimeRange(symbol, startTime, endTime, frequency);
    }

    @GetMapping("/insertKline")
    public int insertKline(@RequestParam(value = "symbol") @NotBlank String symbol,
                           @RequestParam(value = "startTime") @NotNull Long startTime,
                           @RequestParam(value = "endTime") @NotNull Long endTime) {

        validationService.validateSymbol(symbol);
        validationService.validateTime(startTime, endTime);

        return service.insertKline(symbol, startTime, endTime);
    }

}
