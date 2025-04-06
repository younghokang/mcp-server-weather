package com.example.weather_api;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class WeatherApiApplication {

	@Value("${data-go-kr.weather.url}") 
	private String OPEN_API_WEATHER_URL;

	public static void main(String[] args) {
		SpringApplication.run(WeatherApiApplication.class, args);
	}

	@Bean
	public ToolCallbackProvider weatherTools(WeatherService weatherService) {
		return  MethodToolCallbackProvider.builder().toolObjects(weatherService).build();
	}

	@Bean
	public RestClient restClient() {
		return RestClient.builder()
				.requestFactory(new HttpComponentsClientHttpRequestFactory())
				.baseUrl(OPEN_API_WEATHER_URL)
				.build();
	}

}
