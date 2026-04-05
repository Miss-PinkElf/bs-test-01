package com.grain.platform.controller;

import com.grain.platform.dto.prediction.PredictionPointDto;
import com.grain.platform.dto.prediction.PredictionRequest;
import com.grain.platform.dto.sensor.SensorDataPointDto;
import com.grain.platform.service.DemoDataService;
import com.grain.platform.service.ForecastService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/predictions")
public class PredictionController {

    private final DemoDataService demoDataService;
    private final ForecastService forecastService;

    public PredictionController(DemoDataService demoDataService, ForecastService forecastService) {
        this.demoDataService = demoDataService;
        this.forecastService = forecastService;
    }

    @PostMapping("/temperature")
    public List<PredictionPointDto> predictTemperature(@Valid @RequestBody PredictionRequest request) {
        List<SensorDataPointDto> history = demoDataService.querySensorData(request.warehouseId(), request.metricType());
        return forecastService.predict(history, request.futureSteps());
    }
}
