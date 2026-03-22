package ru.yandex.practicum.shoppingstore.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.interaction.dto.*;
import ru.yandex.practicum.interaction.dto.enums.ProductCategory;
import ru.yandex.practicum.interaction.dto.enums.ProductState;
import ru.yandex.practicum.shoppingstore.exception.ProductNotFoundException;
import ru.yandex.practicum.shoppingstore.model.Product;
import ru.yandex.practicum.shoppingstore.repository.ProductRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository repository;

    public Page<ProductDto> getProducts(ProductCategory category, int page, int size, List<String> sort) {
        Pageable pageable = createPageable(page, size, sort);
        Page<Product> products = repository.findByProductCategoryAndProductState(
                category, ProductState.ACTIVE, pageable);
        return products.map(this::toDto);
    }

    public ProductDto createNewProduct(ProductDto dto) {
        Product product = toEntity(dto);
        product.setProductId(null);
        product.setProductState(ProductState.ACTIVE);
        return toDto(repository.save(product));
    }

    public ProductDto updateProduct(ProductDto dto) {
        Product product = repository.findById(dto.getProductId())
                .orElseThrow(ProductNotFoundException::new);
        updateEntity(product, dto);
        return toDto(repository.save(product));
    }

    public boolean removeProductFromStore(String productId) {
        return repository.findById(productId)
                .map(product -> {
                    product.setProductState(ProductState.DEACTIVATE);
                    repository.save(product);
                    return true;
                })
                .orElse(false);
    }

    public boolean setProductQuantityState(SetProductQuantityStateRequest request) {
        return repository.findById(request.getProductId())
                .map(product -> {
                    product.setQuantityState(request.getQuantityState());
                    repository.save(product);
                    return true;
                })
                .orElse(false);
    }

    public ProductDto getProduct(String productId) {
        Product product = repository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);
        return toDto(product);
    }

    private ProductDto toDto(Product p) {
        ProductDto dto = new ProductDto();
        dto.setProductId(p.getProductId());
        dto.setProductName(p.getProductName());
        dto.setDescription(p.getDescription());
        dto.setImageSrc(p.getImageSrc());
        dto.setQuantityState(p.getQuantityState());
        dto.setProductState(p.getProductState());
        dto.setProductCategory(p.getProductCategory());
        dto.setPrice(p.getPrice());
        return dto;
    }

    private Product toEntity(ProductDto dto) {
        Product p = new Product();
        p.setProductId(dto.getProductId());
        p.setProductName(dto.getProductName());
        p.setDescription(dto.getDescription());
        p.setImageSrc(dto.getImageSrc());
        p.setQuantityState(dto.getQuantityState());
        p.setProductState(dto.getProductState());
        p.setProductCategory(dto.getProductCategory());
        p.setPrice(dto.getPrice());
        return p;
    }

    private void updateEntity(Product p, ProductDto dto) {
        if (dto.getProductName() != null) p.setProductName(dto.getProductName());
        if (dto.getDescription() != null) p.setDescription(dto.getDescription());
        if (dto.getImageSrc() != null) p.setImageSrc(dto.getImageSrc());
        if (dto.getQuantityState() != null) p.setQuantityState(dto.getQuantityState());
        if (dto.getProductCategory() != null) p.setProductCategory(dto.getProductCategory());
        if (dto.getPrice() > 0) p.setPrice(dto.getPrice());
    }

    private Pageable createPageable(int page, int size, List<String> sortParams) {
        if (sortParams == null || sortParams.isEmpty()) {
            return PageRequest.of(page, size);
        }

        List<Sort.Order> orders = sortParams.stream()
                .map(param -> {
                    String[] parts = param.split(",");
                    String property = parts[0];
                    Sort.Direction dir = parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                            ? Sort.Direction.DESC : Sort.Direction.ASC;
                    return new Sort.Order(dir, property);
                })
                .toList();

        return PageRequest.of(page, size, Sort.by(orders));
    }
}
