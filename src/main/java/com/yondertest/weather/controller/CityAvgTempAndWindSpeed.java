package com.yondertest.weather.controller;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class CityAvgTempAndWindSpeed {

    private String name;
    private String temperature;
    private String wind;

}
