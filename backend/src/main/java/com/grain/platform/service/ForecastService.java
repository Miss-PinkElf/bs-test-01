package com.grain.platform.service;

import com.grain.platform.dto.prediction.PredictionPointDto;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ForecastService {

    public List<PredictionPointDto> predictDaily(List<PredictionPointDto> history, int forecastDays) {
        List<PredictionPointDto> sorted = history.stream()
                .sorted(Comparator.comparing(PredictionPointDto::time))
                .toList();

        if (sorted.isEmpty()) {
            return List.of();
        }

        double slope = calculateSlope(sorted);
        double base = sorted.get(sorted.size() - 1).value();
        LocalDateTime lastTime = sorted.get(sorted.size() - 1).time();
        List<PredictionPointDto> result = new ArrayList<>();

        for (int step = 1; step <= forecastDays; step++) {
            result.add(new PredictionPointDto(
                    lastTime.plusDays(step),
                    round(base + slope * step)
            ));
        }

        return result;
    }

    private double calculateSlope(List<PredictionPointDto> points) {
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
            double y = points.get(index).value();
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
        return Math.round(value * 100.0) / 100.0;
    }
}
