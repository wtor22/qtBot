package com.quartztop.bot.tg_bot.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BotUserDTO {
    private Long telegramId;
    private String username;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDateTime registeredAt;
}
