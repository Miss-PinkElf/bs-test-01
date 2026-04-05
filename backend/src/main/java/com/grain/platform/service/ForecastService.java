package com.grain.platform.service;

import com.grain.platform.dto.prediction.PredictionPointDto;
import com.grain.platform.dto.sensor.SensorDataPointDto;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ForecastService {

    public List<PredictionPointDto> predict(List<SensorDataPointDto> history, int futureSteps) {
        List<SensorDataPointDto> sorted = history.stream()
                .sorted(Comparator.comparing(SensorDataPointDto::collectedAt))
                .toList();

        List<PredictionPointDto> result = new ArrayList<>();
        sorted.forEach(item -> result.add(new PredictionPointDto(item.collectedAt(), item.metricValue(), null)));

        if (sorted.isEmpty()) {
            return result;
        }

        double slope = calculateSlope(sorted);
        double base = sorted.get(sorted.size() - 1).metricValue();
        LocalDateTime lastTime = sorted.get(sorted.size() - 1).collectedAt();

        for (int step = 1; step <= futureSteps; step++) {
            result.add(new PredictionPointDto(
                    lastTime.plusHours(step),
                    null,
                    round(base + slope * step)
            ));
        }

        return result;
    }

    private double calculateSlope(List<SensorDataPointDto> points) {
        if (points.size() < 2) {
            return 0.0;
        }

        int n = points.size();
        double sumX = 0.0;
        double sumY = 0.0;
        double sumXY = 0.0;
        double sumX2 = 0.0;

        for (int index = 0; index < n; index++) {
            double x = index + 1;
            double y = points.get(index).metricValue();
            sumX += x;
            sumY += y;
            sumXY += x * y;
            sumX2 += x * x;
        }

        double denominator = n * sumX2 - sumX * sumX;
        if (denominator == 0.0) {
            return 0.0;
        }

        return (n * sumXY - sumX * sumY) / denominator;
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
