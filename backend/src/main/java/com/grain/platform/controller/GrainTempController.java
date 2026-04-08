package com.grain.platform.controller;

import com.grain.platform.common.ApiResponse;
import com.grain.platform.dto.grain.GrainTempImportResultDto;
import com.grain.platform.dto.grain.GrainTempRecordItemDto;
import com.grain.platform.dto.grain.GrainTempSummaryItemDto;
import com.grain.platform.service.GrainTempService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
    public ResponseEntity<byte[]> downloadTemplate() {
        byte[] content = grainTempService.getImportTemplate().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=grain-temp-template.csv")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(content);
    }

    @GetMapping("/records")
    public ApiResponse<List<GrainTempRecordItemDto>> listRecords(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(required = false) String zoneCode,
            @RequestParam(required = false) Integer layerNo
    ) {
        return ApiResponse.success(grainTempService.listRecords(warehouseId, startTime, endTime, zoneCode, layerNo));
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
