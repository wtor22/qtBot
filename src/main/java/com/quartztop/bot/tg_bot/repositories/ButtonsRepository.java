package com.quartztop.bot.tg_bot.repositories;

import com.quartztop.bot.tg_bot.entity.buttons.Button;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ButtonsRepository extends JpaRepository<Button, Long> {

    boolean existsByTextButton(String textButton);

    Button findByTextButton(String textButton);

    List<Button> findAllByOrderByOrderInBotIndexAsc();

}
