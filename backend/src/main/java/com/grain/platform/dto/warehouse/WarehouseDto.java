package com.grain.platform.dto.warehouse;

public record WarehouseDto(
        Long id,
        String warehouseCode,
        String warehouseName,
        String location,
        Double capacityTon,
        String managerName,
        String status
) {
}
