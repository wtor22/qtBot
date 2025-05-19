package com.quartztop.bot.tg_bot.entity.botUsers;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Table(name = "bot_users")
public class BotUser {

    @Id
    private Long telegramId;

    @Column(name = "user_name")
    private String username;
    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "phone")
    private String phoneNumber;
    @Column(name = "telegram_fio")
    private String telegramFio;
    @Column(name = "time_registered")
    private LocalDateTime registeredAt;

    @Enumerated(EnumType.STRING)
    private BotUserStatus status;

    @ManyToOne
    @JoinColumn(name = "user_role")
    private BotUserRole botUserRole;

}
