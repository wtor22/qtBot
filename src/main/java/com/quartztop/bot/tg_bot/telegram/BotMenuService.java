package com.quartztop.bot.tg_bot.telegram;

import com.quartztop.bot.tg_bot.dto.ButtonDto;
import com.quartztop.bot.tg_bot.entity.buttons.Button;
import com.quartztop.bot.tg_bot.services.crud.ButtonsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BotMenuService {

    private final ButtonsService buttonsService;

    public SendMessage linkImageMenu(long chatId) {
        log.error("START IMAGES MENU");
        List<ButtonDto> list = buttonsService.getAll();
        log.error("LIST BUTTON DTO SIZE = " + list.size());
        List<InlineKeyboardRow> listRows = new ArrayList<>();
        for(ButtonDto buttonDto : list) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(buttonDto.getTextButton());
            inlineKeyboardButton.setUrl(buttonDto.getButtonValue());
            InlineKeyboardRow inlineKeyboardButtons = new InlineKeyboardRow();
            inlineKeyboardButtons.add(inlineKeyboardButton);
            listRows.add(inlineKeyboardButtons);
        }
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(listRows);

        return SendMessage.builder()
                .chatId(chatId)
                .text("Переходите по ссылкам ниже \uD83D\uDC47")
                .replyMarkup(inlineKeyboardMarkup)
                .build();
    }

    public static SendMessage mainMenu(long chatId) {
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("📦 Проверить наличие"));
        row1.add(new KeyboardButton("📷 Фото изделий"));

        KeyboardRow row2 = new KeyboardRow();
        row2.add(new KeyboardButton("🎁 Акции"));
        row2.add(new KeyboardButton("❓Задать вопрос"));

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
