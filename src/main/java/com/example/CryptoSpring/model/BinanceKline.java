package com.example.CryptoSpring.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class BinanceKline implements Serializable {
    private String symbol;
    private Long openTime;
    private Long endTime;
    private Double openPrice;
    private Double highPrice;
    private Double lowPrice;
    private Double closePrice;
    private Double volume;
    private Long  numTrades;
    private Double quoteAssetVolume;
    private Double baseAssetVolume;

    public BinanceKline(String symbol, Long openTime, Long endTime, Double openPrice, Double highPrice, Double lowPrice, Double closePrice, Double volume, Long numTrades, Double quoteAssetVolume, Double baseAssetVolume) {
        this.symbol = symbol;
        this.openTime = openTime;
        this.endTime = endTime;
        this.openPrice = openPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.closePrice = closePrice;
        this.volume = volume;
        this.numTrades = numTrades;
        this.quoteAssetVolume = quoteAssetVolume;
        this.baseAssetVolume = baseAssetVolume;
    }

    public BinanceKline() {

    }
}
