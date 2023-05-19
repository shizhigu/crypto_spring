package com.example.CryptoSpring.service;

import com.example.CryptoSpring.model.BinanceKline;
import com.example.CryptoSpring.repository.BinanceklineMyBatisRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class KlineLoadService {

    @Autowired
    private BinanceklineMyBatisRepository repository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${KlineApi}")
    private String url;

    public List<BinanceKline> returnListOfKline(String[][] klinesBody, String symbol) {
        List<BinanceKline> klinesList = new ArrayList<>();
        for (String[] myParams : klinesBody) {
            Long openTime = Long.parseLong(myParams[0]);
            Double openPrice = Double.parseDouble(myParams[1]);
            Double highPrice = Double.parseDouble(myParams[2]);
            Double lowPrice = Double.parseDouble(myParams[3]);
            Double closePrice = Double.parseDouble(myParams[4]);
            Double volume = Double.parseDouble(myParams[5]);
            Long endTime = Long.parseLong(myParams[6]);
            Double quoteAssetVolume = Double.parseDouble(myParams[7]);
            Long numTrades = Long.parseLong(myParams[8]);
            Double baseAssetVolume = Double.parseDouble(myParams[9]);

            klinesList.add(new BinanceKline(symbol, openTime, endTime, openPrice, highPrice, lowPrice, closePrice, volume, numTrades, quoteAssetVolume, baseAssetVolume));
        }
        return klinesList;
    }

    public int insertKline(@RequestParam(value = "symbol") String symbol,
                           @RequestParam(value = "startTime") Long startTime,
                           @RequestParam(value = "endTime") Long endTime) {

        int period = 1000 * 1000 * 60;
        endTime--;
        int num = 0;
        while (startTime < endTime) {
            String apiUrl = String.format(url, symbol, startTime, Math.min(startTime + period, endTime));
            ResponseEntity<String[][]> response = restTemplate.getForEntity(apiUrl, String[][].class);
            startTime += period;
            List<BinanceKline> klineList = returnListOfKline(response.getBody(), symbol);
            repository.insertBatch(klineList);
            num += klineList.size();
        }
        return num;
    }
}
