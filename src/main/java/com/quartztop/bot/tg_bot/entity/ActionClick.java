package com.quartztop.bot.tg_bot.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "action_click")
public class ActionClick {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "click_time")
    private LocalDateTime clickTime;

    @Enumerated(EnumType.STRING)
    private ClickType clickType;

    @Column(name = "action_id")
    private Long actionId;

    @ManyToOne
    @JoinColumn(name = "bot_user_id")
    private BotUser botUser;
}
