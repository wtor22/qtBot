package com.quartztop.bot.tg_bot.services.crud;

import com.quartztop.bot.tg_bot.entity.activity.MessageType;
import com.quartztop.bot.tg_bot.entity.activity.TicketMessage;
import com.quartztop.bot.tg_bot.entity.activity.TicketStatus;
import com.quartztop.bot.tg_bot.entity.botUsers.BotUser;
import com.quartztop.bot.tg_bot.repositories.TicketMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TicketMessageService {

    private final TicketMessageRepository ticketMessageRepository;

    public TicketMessage addQuestion(BotUser user, String text) {
        String ticketNumber = generateTicketNumber(); // например, UUID
        return saveMessage(ticketNumber, user, text, MessageType.QUESTION, TicketStatus.OPENED);
    }

    public TicketMessage addAnswer(String ticketNumber, BotUser user, String text) {
        return saveMessage(ticketNumber, user, text, MessageType.ANSWER,TicketStatus.IN_WORK);
    }

    public void update(TicketMessage ticketMessage) {
        ticketMessageRepository.save(ticketMessage);
    }
    public List<TicketMessage> getMessagesByTicket(String ticketNumber) {
        return ticketMessageRepository.findAllByTicketNumberOrderByTimestamp(ticketNumber);
    }
    public TicketMessage getFirstMessageTicketByTicketNumber(String ticketNumber) {
        return ticketMessageRepository.findFirstByTicketNumberOrderByTimestampAsc(ticketNumber).orElseThrow();
    }

    private TicketMessage saveMessage(String ticketNumber, BotUser user, String text, MessageType type, TicketStatus status) {
        TicketMessage message = new TicketMessage();
        message.setTicketNumber(ticketNumber);
        message.setBotUser(user);
        message.setText(text);
        message.setType(type);
        message.setStatus(status);
        message.setTimestamp(LocalDateTime.now());
        return ticketMessageRepository.save(message);
    }

    private String generateTicketNumber() {
        return "TICKET-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
