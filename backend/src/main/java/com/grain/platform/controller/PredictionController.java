package com.grain.platform.controller;

import com.grain.platform.common.ApiResponse;
import com.grain.platform.dto.prediction.PredictionBatchDeleteRequest;
import com.grain.platform.dto.prediction.PredictionRequest;
import com.grain.platform.dto.prediction.PredictionTaskResponse;
import com.grain.platform.service.PredictionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    @PostMapping
    public ApiResponse<PredictionTaskResponse> predict(@Valid @RequestBody PredictionRequest request) {
        log.info("调用滚动预测接口，warehouseId={}, metricCode={}, targetType={}, forecastDays={}",
                request.warehouseId(), request.metricCode(), request.targetType(), request.forecastDays());
        PredictionTaskResponse response = predictionService.predict(request);
        int resultCount = response.resultList() == null ? 0 : response.resultList().size();
        log.info("滚动预测成功，taskId={}, metricCode={}, riskLevel={}, resultCount={}",
                response.taskId(), response.metricCode(), response.riskLevel(), resultCount);
        return ApiResponse.success(response);
    }

    @GetMapping("/tasks")
    public ApiResponse<List<PredictionTaskResponse>> listTasks() {
        log.info("调用预测任务列表接口");
        List<PredictionTaskResponse> response = predictionService.listTasks();
        log.info("获取预测任务列表成功，count={}", response.size());
        return ApiResponse.success(response);
    }

    @GetMapping("/tasks/{taskId}")
    public ApiResponse<PredictionTaskResponse> getTask(@PathVariable Long taskId) {
        log.info("调用预测任务详情接口，taskId={}", taskId);
        PredictionTaskResponse response = predictionService.getTask(taskId);
        int resultCount = response.resultList() == null ? 0 : response.resultList().size();
        log.info("获取预测任务详情成功，taskId={}, resultCount={}", response.taskId(), resultCount);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/tasks/{taskId}")
    public ApiResponse<Void> deleteTask(@PathVariable Long taskId) {
        log.info("调用删除预测任务接口，taskId={}", taskId);
        predictionService.deleteTask(taskId);
        log.info("删除预测任务成功，taskId={}", taskId);
        return ApiResponse.success(null);
    }

    @PostMapping("/tasks/batch-delete")
    public ApiResponse<Void> batchDeleteTasks(@Valid @RequestBody PredictionBatchDeleteRequest request) {
        log.info("调用批量删除预测任务接口，count={}", request.taskIds() == null ? 0 : request.taskIds().size());
        predictionService.deleteTasksBatch(request.taskIds());
        log.info("批量删除预测任务成功");
        return ApiResponse.success(null);
    }
}
