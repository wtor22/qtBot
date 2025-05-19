package com.quartztop.bot.tg_bot.repositories;

import com.quartztop.bot.tg_bot.entity.activity.ActionClick;
import com.quartztop.bot.tg_bot.entity.activity.ClickStats;
import com.quartztop.bot.tg_bot.entity.activity.ClickType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ActionClickRepositories extends JpaRepository<ActionClick,Long> {
    long countByClickType(ClickType clickType);

    List<ActionClick> findAllByClickType(ClickType clickType);
    @Query("SELECT ac.clickType AS clickType, COUNT(ac) AS count " +
            "FROM ActionClick ac " +
            "WHERE ac.clickTime BETWEEN :start AND :end " +
            "GROUP BY ac.clickType")
    List<ClickStats> countGroupedByClickType(@Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end);
}
