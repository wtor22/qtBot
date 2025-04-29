package com.quartztop.bot.tg_bot.responses.telegramResponses;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TelegramActionDto {

    private String name;
    private String description;
    private String titleImageUrl;
    private String content;
    private Long id;
}
