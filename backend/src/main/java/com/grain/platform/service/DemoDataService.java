package com.grain.platform.service;

import com.grain.platform.dto.dashboard.DashboardOverviewResponse;
import com.grain.platform.dto.sensor.SensorDataCreateRequest;
import com.grain.platform.dto.sensor.SensorDataPointDto;
import com.grain.platform.dto.warehouse.WarehouseDto;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

@Service
public class DemoDataService {

    private final AtomicLong sensorIdGenerator = new AtomicLong(1000);
    private final List<WarehouseDto> warehouses = new ArrayList<>();
    private final List<SensorDataPointDto> sensorData = new ArrayList<>();

    public DemoDataService() {
        warehouses.add(new WarehouseDto(1L, "WH-A01", "一号粮仓", "北区 1 栋", 500, "张三", "RUNNING"));
        warehouses.add(new WarehouseDto(2L, "WH-B02", "二号粮仓", "北区 2 栋", 650, "李四", "RUNNING"));
        warehouses.add(new WarehouseDto(3L, "WH-C03", "三号粮仓", "南区 1 栋", 720, "王五", "MAINTAINING"));

        seedSeries(1L, "一号粮仓", "temperature", 24.2);
        seedSeries(1L, "一号粮仓", "humidity", 56.0);
        seedSeries(2L, "二号粮仓", "temperature", 25.1);
        seedSeries(2L, "二号粮仓", "humidity", 58.5);
    }

    public DashboardOverviewResponse getOverview() {
        return new DashboardOverviewResponse(
                warehouses.size(),
                sensorData.size(),
                2,
                List.of("一号粮仓温度接近预警阈值", "二号粮仓湿度波动较大")
        );
    }

    public List<WarehouseDto> listWarehouses() {
        return warehouses.stream().sorted(Comparator.comparing(WarehouseDto::id)).toList();
    }

    public WarehouseDto createWarehouse(WarehouseDto request) {
        long id = warehouses.stream().mapToLong(WarehouseDto::id).max().orElse(0L) + 1;
        WarehouseDto created = new WarehouseDto(
                id,
                request.code(),
                request.name(),
                request.location(),
                request.capacityTon(),
                request.managerName(),
                request.status() == null ? "RUNNING" : request.status().toUpperCase(Locale.ROOT)
        );
        warehouses.add(created);
        return created;
    }

    public List<SensorDataPointDto> querySensorData(Long warehouseId, String metricType) {
        return sensorData.stream()
                .filter(item -> warehouseId == null || warehouseId.equals(item.warehouseId()))
                .filter(item -> metricType == null || metricType.isBlank() || metricType.equalsIgnoreCase(item.metricType()))
                .sorted(Comparator.comparing(SensorDataPointDto::collectedAt))
                .toList();
    }

    public SensorDataPointDto createSensorData(SensorDataCreateRequest request) {
        String warehouseName = warehouses.stream()
                .filter(item -> item.id().equals(request.warehouseId()))
                .findFirst()
                .map(WarehouseDto::name)
                .orElse("未知粮仓");

        SensorDataPointDto created = new SensorDataPointDto(
                sensorIdGenerator.incrementAndGet(),
                request.warehouseId(),
                warehouseName,
                request.metricType().toLowerCase(Locale.ROOT),
                request.metricValue(),
                request.collectedAt() == null ? LocalDateTime.now() : request.collectedAt()
        );
        sensorData.add(created);
        return created;
    }

    private void seedSeries(Long warehouseId, String warehouseName, String metricType, double baseValue) {
        LocalDateTime start = LocalDateTime.now().minusHours(11);
        IntStream.range(0, 12).forEach(index -> {
            double currentValue = baseValue + Math.sin(index / 2.0) * 1.2 + (index * 0.15);
            sensorData.add(new SensorDataPointDto(
                    sensorIdGenerator.incrementAndGet(),
                    warehouseId,
                    warehouseName,
                    metricType,
                    Math.round(currentValue * 10.0) / 10.0,
                    start.plusHours(index)
            ));
        });
    }
}
