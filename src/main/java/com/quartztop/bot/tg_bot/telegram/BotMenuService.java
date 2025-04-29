package com.quartztop.bot.tg_bot.telegram;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
public class BotMenuService {

    public static SendMessage mainMenu(long chatId) {
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
        return SendMessage.builder()
                .chatId(chatId)
                .text("Главное меню в самом низу \uD83D\uDC47")
                .replyMarkup(markup)
                .build();
    }

    public static SendMessage sendPhoneRequestKeyboard(Long chatId) {

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

        return SendMessage.builder()
                .chatId(chatId)
                .text(messageText)
                .replyMarkup(keyboardMarkup)
                .build();
    }

}
