package com.quartztop.bot.tg_bot.entity.activity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TicketStatus {
    OPENED("🟢 Открыт"),
    IN_WORK("🟡 В работе"),
    CLOSED("🔴 Закрыт");

    private final String label;
}
