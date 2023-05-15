package com.example.CryptoSpring.repository;

import com.example.CryptoSpring.model.BinanceKline;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BinanceklineMyBatisRepository {
    @Select("select * from binance_kline")
    public List<BinanceKline> findAll();

    @Select("select * from binance_kline where symbol=#{symbol} and open_time between #{startTime} and #{endTime}")
    public List<BinanceKline> getSymbolKlineByTimeRange(String symbol, Long startTime, Long endTime);


    @Insert("INSERT INTO binance_kline  (symbol, open_time, end_time, open_price, high_price, low_price, close_price, volume, num_trades, quote_asset_volume, base_asset_volume)" +
            " VALUES (#{symbol}, #{openTime}, #{endTime}, #{openPrice}, #{highPrice}, #{lowPrice}, #{closePrice}, #{volume}, #{numTrades}, #{quoteAssetVolume}, #{baseAssetVolume})")
    public int insert(BinanceKline bkInserted);

    @Insert({
            "<script>",
            "insert into binance_kline (symbol, open_time, end_time, open_price, high_price, low_price, close_price, volume, num_trades, quote_asset_volume, base_asset_volume)",
            "values ",
            "<foreach  collection='klinesList' item='kline' separator=','>",
            "( #{kline.symbol}, #{kline.openTime}, #{kline.endTime}, #{kline.openPrice}, #{kline.highPrice}, #{kline.lowPrice}, #{kline.closePrice}, #{kline.volume}, #{kline.numTrades}, #{kline.quoteAssetVolume}, #{kline.baseAssetVolume} )",
            "</foreach>",
            "</script>"
    })
    int insertBatch(@Param("klinesList") List<BinanceKline> klinesList);
}
