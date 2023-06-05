package com.example.CryptoSpring.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
}
