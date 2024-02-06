package com.yondertest.weather.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yondertest.weather.model.DailyForecast;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class WeatherAPIResponse {

     private String name;

     @JsonProperty("temperature")
     private int temperature;

     @JsonProperty("wind")
     private int wind;

     @JsonProperty("description")
     private String description;

     @JsonProperty("forecast")
     private List<DailyForecast> forecast;
}
