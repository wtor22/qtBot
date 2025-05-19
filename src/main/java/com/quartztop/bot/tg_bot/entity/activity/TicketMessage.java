package com.quartztop.bot.tg_bot.entity.activity;

import com.quartztop.bot.tg_bot.entity.botUsers.BotUser;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
public class TicketMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ticketNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    private BotUser botUser;

    @Enumerated(EnumType.STRING)
    private MessageType type;

    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    @Column(length = 2048)
    private String text;

    private LocalDateTime timestamp;

    public TicketMessage() {
        this.timestamp = LocalDateTime.now();
    }
}
