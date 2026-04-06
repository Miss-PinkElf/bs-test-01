package com.grain.platform.controller;

import com.grain.platform.common.ApiResponse;
import com.grain.platform.dto.prediction.PredictionRequest;
import com.grain.platform.dto.prediction.PredictionTaskResponse;
import com.grain.platform.service.PredictionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/predictions")
public class PredictionController {

    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @PostMapping("/temperature")
    public ApiResponse<PredictionTaskResponse> predictTemperature(@Valid @RequestBody PredictionRequest request) {
        return ApiResponse.success(predictionService.predict(request));
    }

    @GetMapping("/tasks")
    public ApiResponse<List<PredictionTaskResponse>> listTasks() {
        return ApiResponse.success(predictionService.listTasks());
    }

    @GetMapping("/tasks/{taskId}")
    public ApiResponse<PredictionTaskResponse> getTask(@PathVariable Long taskId) {
        return ApiResponse.success(predictionService.getTask(taskId));
    }
}
