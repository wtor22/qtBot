package com.quartztop.bot.tg_bot.services;

import com.quartztop.bot.tg_bot.config.BotConfig;
import com.quartztop.bot.tg_bot.dto.StockByCategoryResponse;
import com.quartztop.bot.tg_bot.dto.StockByProductResponse;
import com.quartztop.bot.tg_bot.dto.StockByStoreResponse;
import com.quartztop.bot.tg_bot.dto.TelegramActionDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class StockClient {

    private final RestTemplate restTemplate;
    private final BotConfig botConfig;

    private static final String FREE_SPACE = "        ";

    public StockClient(RestTemplate restTemplate, BotConfig botConfig) {
        this.restTemplate = restTemplate;
        this.botConfig = botConfig;
    }

    public List<StockByCategoryResponse> getStock(String search) {


        String url = botConfig.getAppUrl() + "/stock/search?search=" + UriUtils.encode(search, StandardCharsets.UTF_8);
        try {
            ResponseEntity<StockByCategoryResponse[]> response = restTemplate.getForEntity(url, StockByCategoryResponse[].class);
            return Arrays.asList(Objects.requireNonNull(response.getBody()));
        } catch (ResourceAccessException e) {
            log.error("❌ Не удалось подключиться к API остатков: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            log.error("🔥 Ошибка при получении остатков", e);
            return null;
        }
    }

    public List<TelegramActionDto> getActions() {
        String url = botConfig.getAppUrl() + "/actions";
        ResponseEntity<TelegramActionDto[]> response = restTemplate.getForEntity(url, TelegramActionDto[].class);
        return Arrays.asList(Objects.requireNonNull(response.getBody()));
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

    public TelegramActionDto getActionById(long actionId) {
        String url = botConfig.getAppUrl() + "/actions/" + actionId;

        log.error("PRINT URL " + url);

        ResponseEntity<TelegramActionDto> response = restTemplate.getForEntity(url, TelegramActionDto.class);
        return Objects.requireNonNull(response.getBody());

    }

    public String getStockBySearch(String search) {
        List<StockByCategoryResponse> stockList = getStock(search);
        if (stockList == null) {
            return "🚫 Ошибка подключения. Попробуйте позже.";
        }
        if (stockList.isEmpty()) {
            return "❌ Ничего не найдено по запросу: *" + search + "*";
        }
        StringBuilder response = new StringBuilder();
        for (StockByCategoryResponse categoryResponse : stockList) {
            String category = categoryResponse.getCategory();
            List<StockByProductResponse> products = categoryResponse.getProductsList();

            response.append("📦 *Категория:* ").append(category).append("\n");

            for (StockByProductResponse product : products) {
                response
                        .append("\n")
                        .append("\uD83D\uDD38  <b>").append(product.getProductName()).append("</b>")
                        .append("\n").append(FREE_SPACE).append("Арт. ").append(product.getArticle()).append("\n")
                        .append(FREE_SPACE).append(product.getThicknessProduct()).append(" мм. ")
                        .append("(").append(product.getSortProduct()).append(" сорт)\n")
                        .append(FREE_SPACE).append(product.getFormatProduct()).append(" ").append(product.getSurfaceProduct())
                        .append("\n").append(FREE_SPACE).append("Размеры: ").append(product.getSizeProduct());
                if(product.getRecommendedPrice() != null && !product.getRecommendedPrice().equals("0")) {
                    response.append("\n").append(FREE_SPACE).append("РРЦ: ").append(product.getRecommendedPrice());
                }

                response.append("\n\n").append(FREE_SPACE).append("Наличие на складах:").append("\n");

                List<StockByStoreResponse> stores = product.getByStoreResponseList();
                for (StockByStoreResponse store : stores) {
                    response.append("\n").append(FREE_SPACE).append("<u>").append(store.getNameStore()).append("</u> ");
                    if (store.getStock() > 0) {
                        response.append(": <b>").append(store.getStock()).append("</b>\n");
                    } else  {
                        response.append("<b>: 0.0</b>\n");
                    }
                    if(store.getReserve() > 0) response.append(FREE_SPACE).append("Забронировано: <b>").append(store.getReserve()).append("</b>\n");
                    if (store.getInTransit() > 0) response.append(FREE_SPACE).append("Ожидается: <b>").append(store.getInTransit()).append("</b>\n");

                    float countAvailable = store.getStock() + store.getInTransit() - store.getReserve();
                    if (countAvailable < 0) countAvailable = 0;

                    response.append(FREE_SPACE).append("Доступно к заказу: <b>").append(countAvailable).append("</b>\n\n");
                }
            }
            response.append("━━━━━━━━━━━━━━━━━━━━━\n\n"); // Разделитель категорий
        }
        return response.toString();
    }
}
