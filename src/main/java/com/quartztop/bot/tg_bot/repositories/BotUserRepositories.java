package com.quartztop.bot.tg_bot.repositories;

import com.quartztop.bot.tg_bot.entity.BotUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BotUserRepositories extends JpaRepository<BotUser, Long> {
    boolean existsByTelegramId(Long telegramId);

    Optional<BotUser> findByTelegramId(Long id);

}
