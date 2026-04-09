package com.grain.platform.controller;

import com.grain.platform.common.ApiResponse;
import com.grain.platform.common.PageResult;
import com.grain.platform.dto.grain.GrainTempImportResultDto;
import com.grain.platform.dto.grain.GrainTempRecordItemDto;
import com.grain.platform.dto.grain.GrainTempRecordUpsertRequest;
import com.grain.platform.dto.grain.GrainTempSummaryItemDto;
import com.grain.platform.service.GrainTempService;
import com.grain.platform.vo.common.IdVO;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/grain-temp")
public class GrainTempController {

    private final GrainTempService grainTempService;

    public GrainTempController(GrainTempService grainTempService) {
        this.grainTempService = grainTempService;
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<GrainTempImportResultDto> importData(@RequestParam("file") MultipartFile file) throws IOException {
        return ApiResponse.success(grainTempService.importData(file));
    }

    @GetMapping("/import/template")
    public ResponseEntity<byte[]> downloadTemplate() throws IOException {
        byte[] content = grainTempService.getImportTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=grain-temp-fixed-template.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(content);
    }

    @PostMapping("/records")
    public ApiResponse<IdVO> createRecord(@Valid @RequestBody GrainTempRecordUpsertRequest request) {
        return ApiResponse.success(grainTempService.createRecord(request));
    }

    @PutMapping("/records/{id}")
    public ApiResponse<IdVO> updateRecord(@PathVariable Long id,
                                          @Valid @RequestBody GrainTempRecordUpsertRequest request) {
        return ApiResponse.success(grainTempService.updateRecord(id, request));
    }

    @DeleteMapping("/records/{id}")
    public ApiResponse<Void> deleteRecord(@PathVariable Long id) {
        grainTempService.deleteRecord(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/records")
    public ApiResponse<PageResult<GrainTempRecordItemDto>> listRecords(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(required = false) String zoneCode,
            @RequestParam(required = false) Integer layerNo,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        return ApiResponse.success(grainTempService.listRecordPage(
                warehouseId,
                startTime,
                endTime,
                zoneCode,
                layerNo,
                pageNum,
                pageSize
        ));
    }

    @GetMapping("/summaries")
    public ApiResponse<List<GrainTempSummaryItemDto>> listSummaries(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime
    ) {
        return ApiResponse.success(grainTempService.listSummaries(warehouseId, startTime, endTime));
    }
}
