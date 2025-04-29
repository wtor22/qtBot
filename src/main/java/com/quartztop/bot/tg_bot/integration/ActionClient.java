package com.quartztop.bot.tg_bot.integration;

import com.quartztop.bot.tg_bot.config.BotConfig;
import com.quartztop.bot.tg_bot.responses.telegramResponses.NextActionResult;
import com.quartztop.bot.tg_bot.responses.telegramResponses.TelegramActionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class ActionClient {

    private final RestTemplate restTemplate;
    private final BotConfig botConfig;
    public TelegramActionDto getActionById(long actionId) {
        String url = botConfig.getAppUrl() + "/actions/" + actionId;

        ResponseEntity<TelegramActionDto> response = restTemplate.getForEntity(url, TelegramActionDto.class);
        return Objects.requireNonNull(response.getBody());
    }

    public NextActionResult getNextAction(Long currentId) {
        String url = botConfig.getAppUrl() + "/actions/next";
        if (currentId != null) {
            url += "?currentId=" + currentId;
        }
        try {
            ResponseEntity<TelegramActionDto> response =
                    restTemplate.getForEntity(url, TelegramActionDto.class);
            return new NextActionResult(true, response.getBody());

        } catch (ResourceAccessException e) {
            log.warn("⚠️ API недоступен: {}", e.getMessage());
        } catch (HttpClientErrorException e) {
            log.warn("❌ Ошибка от API: {}", e.getStatusCode());
        } catch (Exception e) {
            log.error("🔥 Неизвестная ошибка при получении nextAction", e);
        }
        return new NextActionResult(false, null); // не получилось достучаться
    }
}
