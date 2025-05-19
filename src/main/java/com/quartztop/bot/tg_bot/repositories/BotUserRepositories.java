package com.quartztop.bot.tg_bot.repositories;

import com.quartztop.bot.tg_bot.entity.botUsers.BotUser;
import com.quartztop.bot.tg_bot.entity.botUsers.BotUserRole;
import com.quartztop.bot.tg_bot.entity.botUsers.BotUserStatus;
import com.quartztop.bot.tg_bot.entity.botUsers.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BotUserRepositories extends JpaRepository<BotUser, Long> {
    boolean existsByTelegramId(Long telegramId);

    Optional<BotUser> findByTelegramId(Long id);
    Long countByRegisteredAtBetween(LocalDateTime start, LocalDateTime end);

    long countByStatus(BotUserStatus botUserStatus);

    Long countByStatusAndRegisteredAtBetween(BotUserStatus botUserStatus, LocalDateTime start, LocalDateTime end);

    List<BotUser> findByPhoneNumberContainingAndBotUserRole(String part, BotUserRole botUserRole);

    List<BotUser> findByBotUserRole(BotUserRole role);
}
