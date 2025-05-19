package com.quartztop.bot.tg_bot.telegram;


import com.quartztop.bot.tg_bot.config.BotConfig;
import com.quartztop.bot.tg_bot.entity.activity.TicketMessage;
import com.quartztop.bot.tg_bot.entity.botUsers.BotUser;
import com.quartztop.bot.tg_bot.entity.botUsers.BotUserStatus;
import com.quartztop.bot.tg_bot.entity.activity.ClickType;
import com.quartztop.bot.tg_bot.integration.ActionClient;
import com.quartztop.bot.tg_bot.integration.StockClient;
import com.quartztop.bot.tg_bot.repositories.BotUserRepositories;
import com.quartztop.bot.tg_bot.responses.telegramResponses.NextActionResult;
import com.quartztop.bot.tg_bot.services.crud.ActionClickService;
import com.quartztop.bot.tg_bot.services.crud.SearchRequestService;
import com.quartztop.bot.tg_bot.services.crud.TicketMessageService;
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
    private final SearchRequestService searchRequestService;
    private final BotMenuService botMenuService;
    private final TicketMessageService ticketMessageService;
    private final TicketSessionService ticketSessionService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public TelegramBot(BotConfig botConfig, StockClient stockClient, ActionClient actionClient, CallbackHandler callbackHandler, BotMessageUtils botMessageUtils, RegistrationHandler registrationHandler, BotUserRepositories botUserRepositories, ActionClickService actionClickService, SearchRequestService searchRequestService, BotMenuService botMenuService, TicketMessageService ticketMessageService, TicketSessionService ticketSessionService) {
        this.botConfig = botConfig;
        telegramClient = new OkHttpTelegramClient(botConfig.getToken());
        this.stockClient = stockClient;
        this.actionClient = actionClient;
        this.callbackHandler = callbackHandler;
        this.botMessageUtils = botMessageUtils;
        this.registrationHandler = registrationHandler;
        this.botUserRepositories = botUserRepositories;
        this.actionClickService = actionClickService;
        this.searchRequestService = searchRequestService;
        this.botMenuService = botMenuService;
        this.ticketMessageService = ticketMessageService;
        this.ticketSessionService = ticketSessionService;
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
                    actionClickService.create(null, user, ClickType.GET_PHOTO);
                    send(botMenuService.linkImageMenu(chatId));
                    userState.put(chatId, "PHOTOS_MENU");
                }
                case "🎁 Акции" -> {
                    actionClickService.create(null, user, ClickType.ACTION_CLICK);
                    NextActionResult result = actionClient.getNextAction(null, user);
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
                case "❓Задать вопрос" -> {
                    sendText(chatId, "✍️ Напиши свой вопрос");
                    userState.put(chatId, "TEXT_MODE");
                }
                default -> {
                    state = userState.getOrDefault(chatId, "MAIN_MENU");

                    switch (state) {
                        case "SEARCH_MODE" -> {
                            // здесь ты можешь вызывать свой StockClient и искать по артикулу
                            sendText(chatId, "🔍 Ищу товар: " + text);
                            String responseString = stockClient.getStockBySearch(text);
                            searchRequestService.create(text,user);

                            if (responseString.length() < 4096) {
                                sendText(chatId,responseString);
                            } else {
                                sendText(chatId, "ОЙ ... Слишком большой ответ - Телеграм не пропускает. Попробуй уточнить запрос!");
                            }
                            sendText(chatId,"Что еще поищем? 🤖");
                            send(BotMenuService.mainMenu(chatId));
                        }
                        case "TEXT_MODE" -> {
                            // Заглушка для ChatGPT — можно прикрутить OpenAI API тут
                            sendText(chatId, "✅ Спасибо, вопрос получен!\n" +
                                    "\n" +
                                    "я уже передал его администратору. Обычно он отвечает в течение часа.\n" +
                                    "\n" +
                                    "⏳ Пожалуйста, ожидай — тебе придёт уведомление, как только ответ появится.");

                            TicketMessage ticket = ticketMessageService.addQuestion(user, text);
                            send(BotMenuService.mainMenu(chatId));
                            actionClickService.create(ticket.getId(), user, ClickType.CREATE_QUESTION);
                            botMessageUtils.sendAdminQuestionNotification(user,ticket);
                            userState.put(chatId, "MAIN_MENU");
                        }
                        case "AWAITING_ANSWER_TICKET" -> {

                            String ticketNumber = ticketSessionService.getTicketId(chatId);
                            ticketMessageService.addAnswer(ticketNumber,user,text);
                            sendText(chatId, "Ответ отправлен.");
                            TicketMessage startTicketMessage = ticketMessageService.getFirstMessageTicketByTicketNumber(ticketNumber);
                            String answer = "\uD83D\uDE4C Привет снова! \n\nЯ уже подготовил ответ на твой вопрос:\n\n\uD83D\uDCE8 Вопрос: \n" +
                                    startTicketMessage.getText() + "\n\n\uD83D\uDC69\u200D\uD83D\uDCBC Ответ от нашего администратора: \n" +
                                    text + "\n\n\uD83D\uDE42 Если ещё что-то нужно — пиши, я всегда на связи!";

                            sendText(startTicketMessage.getBotUser().getTelegramId(),answer);

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
