package com.quartztop.bot.tg_bot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "bot_users")
public class BotUser {

    @Id
    private Long telegramId;

    private String username;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDateTime registeredAt;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

}
