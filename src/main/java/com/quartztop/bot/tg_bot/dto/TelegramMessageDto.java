package com.quartztop.bot.tg_bot.dto;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelegramMessageDto {
    private String username;
    private String text;
}
