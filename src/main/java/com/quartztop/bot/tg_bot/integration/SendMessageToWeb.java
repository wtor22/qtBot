package com.quartztop.bot.tg_bot.integration;

import com.quartztop.bot.tg_bot.config.BotConfig;
import com.quartztop.bot.tg_bot.dto.TelegramMessageDto;
import com.quartztop.bot.tg_bot.repositories.SearchRequestRepository;
import com.quartztop.bot.tg_bot.services.crud.SearchRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SendMessageToWeb {

    private final RestTemplate restTemplate;
    private final BotConfig botConfig;
    private final SearchRequestRepository searchRequestRepository;

    public void sendMessage(TelegramMessageDto telegramMessageDto) {
        String url = botConfig.getAppUrl() + "/message";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TelegramMessageDto> request = new HttpEntity<>(telegramMessageDto, headers);

        restTemplate.postForEntity(url, request, Void.class);
    }

    public void sendStockRequest(TelegramMessageDto telegramMessageDto) {
        String url = botConfig.getAppUrl() + "/request";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TelegramMessageDto> request = new HttpEntity<>(telegramMessageDto, headers);

        log.error("Send Message to " + url + "\n" + telegramMessageDto.getText() + " FROM " + telegramMessageDto.getUsername());
        restTemplate.postForEntity(url, request, Void.class);
    }

    public void sendListStockRequest() {
        List<String> listRequest = searchRequestRepository.findTextsByTimeRequestBetween(LocalDate.now().atStartOfDay(), LocalDate.now().plusDays(1).atStartOfDay());
        String url = botConfig.getAppUrl() + "/request";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<List<String>> request = new HttpEntity<>(listRequest, headers);
        restTemplate.postForEntity(url, request, Void.class);
    }
}
