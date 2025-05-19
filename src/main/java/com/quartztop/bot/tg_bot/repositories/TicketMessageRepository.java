package com.quartztop.bot.tg_bot.repositories;

import com.quartztop.bot.tg_bot.entity.activity.TicketMessage;
import com.quartztop.bot.tg_bot.entity.botUsers.BotUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketMessageRepository extends JpaRepository<TicketMessage, Long> {
    List<TicketMessage> findAllByTicketNumberOrderByTimestamp(String ticketNumber);

    List<TicketMessage> findAllByBotUser(BotUser botUser);

    Optional<TicketMessage> findFirstByBotUserOrderByTimestampDesc(BotUser botUser);
    Optional<TicketMessage> findFirstByTicketNumberOrderByTimestampAsc(String ticketNumber);
}
