package com.quartztop.bot.tg_bot.utils;

public class PhoneUtils {
    public static Region getRegionByPhone(String phone) {
        if (phone == null) return Region.UNKNOWN;

        // Очищаем всё, оставляя только цифры
        phone = phone.replaceAll("[^0-9]", "");

        if (phone.startsWith("7")) return Region.RU;
        if (phone.startsWith("375")) return Region.BY;

        return Region.UNKNOWN;
    }
}
