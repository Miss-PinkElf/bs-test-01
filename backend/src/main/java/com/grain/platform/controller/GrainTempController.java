package com.grain.platform.controller;

import com.grain.platform.common.ApiResponse;
import com.grain.platform.common.PageResult;
import com.grain.platform.dto.grain.GrainTempImportResultDto;
import com.grain.platform.dto.grain.GrainTempRecordFilterOptionsDto;
import com.grain.platform.dto.grain.GrainTempRecordItemDto;
import com.grain.platform.dto.grain.GrainTempRecordUpsertRequest;
import com.grain.platform.dto.grain.GrainTempSummaryItemDto;
import com.grain.platform.security.AccessControlService;
import com.grain.platform.security.CurrentUserContext;
import com.grain.platform.service.GrainTempService;
import com.grain.platform.vo.common.IdVO;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/grain-temp")
public class GrainTempController {

    private final GrainTempService grainTempService;
    private final AccessControlService accessControlService;

    public GrainTempController(GrainTempService grainTempService, AccessControlService accessControlService) {
        this.grainTempService = grainTempService;
        this.accessControlService = accessControlService;
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<GrainTempImportResultDto> importData(
            @RequestHeader(value = "X-Demo-Username", required = false) String username,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        CurrentUserContext currentUser = accessControlService.requireCurrentUser(username);
        accessControlService.assertWarehouseWriteAccess(currentUser, currentUser.warehouseId());
        return ApiResponse.success(grainTempService.importData(file, currentUser.userId(), currentUser.warehouseId()));
    }

    @GetMapping("/import/template")
    public ResponseEntity<byte[]> downloadTemplate(
            @RequestHeader(value = "X-Demo-Username", required = false) String username
    ) throws IOException {
        accessControlService.requireCurrentUser(username);
        byte[] content = grainTempService.getImportTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=grain-temp-fixed-template.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(content);
    }

    @PostMapping("/records")
    public ApiResponse<IdVO> createRecord(
            @RequestHeader(value = "X-Demo-Username", required = false) String username,
            @Valid @RequestBody GrainTempRecordUpsertRequest request
    ) {
        CurrentUserContext currentUser = accessControlService.requireCurrentUser(username);
        accessControlService.assertWarehouseWriteAccess(currentUser, request.warehouseId());
        return ApiResponse.success(grainTempService.createRecord(request, currentUser.userId()));
    }

    @PutMapping("/records/{id}")
    public ApiResponse<IdVO> updateRecord(
            @RequestHeader(value = "X-Demo-Username", required = false) String username,
            @PathVariable Long id,
            @Valid @RequestBody GrainTempRecordUpsertRequest request
    ) {
        CurrentUserContext currentUser = accessControlService.requireCurrentUser(username);
        accessControlService.assertWarehouseWriteAccess(currentUser, request.warehouseId());
        return ApiResponse.success(grainTempService.updateRecord(id, request, currentUser.userId()));
    }

    @DeleteMapping("/records/{id}")
    public ApiResponse<Void> deleteRecord(
            @RequestHeader(value = "X-Demo-Username", required = false) String username,
            @PathVariable Long id
    ) {
        CurrentUserContext currentUser = accessControlService.requireCurrentUser(username);
        accessControlService.assertWarehouseWriteAccess(currentUser, grainTempService.getRecordWarehouseId(id));
        grainTempService.deleteRecord(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/records/filter-options")
    public ApiResponse<GrainTempRecordFilterOptionsDto> listRecordFilterOptions(
            @RequestHeader(value = "X-Demo-Username", required = false) String username,
            @RequestParam(required = false) Long warehouseId
    ) {
        CurrentUserContext currentUser = accessControlService.requireCurrentUser(username);
        return ApiResponse.success(grainTempService.recordFilterOptions(
                accessControlService.resolveWarehouseScope(currentUser, warehouseId)
        ));
    }

    @GetMapping("/records")
    public ApiResponse<PageResult<GrainTempRecordItemDto>> listRecords(
            @RequestHeader(value = "X-Demo-Username", required = false) String username,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(required = false) String zoneCode,
            @RequestParam(required = false) Integer layerNo,
            @RequestParam(required = false) Integer pointNo,
            @RequestParam(required = false) BigDecimal tempMin,
            @RequestParam(required = false) BigDecimal tempMax,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        CurrentUserContext currentUser = accessControlService.requireCurrentUser(username);
        return ApiResponse.success(grainTempService.listRecordPage(
                accessControlService.resolveWarehouseScope(currentUser, warehouseId),
                startTime,
                endTime,
                zoneCode,
                layerNo,
                pointNo,
                tempMin,
                tempMax,
                keyword,
                pageNum,
                pageSize
        ));
    }

    @GetMapping("/summaries")
    public ApiResponse<List<GrainTempSummaryItemDto>> listSummaries(
            @RequestHeader(value = "X-Demo-Username", required = false) String username,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime
    ) {
        CurrentUserContext currentUser = accessControlService.requireCurrentUser(username);
        return ApiResponse.success(grainTempService.listSummaries(
                accessControlService.resolveWarehouseScope(currentUser, warehouseId),
                startTime,
                endTime
        ));
    }

    // 汇总全量接口给图表序列，分页接口给表格，两边共享同一套 service 过滤口径。
    @GetMapping("/summaries/page")
    public ApiResponse<PageResult<GrainTempSummaryItemDto>> listSummaryPage(
            @RequestHeader(value = "X-Demo-Username", required = false) String username,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(required = false) String warningLevel,
            @RequestParam(required = false) BigDecimal tempMin,
            @RequestParam(required = false) BigDecimal tempMax,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        CurrentUserContext currentUser = accessControlService.requireCurrentUser(username);
        return ApiResponse.success(grainTempService.listSummaryPage(
                accessControlService.resolveWarehouseScope(currentUser, warehouseId),
                startTime,
                endTime,
                warningLevel,
                tempMin,
                tempMax,
                keyword,
                pageNum,
                pageSize
        ));
    }
}
