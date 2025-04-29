package com.quartztop.bot.tg_bot.telegram;


import com.quartztop.bot.tg_bot.config.BotConfig;
import com.quartztop.bot.tg_bot.entity.ActionClick;
import com.quartztop.bot.tg_bot.entity.BotUser;
import com.quartztop.bot.tg_bot.entity.BotUserStatus;
import com.quartztop.bot.tg_bot.entity.ClickType;
import com.quartztop.bot.tg_bot.integration.ActionClient;
import com.quartztop.bot.tg_bot.integration.StockClient;
import com.quartztop.bot.tg_bot.repositories.BotUserRepositories;
import com.quartztop.bot.tg_bot.responses.telegramResponses.NextActionResult;
import com.quartztop.bot.tg_bot.services.crud.ActionClickService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



@Slf4j
@Component
public class TelegramBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer{

    private final TelegramClient telegramClient;
    private final BotConfig botConfig;
    private final StockClient stockClient;
    private final ActionClient actionClient;
    private final CallbackHandler callbackHandler;
    private final BotMessageUtils botMessageUtils;
    private final RegistrationHandler registrationHandler;
    private final BotUserRepositories botUserRepositories;
    private final ActionClickService actionClickService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public TelegramBot(BotConfig botConfig, StockClient stockClient, ActionClient actionClient, CallbackHandler callbackHandler, BotMessageUtils botMessageUtils, RegistrationHandler registrationHandler, BotUserRepositories botUserRepositories, ActionClickService actionClickService) {
        this.botConfig = botConfig;
        telegramClient = new OkHttpTelegramClient(botConfig.getToken());
        this.stockClient = stockClient;
        this.actionClient = actionClient;
        this.callbackHandler = callbackHandler;
        this.botMessageUtils = botMessageUtils;
        this.registrationHandler = registrationHandler;
        this.botUserRepositories = botUserRepositories;
        this.actionClickService = actionClickService;
    }

    private final Map<Long, String> userState = new ConcurrentHashMap<>();

    @Override
    public void consume(Update update) {

        // Если пришел callback
        if (update.hasCallbackQuery()) {
            try {
                User tgUser = update.getCallbackQuery().getFrom();
                callbackHandler.handleCallback(update.getCallbackQuery(), userState, tgUser);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        Message message = update.getMessage();
        User tgUser = message.getFrom();
        long chatId = message.getChatId();

        String text = message.getText();

        // Нажата кнопка поделится контактом
        if (update.getMessage().hasContact()) {
            BotUser user = botUserRepositories.findByTelegramId(tgUser.getId()).orElse(null);
            try {
                registrationHandler.handleRegistrationSteps(user, message, userState);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        if (update.hasMessage() && update.getMessage().hasText()) {

            registrationHandler.registerUserIfNeeded(tgUser);
            BotUser user = botUserRepositories.findByTelegramId(tgUser.getId()).orElse(null);

            String state = userState.get(chatId);

            if (user != null && user.getStatus() == BotUserStatus.REGISTERED) {
                try {
                    registrationHandler.handleRegistrationSteps(user, message, userState);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return;
            }
            switch (text) {
                case "/start" -> {
                    send(BotMenuService.mainMenu(chatId));
                    userState.put(chatId, "MAIN_MENU");
                }
                case "📦 Проверить наличие" -> {
                    sendText(chatId, "🔎 Введи артикул или название товара для поиска:");
                    userState.put(chatId, "SEARCH_MODE"); // ставим состояние поиска
                }
                case "📷 Фото изделий" -> {
                    sendText(chatId, "🖼️ Вот фото изделий (заглушка)");
                    userState.put(chatId, "PHOTOS_MENU");
                }
                case "🎁 Акции" -> {
                    actionClickService.create(null, user, ClickType.ACTION_CLICK);
                    NextActionResult result = actionClient.getNextAction(null);
                    if (!result.isSuccess()) {
                        sendText(chatId, "⛔ Сейчас не могу получить информацию. Попробуй чуть позже.");
                        return;
                    }
                    if (!result.hasAction()) {
                        sendText(chatId, "🎉 Сейчас нет активных акций");
                        return;
                    }
                    try {
                        botMessageUtils.sendPreviewAction(chatId,result.getAction());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    userState.put(chatId, "ACTIONS_MODE"); // ставим состояние
                }
                case "💬 Задать вопрос " -> {
                    sendText(chatId, "✍️ Напиши свой вопрос, (Заглушка)");
                    userState.put(chatId, "TEXT_MODE");
                }
                default -> {
                    state = userState.getOrDefault(chatId, "MAIN_MENU");

                    switch (state) {
                        case "SEARCH_MODE" -> {
                            // здесь ты можешь вызывать свой StockClient и искать по артикулу
                            sendText(chatId, "🔍 Ищу товар: " + text);
                            String responseString = stockClient.getStockBySearch(text);

                            if (responseString.length() < 4096) {
                                sendText(chatId,responseString);
                            } else {
                                log.warn("SIZE TEXT = " + responseString.length());
                                sendText(chatId, "ОЙ ... Слишком большой ответ - Телеграм не пропускает. Попробуй уточнить запрос!");
                            }
                            sendText(chatId,"Что еще поищем? 🤖");
                            send(BotMenuService.mainMenu(chatId));
                        }
                        case "CHATGPT_MODE" -> {
                            // Заглушка для ChatGPT — можно прикрутить OpenAI API тут
                            sendText(chatId, "🤖 Ответ : (заглушка)");
                            send(BotMenuService.mainMenu(chatId));
                            userState.put(chatId, "MAIN_MENU");
                        }
                        case "ACTIONS_MODE" -> {
                        }
                        default -> {
                            sendText(chatId, "🤷 Я тебя не понял. Вот главное меню:");
                            send(BotMenuService.mainMenu(chatId));
                            userState.put(chatId, "MAIN_MENU");
                        }
                    }
                }
            }
            log.error("PRINT STATE " + state + "  --  " + userState.get(chatId) + " USER STATUS " + user.getStatus());
        }
    }


    private void sendText(long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode("HTML")
                .build();
        send(message);
    }

    private void send(SendMessage message) {
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения" + e.getMessage());
        }
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }
}
