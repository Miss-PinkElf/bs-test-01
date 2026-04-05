package com.grain.platform.dto.warehouse;

public record WarehouseDto(
        Long id,
        String code,
        String name,
        String location,
        Integer capacityTon,
        String managerName,
        String status
) {
}
