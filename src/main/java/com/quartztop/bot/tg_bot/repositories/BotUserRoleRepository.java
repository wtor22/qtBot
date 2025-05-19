package com.quartztop.bot.tg_bot.repositories;

import com.quartztop.bot.tg_bot.entity.botUsers.BotUserRole;
import com.quartztop.bot.tg_bot.entity.botUsers.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BotUserRoleRepository extends JpaRepository<BotUserRole, Long> {

    boolean existsByRole(Roles role);
    BotUserRole findByRole(Roles role);
}
