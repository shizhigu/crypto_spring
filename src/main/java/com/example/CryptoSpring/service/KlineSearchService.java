package com.example.CryptoSpring.service;

import com.example.CryptoSpring.model.BinanceKline;
import com.example.CryptoSpring.repository.BinanceklineMyBatisRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class KlineSearchService {

    @Autowired
    private BinanceklineMyBatisRepository repository;

    @Autowired
    private RedisService redisService;

    public List<BinanceKline> getKlinesFromTimeRange(@NotBlank String symbol, @NotNull Long startTime,
                                                     @NotNull Long endTime, @NotNull Integer frequency) {
        // get from cache
        List<BinanceKline> listKlines = redisService.getListKlines(symbol, frequency, startTime, endTime);
        if (!listKlines.isEmpty()) {
            return listKlines;
        }
        // get
        List<BinanceKline> kLinesList = repository.getSymbolKlineByTimeRange(symbol, startTime, endTime - 1);
        List<BinanceKline> mergedKlinesList = this.mergeKLines(kLinesList, frequency);
        // put into cache
        redisService.put(symbol, frequency, mergedKlinesList);
        return mergedKlinesList;
    }

    public List<BinanceKline> mergeKLines(List<BinanceKline> klinesList, Integer frequency) {
        if (klinesList.size()==0) { List.of(); }
        return IntStream.range(0, klinesList.size()/frequency + 1)
                .mapToObj(i -> klinesList.subList(i*frequency, Math.min(i*frequency + frequency, klinesList.size())))
                //.parallel()
                .map(this::merge)
                .collect(Collectors.toList());
    }

    protected BinanceKline merge(List<BinanceKline> klinesList) {
        BinanceKline firstKline = klinesList.get(0);
        BinanceKline lastKline = klinesList.get(klinesList.size() - 1);

        BinanceKline kLine = new BinanceKline();
        kLine.setSymbol(firstKline.getSymbol());
        kLine.setOpenPrice(firstKline.getOpenPrice());
        kLine.setOpenTime(firstKline.getOpenTime());
        kLine.setClosePrice(lastKline.getClosePrice());
        kLine.setEndTime(lastKline.getEndTime());

        Double volume = 0.0;
        Double baseAssetVolume = 0.0;
        Double quoteAssetVolume = 0.0;
        Long numTrades = 0L;
        Double high = Double.MIN_VALUE;
        Double low = Double.MAX_VALUE;

        for (int i = 0; i < klinesList.size(); i++) {
            BinanceKline currentKLine = klinesList.get(i);
            volume += currentKLine.getVolume();
            baseAssetVolume += currentKLine.getBaseAssetVolume();
            quoteAssetVolume += currentKLine.getQuoteAssetVolume();
            numTrades += currentKLine.getNumTrades();
            high = Math.max(high, currentKLine.getHighPrice());
            low = Math.min(low, currentKLine.getLowPrice());
        }
        kLine.setVolume(volume);
        kLine.setBaseAssetVolume(baseAssetVolume);
        kLine.setQuoteAssetVolume(quoteAssetVolume);
        kLine.setNumTrades(numTrades);
        return kLine;
    }
}
