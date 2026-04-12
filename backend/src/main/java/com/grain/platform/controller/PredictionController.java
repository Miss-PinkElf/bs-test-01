package com.grain.platform.controller;

import com.grain.platform.common.ApiResponse;
import com.grain.platform.common.ForbiddenException;
import com.grain.platform.common.PageResult;
import com.grain.platform.dto.prediction.PredictionBatchDeleteRequest;
import com.grain.platform.dto.prediction.PredictionRequest;
import com.grain.platform.dto.prediction.PredictionTaskResponse;
import com.grain.platform.security.AccessControlService;
import com.grain.platform.security.CurrentUserContext;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/predictions")
public class PredictionController {

    private static final Logger log = LoggerFactory.getLogger(PredictionController.class);

    private final PredictionService predictionService;
    private final AccessControlService accessControlService;

    public PredictionController(PredictionService predictionService, AccessControlService accessControlService) {
        this.predictionService = predictionService;
        this.accessControlService = accessControlService;
    }

    @PostMapping
    public ApiResponse<PredictionTaskResponse> predict(
            @RequestHeader(value = "X-Demo-Username", required = false) String username,
            @Valid @RequestBody PredictionRequest request
    ) {
        CurrentUserContext currentUser = accessControlService.requireCurrentUser(username);
        accessControlService.assertWarehouseWriteAccess(currentUser, request.warehouseId());
        log.info("调用滚动预测接口，warehouseId={}, metricCode={}, targetType={}, forecastDays={}",
                request.warehouseId(), request.metricCode(), request.targetType(), request.forecastDays());
        PredictionTaskResponse response = predictionService.predict(request, currentUser.userId());
        int resultCount = response.resultList() == null ? 0 : response.resultList().size();
        log.info("滚动预测成功，taskId={}, metricCode={}, riskLevel={}, resultCount={}",
                response.taskId(), response.metricCode(), response.riskLevel(), resultCount);
        return ApiResponse.success(response);
    }

    @GetMapping("/tasks")
    public ApiResponse<List<PredictionTaskResponse>> listTasks(
            @RequestHeader(value = "X-Demo-Username", required = false) String username
    ) {
        CurrentUserContext currentUser = accessControlService.requireCurrentUser(username);
        log.info("调用预测任务列表接口");
        List<PredictionTaskResponse> response = predictionService.listTasks(
                accessControlService.resolveWarehouseScope(currentUser, null)
        );
        log.info("获取预测任务列表成功，count={}", response.size());
        return ApiResponse.success(response);
    }

    // 保留全量列表接口给旧调用兼容，管理端新表格统一走下面的分页接口。
    @GetMapping("/tasks/page")
    public ApiResponse<PageResult<PredictionTaskResponse>> listTaskPage(
            @RequestHeader(value = "X-Demo-Username", required = false) String username,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer pageNum,
            @RequestParam(required = false) Integer pageSize
    ) {
        CurrentUserContext currentUser = accessControlService.requireCurrentUser(username);
        log.info("调用预测任务分页列表接口，keyword={}, pageNum={}, pageSize={}", keyword, pageNum, pageSize);
        PageResult<PredictionTaskResponse> response = predictionService.listTaskPage(
                keyword,
                pageNum,
                pageSize,
                accessControlService.resolveWarehouseScope(currentUser, null)
        );
        log.info("获取预测任务分页列表成功，pageNum={}, pageSize={}, total={}",
                response.pageNum(), response.pageSize(), response.total());
        return ApiResponse.success(response);
    }

    @GetMapping("/tasks/{taskId}")
    public ApiResponse<PredictionTaskResponse> getTask(
            @RequestHeader(value = "X-Demo-Username", required = false) String username,
            @PathVariable Long taskId
    ) {
        CurrentUserContext currentUser = accessControlService.requireCurrentUser(username);
        log.info("调用预测任务详情接口，taskId={}", taskId);
        PredictionTaskResponse response = predictionService.getTask(taskId);
        ensureReadableTask(currentUser, response.warehouseId());
        int resultCount = response.resultList() == null ? 0 : response.resultList().size();
        log.info("获取预测任务详情成功，taskId={}, resultCount={}", response.taskId(), resultCount);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/tasks/{taskId}")
    public ApiResponse<Void> deleteTask(
            @RequestHeader(value = "X-Demo-Username", required = false) String username,
            @PathVariable Long taskId
    ) {
        CurrentUserContext currentUser = accessControlService.requireCurrentUser(username);
        accessControlService.assertWarehouseWriteAccess(currentUser, predictionService.getTask(taskId).warehouseId());
        log.info("调用删除预测任务接口，taskId={}", taskId);
        predictionService.deleteTask(taskId);
        log.info("删除预测任务成功，taskId={}", taskId);
        return ApiResponse.success(null);
    }

    @PostMapping("/tasks/batch-delete")
    public ApiResponse<Void> batchDeleteTasks(
            @RequestHeader(value = "X-Demo-Username", required = false) String username,
            @Valid @RequestBody PredictionBatchDeleteRequest request
    ) {
        CurrentUserContext currentUser = accessControlService.requireCurrentUser(username);
        if (request.taskIds() != null) {
            for (Long taskId : request.taskIds()) {
                accessControlService.assertWarehouseWriteAccess(currentUser, predictionService.getTask(taskId).warehouseId());
            }
        }
        log.info("调用批量删除预测任务接口，count={}", request.taskIds() == null ? 0 : request.taskIds().size());
        predictionService.deleteTasksBatch(request.taskIds());
        log.info("批量删除预测任务成功");
        return ApiResponse.success(null);
    }

    private void ensureReadableTask(CurrentUserContext currentUser, Long warehouseId) {
        if (accessControlService.isWarehouseManager(currentUser)
                && !Objects.equals(currentUser.warehouseId(), warehouseId)) {
            throw new ForbiddenException("仓库管理员仅可查看所属仓库预测任务");
        }
    }
}
