package com.quartztop.bot.tg_bot.repositories;

import com.quartztop.bot.tg_bot.entity.ActionClick;
import com.quartztop.bot.tg_bot.entity.ClickType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActionClickRepositories extends JpaRepository<ActionClick,Long> {
    long countByClickType(ClickType clickType);

    List<ActionClick> findAllByClickType(ClickType clickType);
}
