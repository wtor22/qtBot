package com.quartztop.bot.tg_bot.responses.telegramResponses;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class NextActionResult {
    private final boolean success; // получилось ли вообще достучаться до API
    private final TelegramActionDto action; // сама акция, если есть

    public boolean hasAction() {
        return action != null;
    }

}
