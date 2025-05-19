package com.quartztop.bot.tg_bot.telegram;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TicketSessionService {

    // Потокобезопасная мапа: chatId -> ticketId
    private final Map<Long, String> ticketSessionMap = new ConcurrentHashMap<>();

    // Установить тикет в сессию чата
    public void startSession(Long chatId, String ticketNumber) {
        ticketSessionMap.put(chatId, ticketNumber);
    }

    // Получить ID тикета для данного чата (не удаляя)
    public String getTicketId(Long chatId) {
        return ticketSessionMap.get(chatId);
    }

    // Завершить сессию и получить ticketId
    public String endSession(Long chatId) {
        return ticketSessionMap.remove(chatId);
    }

    // Проверка: есть ли активная сессия
    public boolean hasSession(Long chatId) {
        return ticketSessionMap.containsKey(chatId);
    }

    // Очистить всё (например, при рестарте)
    public void clearAll() {
        ticketSessionMap.clear();
    }
}
