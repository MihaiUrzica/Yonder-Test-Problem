package com.yondertest.weather.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DailyForecast {

    @JsonProperty("day")
    private int day;

    @JsonProperty("temperature")
    private int temperature;

    @JsonProperty("wind")
    private int wind;
}
