package com.example.weather_api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestClient;

import com.example.weather_api.WeatherResponse.WeatherResponseBuilder;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WeatherService {

    private static final String COMMA_DELIMITER = ",";

    @Autowired
	private RestClient restClient;

    @Value("${data-go-kr.api.key}")
    private String OPEN_API_KEY;

    /**
     * Get weather forecast for a specific province/district/neighborhood
     * @param province Sido: A province in South Korea
     * @param district Gu: A district in South Korea
     * @param neighborhood Dong: A neighborhood in South Korea
     * @return 
     * @throws IOException 
     */
    @Tool(description = "Get weather forecast for a specific province/district/neighborhood")
    public String getWeatherForecastByLocation(
            String province, // Sido: A province in South Korea
            String district, // Gu: A district in South Korea
            String neighborhood // Dong: A neighborhood in South Korea
    ) throws IOException {
        // Returns weather forecast including:
        // - Temperature
        // - Humidity
        // - Precipitation type
        // - Wind direction
        // - Wind speed
        // - Rainfall
        
        log.info("OPEN_API_KEY: {}", OPEN_API_KEY);

        String level1 = "서울특별시";
        String level2 = "종로구";
        String level3 = "청운효자동";
        
        List<List<String>> list = loadCsv();
        int[] xy = getGridCoordinates(list, level1, level2, level3);

        String baseDate = getCurrentDate();
        String baseTime = getCurrentTime();
        log.info("Base Date: {}", baseDate);
        log.info("Base Time: {}", baseTime);

        ResponseEntity<JsonNode> response = restClient.get()
            .uri(uriBuilder -> uriBuilder
                .path("/getUltraSrtNcst")
                .queryParam("serviceKey", "{serviceKey}")
                .queryParam("numOfRows", "10")
                .queryParam("pageNo", "1")
                .queryParam("dataType", "JSON")
                .queryParam("base_date", baseDate)
                .queryParam("base_time", baseTime)
                .queryParam("nx", xy[0])
                .queryParam("ny", xy[1])
                .build(OPEN_API_KEY)
            )
            .retrieve()
            .toEntity(JsonNode.class);

        log.info("Response Status Code: {}", response.getStatusCode());
        
        JsonNode jsonNode = response.getBody();
        if (jsonNode != null) {
            log.info("Response: {}", response.getBody());

            WeatherResponseBuilder builder = WeatherResponse.builder();
            jsonNode.get("response").get("body").get("items").get("item").forEach(item -> {
                log.info("Weather Item: {}", item);
                String category = item.get("category").asText();
                String value = item.get("obsrValue").asText();
                log.info("Category: {}, Value: {}", category, value);
 
                if (category.equals("T1H")) {
                    builder.temperature(value + " °C");
                } else if (category.equals("REH")) {
                    builder.humidity(value + " %");
                } else if (category.equals("PTY")) {
                    Weather weather = Weather.fromCode(value);
                    builder.precipitationType(weather.getDescription());
                } else if (category.equals("VEC")) {
                    builder.windDirection(value + " °");
                } else if (category.equals("WSD")) {
                    builder.windSpeed(value + " m/s");
                } else if (category.equals("RN1")) {
                    builder.rainfall(value + " mm");
                } else if (category.equals("UUU")) {
                    builder.eastWestWindSpeed(value + " m/s");
                } else if (category.equals("VVV")) {
                    builder.northSouthWindSpeed(value + " m/s");
                }
            });

            WeatherResponse weatherResponse = builder.build();
            log.info("Weather Response: {}", weatherResponse);
            return weatherResponse.toString();
        }
        return "Error retrieving weather data";
    }
	
    private int[] getGridCoordinates(List<List<String>> list, String level1, String level2, String level3) {
        if(level2 == null) {
            level2 = "";
        }
        if (level3 == null) {
            level3 = "";
        }
        for (List<String> record : list) {
            if (record.get(2).equals(level1) && record.get(3).equals(level2) && record.get(4).equals(level3)) {
                int x = Integer.parseInt(record.get(5));
                int y = Integer.parseInt(record.get(6));
                int[] gridCoordinates = { x, y };
                log.info("Grid coordinates: {}, {}", gridCoordinates[0], gridCoordinates[1]);
                return gridCoordinates;
            }
        }
        return new int[] {60, 127}; // Default coordinates if not found
    }

    private List<List<String>> loadCsv() throws IOException {
        File file;
        try {
            file = ResourceUtils.getFile("classpath:static/latlng.csv");
        } catch (FileNotFoundException e) {
            log.error("CSV file not found: {}", e.getMessage());
            throw e;
        }

        try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
            List<List<String>> records = reader.lines()
                .map(line -> Arrays.asList(line.split(COMMA_DELIMITER)))
                .skip(1)
                .collect(Collectors.toList());
                return records;
        } catch(IOException e) {
            log.error("Error reading CSV file: {}", e.getMessage());
            throw e;
        }
    }

    private String getCurrentDate() {
        return DateTimeFormatter.BASIC_ISO_DATE.format(LocalDate.now());
    }
    private String getCurrentTime() {
        return DateTimeFormatter.ISO_LOCAL_TIME.format(LocalTime.now()).replace(":", "").substring(0, 4);
    }
}
