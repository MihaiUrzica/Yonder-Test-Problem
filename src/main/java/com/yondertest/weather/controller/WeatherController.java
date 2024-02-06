package com.yondertest.weather.controller;

import com.yondertest.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping("/getWeatherForSingleCity/{cityName}")
    public Mono<ResponseEntity<WeatherAPIResponse>> getWeatherForSingleCity(@PathVariable String cityName) throws Exception {
        return Mono.just(ResponseEntity.ok(weatherService.getWeatherAPIResponseForCity(cityName)));
    }


    @GetMapping("/getWeatherForCityList/{cities}")
    public ResponseEntity<Flux<CityAvgTempAndWindSpeed>> getWeatherForCityList(@PathVariable List<String> cities) {
        Flux<String> stringFlux = Flux.fromIterable(cities);

        Flux<ResponseEntity<WeatherAPIResponse>> weatherAPIResponseFlux = stringFlux.flatMap(cityName -> {
            try {
                return getWeatherForSingleCity(cityName);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });

        return new ResponseEntity<>(weatherService.getCityAvgTempAndWindSpeedFlux(weatherAPIResponseFlux), HttpStatus.OK);
    }



}
