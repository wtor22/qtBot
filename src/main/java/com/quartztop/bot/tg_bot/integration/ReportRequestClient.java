package com.quartztop.bot.tg_bot.integration;

import com.quartztop.bot.tg_bot.config.BotConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReportRequestClient {

    private final RestTemplate restTemplate;
    private final BotConfig botConfig;

    public byte[] uploadStockReport() {

        String url = botConfig.getAppUrl() + "/sales-report/stock/download";

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                byte[].class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        } else {
            throw new RestClientException("Ошибка при получении Excel файла: " + response.getStatusCode());
        }
    }

    /**
     * type - Это тип Поставщиков
     * general - все основные поставщики кроме InterStone
     * inter_stone - Только InterStone
     */

    public byte[] uploadGeneralReport(int year, String type) {

        String url = botConfig.getAppUrl() + "/sales-report/download?year=" + year + "&type=" + type;
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                byte[].class,
                year
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        } else {
            throw new RestClientException("Ошибка при получении Excel файла: " + response.getStatusCode());
        }
    }

    public byte[]  uploadRatingReport(int year, String type) {

        String url = botConfig.getAppUrl() + "/sales-report/rating/download?year=" + year + "&type=" + type;
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                byte[].class,
                year
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        } else {
            throw new RestClientException("Ошибка при получении Excel файла: " + response.getStatusCode());
        }

    }

}
