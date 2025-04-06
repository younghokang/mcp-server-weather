package com.example.weather_api;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 *  T1H	기온	℃
    RN1	1시간 강수량	mm
    UUU	동서바람성분	m/s
    VVV	남북바람성분	m/s
    REH	습도	%
    PTY	강수형태	코드값
    VEC	풍향	deg
    WSD	풍속	m/s
 */
@Builder
@Getter
@ToString
public class WeatherResponse {
    private String temperature; // 기온
    private String humidity; // 습도
    private String precipitationType; // 강수형태
    private String windDirection; // 풍향
    private String windSpeed; // 풍속
    private String rainfall; // 1시간 강수량
    private String eastWestWindSpeed; // 동서바람성분
    private String northSouthWindSpeed; // 남북바람성분
    
}
