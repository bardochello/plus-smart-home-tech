package ru.yandex.practicum.warehouse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.warehouse.model.WarehouseProduct;

public interface WarehouseProductRepository extends JpaRepository<WarehouseProduct, String> {
}
