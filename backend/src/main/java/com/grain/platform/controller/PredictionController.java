package com.grain.platform.controller;

import com.grain.platform.common.ApiResponse;
import com.grain.platform.dto.prediction.PredictionRequest;
import com.grain.platform.dto.prediction.PredictionTaskResponse;
import com.grain.platform.service.PredictionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(PredictionController.class);

    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    // 执行温度预测。
    @PostMapping("/temperature")
    public ApiResponse<PredictionTaskResponse> predictTemperature(@Valid @RequestBody PredictionRequest request) {
        log.info("调用温度预测接口，warehouseId={}, metricCode={}, futureSteps={}", request.warehouseId(), request.metricCode(), request.futureSteps());
        PredictionTaskResponse response = predictionService.predict(request);
        int resultCount = response.resultList() == null ? 0 : response.resultList().size();
        log.info("温度预测成功，taskId={}, riskLevel={}, resultCount={}", response.taskId(), response.riskLevel(), resultCount);
        return ApiResponse.success(response);
    }

    // 查询预测任务列表。
    @GetMapping("/tasks")
    public ApiResponse<List<PredictionTaskResponse>> listTasks() {
        log.info("调用预测任务列表接口");
        List<PredictionTaskResponse> response = predictionService.listTasks();
        log.info("获取预测任务列表成功，count={}", response.size());
        return ApiResponse.success(response);
    }

    // 查询预测任务详情。
    @GetMapping("/tasks/{taskId}")
    public ApiResponse<PredictionTaskResponse> getTask(@PathVariable Long taskId) {
        log.info("调用预测任务详情接口，taskId={}", taskId);
        PredictionTaskResponse response = predictionService.getTask(taskId);
        int resultCount = response.resultList() == null ? 0 : response.resultList().size();
        log.info("获取预测任务详情成功，taskId={}, resultCount={}", response.taskId(), resultCount);
        return ApiResponse.success(response);
    }
}
