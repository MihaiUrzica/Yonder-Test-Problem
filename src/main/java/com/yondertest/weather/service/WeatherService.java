package com.yondertest.weather.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import com.yondertest.weather.controller.CityAvgTempAndWindSpeed;
import com.yondertest.weather.controller.WeatherAPIResponse;
import com.yondertest.weather.model.DailyForecast;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class WeatherService {

    @Value("${weather-api.url}")
    private String weatherAPIUrl;

    public Mono<CityAvgTempAndWindSpeed> mapWeatherAPIResponseToCityAvgTempAndWindSpeed (WeatherAPIResponse weatherAPIResponse){
        CityAvgTempAndWindSpeed cityAvgTempAndWindSpeed = CityAvgTempAndWindSpeed.builder()
                .name(weatherAPIResponse.getName())
                .temperature(convertAverageTemperatureToString(calculateAverageTemperature(weatherAPIResponse.getForecast())))
                .wind(convertAverageWindSpeedToString(calculateAverageWindSpeed(weatherAPIResponse.getForecast())))
                .build();
        return Mono.just(cityAvgTempAndWindSpeed);
    }

    public WeatherAPIResponse getWeatherAPIResponseForCity(String cityName) throws JsonProcessingException {
        String jsonResponse = getJsonResponse(cityName);
        if (jsonResponse.isEmpty()){
            return computeForNoDataAvailable(cityName);
        }
        else {
            return computeJsonResponseToObject(jsonResponse, cityName);
        }
    }

    public Flux<CityAvgTempAndWindSpeed> getCityAvgTempAndWindSpeedFlux(Flux<ResponseEntity<WeatherAPIResponse>> weatherAPIResponseFlux) {
        Flux<CityAvgTempAndWindSpeed> result = weatherAPIResponseFlux
                .flatMap(responseEntity -> mapWeatherAPIResponseToCityAvgTempAndWindSpeed(responseEntity.getBody()));
        saveResultsToCSV(result);
        return result;
    }

    private void saveResultsToCSV(Flux<CityAvgTempAndWindSpeed> cityDetailsFlux) {
        final String filePath = "./report.csv";

        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath, false))) {
            writer.writeNext(new String[]{"City","Temperature","Wind Speed"});
            cityDetailsFlux.subscribe(
                    city -> writer.writeNext(new String[]{city.getName(),city.getTemperature(), city.getWind()}),
                    error -> log.error("Error {}", error.getMessage()),
                    ()-> {
                        try {
                            writer.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private double calculateAverageWindSpeed(List<DailyForecast> forecasts){
        return forecasts.stream().mapToDouble(DailyForecast::getWind).average().orElse(-1.0);
    }

    private double calculateAverageTemperature(List<DailyForecast> forecasts){
        return forecasts.stream().mapToDouble(DailyForecast::getWind).average().orElse(-999.0);
    }

    private WeatherAPIResponse computeForNoDataAvailable(String cityName) {
        return WeatherAPIResponse.builder()
                .name(cityName)
                .temperature(0)
                .wind(0)
                .description("")
                .forecast(new ArrayList<>())
                .build();
    }

    private WeatherAPIResponse computeJsonResponseToObject(String jsonResponse, String cityName) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        WeatherAPIResponse response = objectMapper.readValue(jsonResponse, WeatherAPIResponse.class);
        response.setName(cityName);
        return response;
    }

    private String getJsonResponse(String cityName) {
        WebClient webClient = WebClient.create();
        return webClient.get().uri(weatherAPIUrl + cityName).retrieve()
                .bodyToMono(String.class)
                .onErrorResume(
                        ex -> ex instanceof WebClientResponseException &&
                                ((WebClientResponseException) ex).getStatusCode().is4xxClientError(),
                        ex -> Mono.just("")
                )
                .block();
    }

    private String convertAverageTemperatureToString(double temperature){
        if (temperature == -999){
            return "";
        }
        else return String.valueOf(temperature);
    }

    private String convertAverageWindSpeedToString(double windValue){
        if (windValue == -1){
            return "";
        }
        else return String.valueOf(windValue);
    }

}
