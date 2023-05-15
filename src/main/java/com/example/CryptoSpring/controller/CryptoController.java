package com.example.CryptoSpring.controller;

import com.example.CryptoSpring.exception.InputErrorException;
import com.example.CryptoSpring.model.BinanceKline;
import com.example.CryptoSpring.repository.BinanceklineMyBatisRepository;
import com.example.CryptoSpring.service.InputValidationService;
import com.example.CryptoSpring.service.KlineService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

//import static sun.jvm.hotspot.code.CompressedStream.L;

@RestController
public class CryptoController {

    @Autowired
    private BinanceklineMyBatisRepository binanceklineMyBatisRepository;

    @Autowired
    private KlineService service;

    @Autowired
    private InputValidationService validationService;

    @GetMapping("/getall")
    public List<BinanceKline> getAll() {
        return binanceklineMyBatisRepository.findAll();
    }

    // @NotBlank, @NotNull, @Range(min=,max=, message=)
    @GetMapping("/findbytime")
    public List<BinanceKline> getSymbolKlineByTimeRange(@RequestParam(value = "symbol") @NotBlank String symbol,
                                                        @RequestParam(value = "startTime") @NotNull Long startTime,
                                                        @RequestParam(value = "endTime") @NotNull Long endTime,
                                                        @RequestParam(value = "frequency") @NotNull Integer frequency) {
        // parameter validation
        // Exception do you want to handle it
        // try{} catch{}

        //check symbol
        validationService.validateSymbol(symbol);
        validationService.validateTime(startTime, endTime);

        // check frequency
        if (frequency <= 0) {
            throw new InputErrorException("Frequency must be greater than 0.");
        }

        return service.getKlinesFromTimeRange(symbol, startTime, endTime, frequency);
    }

    @GetMapping("/insertKline")
    public int insertKline(@RequestParam(value = "symbol") @NotBlank String symbol,
                           @RequestParam(value = "startTime") @NotNull Long startTime,
                           @RequestParam(value = "endTime") @NotNull Long endTime) {

        validationService.validateSymbol(symbol);
        validationService.validateTime(startTime, endTime);

        return service.insertKline(symbol, startTime, endTime);
    }

//    @GetMapping("/testredis")
//    public String testRedis() {
//        return service.testRedis();
//    }

}
