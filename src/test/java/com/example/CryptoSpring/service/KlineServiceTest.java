package com.example.CryptoSpring.service;

import com.example.CryptoSpring.model.BinanceKline;
import com.example.CryptoSpring.repository.BinanceklineMyBatisRepository;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class KlineServiceTest {

    @InjectMocks
    private KlineLoadService service;

    @InjectMocks
    private KlineSearchService searchService;

    @Spy
    private BinanceklineMyBatisRepository repository;

    @Spy
    private RestTemplate restTemplate;

    @Autowired
    private BinanceklineMyBatisRepository repositorySpy;

    @Autowired
    private RedisService redisSpy;

    @Value("${KlineApi}")
    private String url;

    @Test
    public void test_insert_kline(){
        when(repository.insertBatch(Mockito.any())).thenReturn(0);
        ReflectionTestUtils.setField(service, "url", url);

        assertEquals(1440, service.insertKline("BTCUSD", 1664607600000L,
                1664694000000L));
        assertEquals(0, service.insertKline("BTCUSD", 1664694000000L,
                1664694000000L));
    }



    @Test
    public void test_merge_klines() {
        ReflectionTestUtils.setField(searchService, "redisService", redisSpy);

        List<BinanceKline> klineList = new ArrayList<>();
        assertEquals(0, searchService.mergeKLines(klineList, 5).size());

        // divisible by commonly used timeframes; 120 minutes of data
        for (int i = 0; i < 120; i++) {
            klineList.add(mock(BinanceKline.class));
        }
        assertEquals(24, searchService.mergeKLines(klineList, 5).size());
        assertEquals(8, searchService.mergeKLines(klineList, 15).size());
        assertEquals(2, searchService.mergeKLines(klineList, 60).size());

        // not divisible, 121 minutes
        klineList.add(mock(BinanceKline.class));
        assertEquals(25, searchService.mergeKLines(klineList, 5).size());
        assertEquals(9, searchService.mergeKLines(klineList, 15).size());
        assertEquals(3, searchService.mergeKLines(klineList, 60).size());
    }


    @Test
    public void test_return_list_of_kline() {
        // common case
        String apiUrl = String.format(url, "BTCUSD", 1664607600000L, 1664694000000L);
        ResponseEntity<String[][]> response = restTemplate.getForEntity(apiUrl, String[][].class);
        assertEquals(1000, service.returnListOfKline(response.getBody(), "BTCUSD").size());

        // less than 1000 minutes (300 klines)
        String apiUrl1 = String.format(url, "BTCUSD", 1682924406000L, 1682942400000L);
        ResponseEntity<String[][]> response1 = restTemplate.getForEntity(apiUrl1, String[][].class);
        assertNotEquals(1000, service.returnListOfKline(response1.getBody(), "BTCUSD").size());
        assertEquals(300, service.returnListOfKline(response1.getBody(), "BTCUSD").size());

        // exactly 1000 minutes
        String apiUrl2 = String.format(url, "BTCUSD", 1682924400000L, 1682984400000L);
        ResponseEntity<String[][]> response2 = restTemplate.getForEntity(apiUrl2, String[][].class);
        assertEquals(1000, service.returnListOfKline(response2.getBody(), "BTCUSD").size());

    }

    @Test
    public void test_get_klines_from_time_range() {
        ReflectionTestUtils.setField(searchService, "repository", repositorySpy);
        ReflectionTestUtils.setField(searchService, "redisService", redisSpy);

        // get 1-day data (1440 minutes) with commonly used frequencies
        assertEquals(288, searchService.getKlinesFromTimeRange("BTCUSD", 1664607600000L,
                1664694000000L, 5).size());
        assertEquals(96, searchService.getKlinesFromTimeRange("BTCUSD", 1664607600000L,
                1664694000000L, 15).size());
        assertEquals(24, searchService.getKlinesFromTimeRange("BTCUSD", 1664607600000L,
                1664694000000L, 60).size());

    }
}
