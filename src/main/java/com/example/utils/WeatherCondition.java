package com.example.utils;

public enum WeatherCondition {

    CLEAR("Clear", "Ясно"),
    CLOUDS("Clouds", "Облачно"),
    RAIN("Rain", "Дождь"),
    SNOW("Snow", "Снег"),
    THUNDERSTORM("Thunderstorm", "Гроза"),
    DRIZZLE("Drizzle", "Морось"),
    MIST("Mist", "Туман"),
    HAZE("Haze", "Мгла");

    private final String englishName;
    private final String russianName;

    WeatherCondition(String englishName, String russianName) {
        this.englishName = englishName;
        this.russianName = russianName;
    }

    public String getEnglishName() {
        return englishName;
    }

    public String getRussianName() {
        return russianName;
    }

    public static WeatherCondition fromEnglishName(String englishName) {
        if (englishName == null) {
            return null;
        }
        for (WeatherCondition condition : values()) {
            if (condition.englishName.equalsIgnoreCase(englishName.trim())) {
                return condition;
            }
        }
        return null;
    }

    public static String translate(String englishName) {
        WeatherCondition condition = fromEnglishName(englishName);
        return condition != null ? condition.russianName : englishName;
    }
}