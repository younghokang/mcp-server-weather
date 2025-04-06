package com.example.weather_api;

public enum Weather {
    CLEAR("0", "없음"),
    RAIN("1", "비"),
    RAIN_SNOW("2", "비/눈"),
    SNOW("3", "눈"),
    RAINDROP("5", "빗방울"),
    RAINDROP_OF_SNOW("6", "빗방울눈날림"),
    SNOWFLAKE("7", "눈날림");
    
    private final String code;
    private final String description;
    Weather(String code, String description) {
        this.code = code;
        this.description = description;
    }
    public String getCode() {
        return code;
    }
    public String getDescription() {
        return description;
    }
    public static Weather fromCode(String code) {
        for (Weather weather : Weather.values()) {
            if (weather.getCode().equals(code)) {
                return weather;
            }
        }
        return null;
    }
}
