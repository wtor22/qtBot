package com.quartztop.bot.tg_bot.telegram;

import com.quartztop.bot.tg_bot.config.BotConfig;
import com.quartztop.bot.tg_bot.dto.BotUserDTO;
import com.quartztop.bot.tg_bot.entity.activity.TicketMessage;
import com.quartztop.bot.tg_bot.entity.botUsers.BotUser;
import com.quartztop.bot.tg_bot.entity.botUsers.BotUserRole;
import com.quartztop.bot.tg_bot.entity.botUsers.Roles;
import com.quartztop.bot.tg_bot.repositories.BotUserRoleRepository;
import com.quartztop.bot.tg_bot.responses.telegramResponses.TelegramActionDto;
import com.quartztop.bot.tg_bot.services.crud.BotUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class BotMessageUtils {

    private final TelegramClient telegramClient;
    private final BotUserService botUserService;
    private final BotUserRoleRepository botUserRoleRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public BotMessageUtils(BotConfig botConfig, BotUserService botUserService, BotUserRoleRepository botUserRoleRepository) {
        telegramClient = new OkHttpTelegramClient(botConfig.getToken());
        this.botUserService = botUserService;
        this.botUserRoleRepository = botUserRoleRepository;
    }

    void sendText(long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode("HTML")
                .build();
        send(message);
    }
    void send(SendMessage message) {
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения" + e.getMessage());
        }
    }
    void send(SendPhoto sendPhoto) {
        try {
            telegramClient.execute(sendPhoto);
        } catch (TelegramApiException e) {
            // Обработка ошибок, если что-то пошло не так
            log.error("Ошибка при отправке сообщения" + e.getMessage());
        }
    }

    public void sendPreviewAction(long chatId, TelegramActionDto dto) throws InterruptedException {
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

    //public void sendBotUserAnswerNotification()

    public void sendAdminQuestionNotification(BotUser user, TicketMessage ticket) {

        String text = "❓ Новый вопрос от " + user.getFirstName() + ":\n\n"
                + ticket.getText()
                + "\n\n📎 Тикет №" + ticket.getTicketNumber();

        InlineKeyboardButton button = new InlineKeyboardButton("✍ Ответить");
        button.setCallbackData("REPLY_TICKET:" + ticket.getTicketNumber());

        InlineKeyboardRow row = new InlineKeyboardRow();
        row.add(button);
        List<InlineKeyboardRow> keyboard = new ArrayList<>(List.of(row));

        // создаём клавиатуру
        InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder()
                .keyboard(keyboard)
                .build();

        BotUserRole roleAdmin = botUserRoleRepository.findByRole(Roles.ADMIN);
        List<BotUserDTO> listAdmins = botUserService.getUsersDTOByRole(roleAdmin);

        for(BotUserDTO userDTO : listAdmins) {
            Long chatId = userDTO.getTelegramId();
            sendTextWithKeyboard(chatId, text, markup);
        }
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

    public void sendTextWithKeyboard(Long chatId, String text, InlineKeyboardMarkup keyboard) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId.toString())
                .text(text)
                .parseMode("HTML")
                .replyMarkup(keyboard)
                .build();
        send(message);
    }

    public void sendWelcomeMessage(Long chatId) {
        String welcomeText = """
            <b>👋 Добро пожаловать!</b>
            Я — <b>SLABSTOCK BOT</b> 🧱
            
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
}
