package com.quartztop.bot.tg_bot;


import com.quartztop.bot.tg_bot.config.BotConfig;
import com.quartztop.bot.tg_bot.dto.TelegramActionDto;
import com.quartztop.bot.tg_bot.entity.BotUser;
import com.quartztop.bot.tg_bot.entity.UserStatus;
import com.quartztop.bot.tg_bot.repositories.BotUserRepositories;
import com.quartztop.bot.tg_bot.services.NextActionResult;
import com.quartztop.bot.tg_bot.services.StockClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;



@Slf4j
@Component
//@RequiredArgsConstructor
public class TelegramBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer{

    private final TelegramClient telegramClient;
    private final BotConfig botConfig;
    private final StockClient stockClient;
    private final BotUserRepositories botUserRepositories;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public TelegramBot(BotConfig botConfig, StockClient stockClient, BotUserRepositories botUserRepositories) {
        this.botConfig = botConfig;
        telegramClient = new OkHttpTelegramClient(botConfig.getToken());
        this.stockClient = stockClient;
        this.botUserRepositories = botUserRepositories;
    }

    private final Map<Long, String> userState = new ConcurrentHashMap<>();

    @Override
    public void consume(Update update) {
        // Если пришел callback
        if (update.hasCallbackQuery()) {
            try {
                handleCallback(update.getCallbackQuery());
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
            log.error("CONTACT GETTED");
            try {
                handleRegistrationSteps(user, message);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        if (update.hasMessage() && update.getMessage().hasText()) {

            registerUserIfNeeded(tgUser);
            BotUser user = botUserRepositories.findByTelegramId(tgUser.getId()).orElse(null);

            String state = userState.get(chatId);
            log.error("USER IS NULL? " + (user == null));

            if (user != null && user.getStatus() == UserStatus.REGISTERED) {
                try {
                    handleRegistrationSteps(user, message);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return;
            }
            log.error("PRINT STATE " + state + " USER STATUS " + user.getStatus());


            switch (text) {
                case "/start" -> {
                    sendMainMenu(chatId);
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
                    NextActionResult result = stockClient.getNextAction(null);
                    if (!result.isSuccess()) {
                        sendText(chatId, "⛔ Сейчас не могу получить информацию. Попробуй чуть позже.");
                        return;
                    }
                    if (!result.hasAction()) {
                        sendText(chatId, "🎉 Сейчас нет активных акций");
                        return;
                    }
                    TelegramActionDto firstAction = result.getAction();
                    try {
                        sendPreviewAction(chatId,firstAction);
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
                        }
                        case "CHATGPT_MODE" -> {
                            // Заглушка для ChatGPT — можно прикрутить OpenAI API тут
                            sendText(chatId, "🤖 Ответ : (заглушка)");
                            sendMainMenu(chatId);
                            userState.put(chatId, "MAIN_MENU");
                        }
                        case "ACTIONS_MODE" -> {
                        }
                        default -> {
                            sendText(chatId, "🤷 Я тебя не понял. Вот главное меню:");
                            sendMainMenu(chatId);
                            userState.put(chatId, "MAIN_MENU");
                        }
                    }
                }
            }
        }
    }

    private void sendWelcomeMessage(Long chatId) {
        String welcomeText = """
            <b>👋 Добро пожаловать!</b>
            Я — бот компании <b>Кварцтоп</b> 🧱
            
            Здесь ты найдёшь актуальные остатки, цены и новинки 📦
            
            Прежде чем мы начнём — нужно пройти короткую регистрацию. Буквально пару кликов 😉
            """;

        InlineKeyboardButton startButton = InlineKeyboardButton.builder()
                .text("🚀 Начать регистрацию")
                .callbackData("start_registration")
                .build();

        InlineKeyboardRow row = new InlineKeyboardRow();
        row.add(startButton);

        List<InlineKeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row);

        InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder()
                .keyboard(keyboard)
                .build();

        sendTextWithKeyboard(chatId, welcomeText, markup);
    }

    private void handleRegistrationSteps(BotUser user, Message message) throws InterruptedException {
        long chatId = message.getChatId();
        String state = userState.get(chatId);

        if (state == null) {
            sendWelcomeMessage(chatId);
            userState.put(chatId, "START_REGISTRATION");
            return;
        }
        switch (state) {
            case "AWAITING_PHONE" -> {
                if (message.hasContact()) {
                    log.error("MESSAGE HAVE CONTACT");
                    String phone = message.getContact().getPhoneNumber();
                    log.error("PRINT PHONE  " + phone);
                    user.setPhoneNumber(phone);
                    log.error("USER PHONE GETTED " + phone);
                    botUserRepositories.save(user);

                    log.error("USER PHONESAVED");
                    sendText(chatId, "✅ Телефон сохранён!");
                    userState.put(chatId, "AWAITING_NAME");
                    sendText(chatId, "Теперь введи своё имя");
                } else {
                    sendPhoneRequestKeyboard(chatId);
                }
            }

            case "AWAITING_NAME" -> {
                if (message.hasText()) {
                    user.setFirstName(message.getText());
                    botUserRepositories.save(user);
                    userState.put(chatId, "AWAITING_LASTNAME");
                    sendText(chatId, "Отлично " + user.getFirstName() + " полдела сделано! \nТеперь введи свою фамилию");
                }
            }
            case "AWAITING_LASTNAME" -> {
                if (message.hasText()) {
                    user.setLastName(message.getText());
                    user.setStatus(UserStatus.ACTIVE);
                    botUserRepositories.save(user);

                    sendText(chatId, "\uD83C\uDF89 Ура! Регистрация завершена.\n" +
                            "\n" +
                            user.getFirstName() + " " + user.getLastName() +" ты теперь в нашей команде \uD83D\uDCAA\n" +
                            "Всё самое полезное уже под рукой:\n" +
                            "актуальные остатки, цены, медиаматериалы и новинки  \uD83E\uDDF1\n" +
                            "\n" +
                            "Если что-то понадобится — просто задай вопрос!");
                    userState.put(chatId, "MAIN_MENU");
                    sendMainMenu(chatId);
                }
            }
        }
    }

    private void sendPhoneRequestKeyboard(Long chatId) {

        String messageText = """
        📞 Для начала отправь мне твой номер телефона 😉
        
        Чтобы продолжить, просто нажми на кнопку внизу ⬇️
        
        Не переживай, я просто хочу убедиться, что ты настоящий человек, а не такой как я — железяка с Wi-Fi 😅
        """;
        // Создаём кнопку с запросом контакта
        KeyboardButton requestContactButton = KeyboardButton.builder()
                .text("📲  Отправить номер телефона")
                .requestContact(true)
                .build();

        // Создаём строку с кнопкой
        KeyboardRow row = new KeyboardRow();
        row.add(requestContactButton);

        // Создаём клавиатуру
        ReplyKeyboardMarkup keyboardMarkup = ReplyKeyboardMarkup.builder()
                .keyboard(List.of(row))
                .resizeKeyboard(true)
                .oneTimeKeyboard(true)
                .build();

        // Собираем сообщение
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(messageText)
                .replyMarkup(keyboardMarkup)
                .build();

        send(message);
    }

    private void registerUserIfNeeded(User tgUser) {
        Long telegramId = tgUser.getId();
        if (!botUserRepositories.existsByTelegramId(telegramId)) {
            BotUser user = new BotUser();
            user.setTelegramId(telegramId);
            user.setUsername(tgUser.getUserName());
            user.setFirstName(tgUser.getFirstName());
            user.setLastName(tgUser.getLastName());
            user.setRegisteredAt(LocalDateTime.now());
            user.setStatus(UserStatus.REGISTERED);
            botUserRepositories.save(user);
            log.info("Зарегистрирован новый пользователь: " + telegramId);
        }
    }

    private void handleCallback(CallbackQuery callbackQuery) throws InterruptedException {
        String data = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChatId();

        log.error("Print data " + data);

        if (data.startsWith("next_action_")) {
            String idStr = data.replace("next_action_", "");
            Long currentId = Long.parseLong(idStr);
            NextActionResult result = stockClient.getNextAction(currentId);
            if (!result.isSuccess()) {
                sendText(chatId, "⛔ Сейчас не могу получить информацию. Попробуй чуть позже.");
                return;
            }
            if (!result.hasAction()) {
                sendText(chatId, "🎉 Больше нет активных акций");
                return;
            }
            TelegramActionDto action = result.getAction();
            sendPreviewAction(chatId,action);
        }
        if (data.startsWith("action_")) {
            String idStr = data.replace("action_", "");
            long currentId = Long.parseLong(idStr);
            TelegramActionDto actionDto = stockClient.getActionById(currentId);
            sendFullAction(chatId, actionDto);
        }
        if ("start_registration".equals(data)) {
            sendText(chatId, "📋  Отлично! Давай начнём регистрацию.");
            userState.put(chatId, "AWAITING_PHONE");
            sendPhoneRequestKeyboard(chatId);
        }
    }

    private void sendMainMenu(long chatId) {
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("📦 Проверить наличие"));
        row1.add(new KeyboardButton("📷 Фото изделий"));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("🎁 Акции"));
        row2.add(new KeyboardButton("💬 Задать вопрос "));

        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row1);
        keyboard.add(row2);

        ReplyKeyboardMarkup markup = ReplyKeyboardMarkup.builder()
                .keyboard(keyboard)
                .resizeKeyboard(true)
                .oneTimeKeyboard(false)
                .build();
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Главное меню в самом низу \uD83D\uDC47")
                .replyMarkup(markup)
                .build();
        send(message);
    }
    private void sendText(long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode("HTML")
                .build();
        send(message);
    }
    public void sendTextWithKeyboard(Long chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(keyboard)
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

    // Метод отправки фото
    private void send(SendPhoto sendPhoto) {
        try {
            telegramClient.execute(sendPhoto);
        } catch (TelegramApiException e) {
            // Обработка ошибок, если что-то пошло не так
            log.error("Ошибка при отправке сообщения" + e.getMessage());
        }
    }

    private void sendFullAction(long chatId, TelegramActionDto actionDto) {
        //String detailsUrl = "action_" + actionDto.getId();
        String nextAction = "next_action_" + actionDto.getId();

        String messageText = "<b>" + actionDto.getName() + "</b>\n\n"
                + sanitizeHtmlForTelegram(actionDto.getContent());

        InlineKeyboardButton buttonNext = new InlineKeyboardButton("Следующая");
        buttonNext.setCallbackData(nextAction);

        InlineKeyboardRow row = new InlineKeyboardRow();
        row.add(buttonNext);
        List<InlineKeyboardRow> keyboard = new ArrayList<>(List.of(row));

        // создаём клавиатуру
        InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder()
                .keyboard(keyboard)
                .build();

        sendTextWithKeyboard(chatId, messageText, markup);

    }

    private void sendPreviewAction(long chatId, TelegramActionDto dto) throws InterruptedException {
        if (dto.getTitleImageUrl() == null ) {
            sendText(chatId, dto.getName());
            return;
        }
        String detailsUrl = "action_" + dto.getId();
        String nextAction = "next_action_" + dto.getId();
        String caption = "<b>" + dto.getName() + "</b>\n" + sanitizeHtmlForTelegram(dto.getDescription());

        String relativePath = dto.getTitleImageUrl().replaceFirst("/uploads/", ""); // убираем "/uploads/"
        String imagePath = uploadDir + relativePath;

        InlineKeyboardButton button = new InlineKeyboardButton("Подробнее");
        button.setCallbackData(detailsUrl);

        InlineKeyboardButton buttonNext = new InlineKeyboardButton("Следующая");
        buttonNext.setCallbackData(nextAction);

        InlineKeyboardRow row = new InlineKeyboardRow();
        row.add(button);
        row.add(buttonNext);
        List<InlineKeyboardRow> keyboard = new ArrayList<>(List.of(row));

        // создаём клавиатуру
        InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder()
                .keyboard(keyboard)
                .build();

        InputFile photoFile = new InputFile(new File(imagePath));

        // отправка фото с клавиатурой
        SendPhoto photo = SendPhoto.builder()
                .chatId(String.valueOf(chatId))
                .photo(photoFile)
                .caption(caption)
                .parseMode("HTML")
                .replyMarkup(markup)
                .build();

        send(photo);
        Thread.sleep(500);
    }

    public static String sanitizeHtmlForTelegram(String html) {

        if (html == null) return "";
        // Удаляем все <p>, <div> и т.п., заменяем на \n
        String cleaned = html.replaceAll("(?i)</?p[^>]*>", "\n");

        // Удаляем все data-атрибуты и стили
        cleaned = cleaned.replaceAll("(?i)\\s(data-[a-z\\-]+|style)=\"[^\"]*\"", "");

        // Удаляем все остальные теги, кроме разрешённых
        cleaned = cleaned.replaceAll("(?i)<(?!/?(b|strong|i|em|u|ins|s|strike|del|a|code|pre)(\\s|>|$)).*?>", "");

        // Telegram не любит лишние пробелы и пустые строки
        return cleaned.trim();
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
