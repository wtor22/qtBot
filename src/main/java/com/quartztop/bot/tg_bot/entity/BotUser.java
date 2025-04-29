package com.quartztop.bot.tg_bot.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
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
    private BotUserStatus status;

    @ManyToOne
    @JoinColumn(name = "user_role")
    private BotUserRole botUserRole;

}
