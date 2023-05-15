package com.example.CryptoSpring.service;

import com.example.CryptoSpring.configuration.RedisConfig;
import com.example.CryptoSpring.model.BinanceKline;
import com.example.CryptoSpring.repository.BinanceklineMyBatisRepository;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class KlineService {

    @Autowired
    private BinanceklineMyBatisRepository repository;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${KlineApi}")
    private String url;


    //@NotBlank
    private List<BinanceKline> writeIntoRedis(String symbol, Long startTime, Long endTime, Integer frequency) {
        List<BinanceKline> kLinesList = repository.getSymbolKlineByTimeRange(symbol, startTime, endTime);
        List<BinanceKline> mergedKlinesList = this.mergeKLines(kLinesList, frequency);
        return mergedKlinesList;
    }


    public List<BinanceKline> getKlinesFromTimeRange(String symbol, Long startTime, Long endTime, Integer frequency) {
        String key = symbol+'-'+frequency.toString();
        if (redisTemplate.hasKey(key)) {
            Set<Object> klinesSet = redisTemplate.opsForZSet().rangeByScore(key, startTime, endTime-1);
            return klinesSet.stream()
                    .map(obj -> (BinanceKline) obj)
                    .collect(Collectors.toList());
        }
        List<BinanceKline> kLinesList = repository.getSymbolKlineByTimeRange(symbol, startTime, endTime);
        List<BinanceKline> mergedKlinesList = this.mergeKLines(kLinesList, frequency);
        return mergedKlinesList;


    }
//    public List<BinanceKline> getKlinesFromTimeRange(String symbol, Long startTime, Long endTime, Integer frequency) {
//        List<BinanceKline> preMergedList = new ArrayList<>();
//        List<BinanceKline> mergedKlinesList;
//        List<BinanceKline> postMergedList;
//        String key = symbol+'-'+frequency.toString();
//        Long currTime = startTime;
//        if (redisTemplate.hasKey(key)) {
//            ZSetOperations.TypedTuple minScoreObj = redisTemplate.opsForZSet().popMin(key);
//            double minScr = minScoreObj.getScore();
//            Object minObj = minScoreObj.getValue();
//            Long minScore = (long) minScr;
//            redisTemplate.opsForZSet().add(key, minObj, minScore);
//            if (minScore > startTime+frequency*60*1000) {
//                preMergedList = writeIntoRedis(symbol, startTime, (Long) minScore-1, frequency);
//            }
//
//            Set<Object> klinesSet = redisTemplate.opsForZSet().
//                    rangeByScore(key, minScore, endTime-1);
//
//            mergedKlinesList = klinesSet.stream()
//                    .map(obj -> (BinanceKline) obj)
//                    .collect(Collectors.toList());
//            currTime += frequency*60*1000*mergedKlinesList.size();
//
////            mergedKlinesList = Stream.concat(preMergedList.stream(), mergedKlinesList.stream()).toList();
//            if (preMergedList.isEmpty()) {
//                preMergedList = mergedKlinesList;
//            } else {
//                preMergedList.addAll(mergedKlinesList);
//            }
//            if (klinesSet.size() >= (endTime-startTime)/1000/60/frequency) {
//                return preMergedList;
//            }
//        }
//        postMergedList = writeIntoRedis(symbol, currTime, endTime-1, frequency);
//        // List<BinanceKline> entireMergedKlinesList = Stream.concat(mergedKlinesList.stream(), postMergedList.stream()).toList();
//        preMergedList.addAll(postMergedList);
//
//        System.out.println(preMergedList.size());
//
//        return preMergedList;
//    }

//    public List<BinanceKline> getKlinesFromTimeRange(String symbol, Long startTime, Long endTime, Integer frequency){
//        List<BinanceKline> mergedKlinesList = new ArrayList<>();
//        Long time = startTime;
//        while (time < endTime) {
////            String key = symbol + " - " + time + " - " + (time+frequency*60*1000-1);
//            String key = symbol+'-'+frequency.toString();
//            String hashKey = time+ " - " + (time+frequency*60*1000-1);
//            if (redisTemplate.hasKey(key)) {
//                mergedKlinesList.add(
//                        (BinanceKline) redisTemplate.opsForHash().get(key, hashKey)
//                );
//            }
//            time += frequency*60*1000;
//        }
//        if (mergedKlinesList.size() > 1) { return mergedKlinesList; }
//
//        List<BinanceKline> kLinesList = repository.getSymbolKlineByTimeRange(symbol, startTime, endTime);
//        System.out.println(kLinesList.size());
//
//        mergedKlinesList = this.mergeKLines(kLinesList, frequency);
//        System.out.println(mergedKlinesList.size());
//        for (BinanceKline bk : mergedKlinesList) {
//            String key = symbol + " - " + bk.getOpenTime().toString() + " - " + bk.getEndTime().toString();
//            if (!redisTemplate.hasKey(key)) { redisTemplate.opsForHash().put(symbol+'-'+frequency.toString(),
//                    bk.getOpenTime().toString() + " - " + bk.getEndTime().toString(), bk ); }
//
//        }
//
//        return mergedKlinesList;
//    }


    public List<BinanceKline> mergeKLines(List<BinanceKline> klinesList, Integer frequency) {
        int i = 0;
        List<BinanceKline> mergedKLines = new ArrayList<>();
        while (i < klinesList.size()) {
            BinanceKline firstKline = klinesList.get(i);
            BinanceKline kLine = new BinanceKline();
            kLine.setSymbol(firstKline.getSymbol());
            kLine.setOpenPrice(firstKline.getOpenPrice());
            kLine.setOpenTime(firstKline.getOpenTime());
            kLine.setClosePrice(klinesList.get(Math.min(klinesList.size()-1, i+frequency-1)).getClosePrice());
            kLine.setEndTime(klinesList.get(Math.min(klinesList.size()-1, i+frequency-1)).getEndTime());
            Double volume = 0.0;
            Double baseAssetVolume = 0.0;
            Double quoteAssetVolume = 0.0;
            Long numTrades = 0L;
            Double high = Double.MIN_VALUE;
            Double low = Double.MAX_VALUE;

            for (int j = 0; j < frequency; j++) {
                if (i+j >= klinesList.size()) { break; }
                BinanceKline currentKLine = klinesList.get(i + j);
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
            if (kLine.getEndTime()+1-kLine.getOpenTime() >= 30000) {
                mergedKLines.add(kLine);
                redisTemplate.opsForZSet().add(kLine.getSymbol()+'-'+frequency.toString(), kLine, kLine.getOpenTime());
            }
            i += frequency;
        }
        return mergedKLines;
    }

    public List<BinanceKline> returnListofKline(String[][] klinesBody, String symbol) {
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
            List<BinanceKline> klineList = returnListofKline(response.getBody(), symbol);
            repository.insertBatch(klineList);
            num += klineList.size();
        }
        return num;
    }
}
