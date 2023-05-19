package com.example.CryptoSpring.service;

import com.example.CryptoSpring.model.BinanceKline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public String getKey(String symbol, Integer frequency){
        return symbol+'-'+frequency.toString();
    }


    public List<BinanceKline> getListKlines(String symbol, Integer frequency, Long startTime, Long endTime){
        String key = this.getKey(symbol, frequency);
        if (redisTemplate.hasKey(key)) {
            Set<Object> klinesSet = redisTemplate.opsForZSet().rangeByScore(key, startTime, endTime-1);
            if (klinesSet.size() >= (endTime-startTime)/1000/60/frequency*0.95) {
                return klinesSet.stream()
                        .map(obj -> (BinanceKline) obj)
                        .collect(Collectors.toList());
            }
        }
        return List.of();
    }

    public void put(String symbol, Integer frequency, List<BinanceKline> klines){
        String key = getKey(symbol, frequency);
        klines.stream()
                .forEach( i -> redisTemplate.opsForZSet().add(key, i, i.getOpenTime()));
    }
}
