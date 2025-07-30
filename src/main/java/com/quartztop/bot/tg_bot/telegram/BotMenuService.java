package com.quartztop.bot.tg_bot.telegram;

import com.quartztop.bot.tg_bot.dto.ButtonDto;
import com.quartztop.bot.tg_bot.entity.botUsers.BotUser;
import com.quartztop.bot.tg_bot.entity.botUsers.BotUserRole;
import com.quartztop.bot.tg_bot.entity.botUsers.Roles;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BotMenuService {

    private final ButtonsService buttonsService;

    public SendMessage linkImageMenu(long chatId) {
        List<ButtonDto> list = buttonsService.getAll();
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

    public static SendMessage mainMenu(long chatId, BotUser user) {
        KeyboardRow row1 = new KeyboardRow();
        row1.add(new KeyboardButton("📦 Проверить наличие"));
        row1.add(new KeyboardButton("📷 Фото изделий"));

        KeyboardRow row2 = new KeyboardRow();
        if(user.getBotUserRole().getRole() == Roles.ANALYST) {
            row2.add(new KeyboardButton("\uD83D\uDCCA Отчеты"));
        } else {
            row2.add(new KeyboardButton("🎁 Акции"));
            row2.add(new KeyboardButton("❓Задать вопрос"));
        }

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

    public static SendMessage reportMenu(long chatId, BotUser user) {

        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();

        if(user.getBotUserRole().getRole() == Roles.ANALYST) {
            log.info("ПОЛЬЗОВАТЕЛЮ " + user.getFirstName() + " " + user.getLastName() + " с правами " + user.getBotUserRole().getRole() +
                    " ОТПРАВЛЕНО МЕНЮ ВЫБОРА ОТЧЕТОВ ");
            row1.add(new KeyboardButton("\uD83D\uDCCA Основной Отчет"));
            row1.add(new KeyboardButton("\uD83D\uDCCA Рейтинги товаров"));
            row2.add(new KeyboardButton("\uD83D\uDCCA Основной Отчет ИнтерСтоун"));
            row2.add(new KeyboardButton("\uD83D\uDCCA Рейтинги товаров ИнтерСтоун"));
            row3.add(new KeyboardButton("\uD83D\uDCCA Остатки товаров"));
        } else {
            row1.add(new KeyboardButton("🎁 Акции"));
            row1.add(new KeyboardButton("❓Задать вопрос"));
        }

        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);

        ReplyKeyboardMarkup markup = ReplyKeyboardMarkup.builder()
                .keyboard(keyboard)
                .resizeKeyboard(true)
                .oneTimeKeyboard(false)
                .build();
        return SendMessage.builder()
                .chatId(chatId)
                .text("Выберите отчет \uD83D\uDC47")
                .replyMarkup(markup)
                .build();
    }

    public static SendMessage linkStockReportMenu(long chatId, BotUser user) {
        if (user.getBotUserRole().getRole() != Roles.ANALYST) return null;
        List<InlineKeyboardRow> listRows = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton("Остатки товаров по складам и категориям");
        inlineKeyboardButton.setCallbackData("STOCK_REPORT");
        InlineKeyboardRow inlineKeyboardButtons = new InlineKeyboardRow();
        inlineKeyboardButtons.add(inlineKeyboardButton);
        listRows.add(inlineKeyboardButtons);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(listRows);

        return SendMessage.builder()
                .chatId(chatId)
                .text("Выберите отчет \uD83D\uDC47")
                .replyMarkup(inlineKeyboardMarkup)
                .build();

    }


    public static SendMessage linkGeneralReportMenu(long chatId, BotUser user, String typeReport) {
        if (user.getBotUserRole().getRole() != Roles.ANALYST) return null;

        int startYear = 2023;
        int nowYear = LocalDate.now().getYear();

        List<InlineKeyboardRow> listRows = new ArrayList<>();
        for(int i = startYear; i <= nowYear; i++) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(String.valueOf(i));
            inlineKeyboardButton.setCallbackData(typeReport + i);
            InlineKeyboardRow inlineKeyboardButtons = new InlineKeyboardRow();
            inlineKeyboardButtons.add(inlineKeyboardButton);
            listRows.add(inlineKeyboardButtons);
        }

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup(listRows);

        return SendMessage.builder()
                .chatId(chatId)
                .text("Выберите год отчета \uD83D\uDC47")
                .replyMarkup(inlineKeyboardMarkup)
                .build();
    }

    public static SendMessage sendPhoneRequestKeyboard(Long chatId) {

        String messageText = """
        📞 Для начала отправь мне твой номер телефона 😉
        
        Чтобы продолжить, просто нажми на кнопку внизу ⬇️
        
        Не переживай, я просто хочу убедиться, что ты настоящий человек, а не такой как я — железяка с Wi-Fi 😅
        
        ‼️Обрати внимание. Иногда отправить номер телефона возможно только с устройства на котором установлена сим карта.
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
