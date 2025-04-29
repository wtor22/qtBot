package com.quartztop.bot.tg_bot.telegram;

import com.quartztop.bot.tg_bot.config.BotConfig;
import com.quartztop.bot.tg_bot.entity.BotUser;
import com.quartztop.bot.tg_bot.entity.ClickType;
import com.quartztop.bot.tg_bot.repositories.BotUserRepositories;
import com.quartztop.bot.tg_bot.responses.telegramResponses.NextActionResult;
import com.quartztop.bot.tg_bot.responses.telegramResponses.TelegramActionDto;
import com.quartztop.bot.tg_bot.integration.ActionClient;
import com.quartztop.bot.tg_bot.services.crud.ActionClickService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class CallbackHandler {

    private final TelegramClient telegramClient;
    private final ActionClient actionClient;
    private final BotMessageUtils botMessageUtils;
    private final BotUserRepositories botUserRepositories;
    private final ActionClickService actionClickService;

    public CallbackHandler(BotConfig botConfig, ActionClient actionClient, BotMessageUtils botMessageUtils, BotUserRepositories botUserRepositories, ActionClickService actionClickService) {
        this.actionClient = actionClient;
        telegramClient = new OkHttpTelegramClient(botConfig.getToken());
        this.botMessageUtils = botMessageUtils;
        this.botUserRepositories = botUserRepositories;
        this.actionClickService = actionClickService;
    }

    public void handleCallback(CallbackQuery callbackQuery, Map<Long, String> userState, User tgUser) throws InterruptedException {
        String data = callbackQuery.getData();
        Long chatId = callbackQuery.getMessage().getChatId();

        log.error("Print data " + data);

        BotUser user = botUserRepositories.findByTelegramId(tgUser.getId()).orElseThrow();
        if (data.startsWith("next_action_")) {
            String idStr = data.replace("next_action_", "");
            Long currentId = Long.parseLong(idStr);
            NextActionResult result = actionClient.getNextAction(currentId);
            actionClickService.create(null,user, ClickType.NEXT);
            if (!result.isSuccess()) {
                sendText(chatId, "⛔ Сейчас не могу получить информацию. Попробуй чуть позже.");
                return;
            }
            if (!result.hasAction()) {
                sendText(chatId, "🎉 Больше нет активных акций");
                return;
            }
            TelegramActionDto action = result.getAction();
            botMessageUtils.sendPreviewAction(chatId,action);
        }
        if (data.startsWith("action_")) {
            String idStr = data.replace("action_", "");
            long currentId = Long.parseLong(idStr);
            actionClickService.create(currentId,user, ClickType.MORE_DETAILS);
            TelegramActionDto actionDto = actionClient.getActionById(currentId);
            sendFullAction(chatId, actionDto);
        }
        if ("start_registration".equals(data)) {
            sendText(chatId, "📋  Отлично! Давай начнём регистрацию.");
            userState.put(chatId, "AWAITING_PHONE");
            send(BotMenuService.sendPhoneRequestKeyboard(chatId));
        }
    }

    private void sendFullAction(long chatId, TelegramActionDto actionDto) {
        //String detailsUrl = "action_" + actionDto.getId();
        String nextAction = "next_action_" + actionDto.getId();

        String messageText = "<b>" + actionDto.getName() + "</b>\n\n"
                + BotMessageUtils.sanitizeHtmlForTelegram(actionDto.getContent());

        InlineKeyboardButton buttonNext = new InlineKeyboardButton("Следующая");
        buttonNext.setCallbackData(nextAction);

        InlineKeyboardRow row = new InlineKeyboardRow();
        row.add(buttonNext);
        List<InlineKeyboardRow> keyboard = new ArrayList<>(List.of(row));

        // создаём клавиатуру
        InlineKeyboardMarkup markup = InlineKeyboardMarkup.builder()
                .keyboard(keyboard)
                .build();
        botMessageUtils.sendTextWithKeyboard(chatId, messageText, markup);

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

}
