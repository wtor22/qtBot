package com.quartztop.bot.tg_bot.entity.activity;

import com.quartztop.bot.tg_bot.entity.botUsers.BotUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
public class SearchRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private LocalDateTime timeRequest;
    private String request;

    @ManyToOne
    @JoinColumn(name = "bot_user_id")
    private BotUser botUser;
}
