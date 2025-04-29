package com.quartztop.bot.tg_bot.telegram;

import com.quartztop.bot.tg_bot.entity.BotUser;
import com.quartztop.bot.tg_bot.entity.BotUserStatus;
import com.quartztop.bot.tg_bot.repositories.BotUserRepositories;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@Slf4j
public class RegistrationHandler {

    private final BotMessageUtils botMessageUtils;
    private final BotUserRepositories botUserRepositories;

    public RegistrationHandler(BotMessageUtils botMessageUtils, BotUserRepositories botUserRepositories) {
        this.botMessageUtils = botMessageUtils;
        this.botUserRepositories = botUserRepositories;
    }

    void handleRegistrationSteps(BotUser user, Message message, Map<Long, String> userState) throws InterruptedException {
        long chatId = message.getChatId();
        String state = userState.get(chatId);

        if (state == null) {
            botMessageUtils.sendWelcomeMessage(chatId);
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
                    botMessageUtils.sendText(chatId, "✅ Телефон сохранён!");
                    userState.put(chatId, "AWAITING_NAME");
                    botMessageUtils.sendText(chatId, "Теперь введи своё имя");
                } else {
                    botMessageUtils.send(BotMenuService.sendPhoneRequestKeyboard(chatId));
                }
            }

            case "AWAITING_NAME" -> {
                if (message.hasText()) {
                    user.setFirstName(message.getText());
                    botUserRepositories.save(user);
                    userState.put(chatId, "AWAITING_LASTNAME");
                    botMessageUtils.sendText(chatId, "Отлично " + user.getFirstName() + " полдела сделано! \nТеперь введи свою фамилию");
                }
            }
            case "AWAITING_LASTNAME" -> {
                if (message.hasText()) {
                    user.setLastName(message.getText());
                    user.setStatus(BotUserStatus.ACTIVE);


                    botUserRepositories.save(user);

                    botMessageUtils.sendText(chatId, "\uD83C\uDF89 Ура! Регистрация завершена.\n" +
                            "\n" +
                            user.getFirstName() + " " + user.getLastName() +" ты теперь в нашей команде \uD83D\uDCAA\n" +
                            "Всё самое полезное уже под рукой:\n" +
                            "актуальные остатки, цены, медиаматериалы и новинки  \uD83E\uDDF1\n" +
                            "\n" +
                            "Если что-то понадобится — просто задай вопрос!");
                    userState.put(chatId, "MAIN_MENU");
                    botMessageUtils.send(BotMenuService.mainMenu(chatId));
                }
            }
        }
    }

    void registerUserIfNeeded(User tgUser) {
        Long telegramId = tgUser.getId();
        if (!botUserRepositories.existsByTelegramId(telegramId)) {
            BotUser user = new BotUser();
            user.setTelegramId(telegramId);
            user.setUsername(tgUser.getUserName());
            user.setFirstName(tgUser.getFirstName());
            user.setLastName(tgUser.getLastName());
            user.setRegisteredAt(LocalDateTime.now());
            user.setStatus(BotUserStatus.REGISTERED);
            botUserRepositories.save(user);
            log.info("Зарегистрирован новый пользователь: " + telegramId);
        }
    }
}
