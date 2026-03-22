package ru.yandex.practicum.shoppingstore.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.interaction.dto.enums.ProductCategory;
import ru.yandex.practicum.interaction.dto.enums.ProductState;
import ru.yandex.practicum.shoppingstore.model.Product;

public interface ProductRepository extends JpaRepository<Product, String> {
    Page<Product> findByProductCategoryAndProductState(
            ProductCategory category, ProductState state, Pageable pageable);
}
