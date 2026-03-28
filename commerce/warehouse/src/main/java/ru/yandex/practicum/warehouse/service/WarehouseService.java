package ru.yandex.practicum.warehouse.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.interaction.dto.*;
import ru.yandex.practicum.warehouse.exception.*;
import ru.yandex.practicum.warehouse.model.DimensionEmbeddable;
import ru.yandex.practicum.warehouse.model.WarehouseProduct;
import ru.yandex.practicum.warehouse.repository.WarehouseProductRepository;

import java.util.Map;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WarehouseService {

    private final WarehouseProductRepository repository;

    private static final String[] ADDRESSES = {"ADDRESS_1", "ADDRESS_2"};
    private static final String CURRENT_ADDRESS = ADDRESSES[new Random().nextInt(ADDRESSES.length)];

    public void newProductInWarehouse(NewProductInWarehouseRequest request) {
        if (repository.existsById(request.getProductId())) {
            throw new SpecifiedProductAlreadyInWarehouseException(request.getProductId());
        }

        WarehouseProduct product = new WarehouseProduct();
        product.setProductId(request.getProductId());
        product.setDimension(new DimensionEmbeddable(request.getDimension()));
        product.setWeight(request.getWeight());
        product.setFragile(request.isFragile());
        product.setQuantity(0);

        repository.save(product);
    }

    public BookedProductsDto checkProductQuantityEnoughForShoppingCart(ShoppingCartDto cart) {
        Map<String, Long> requested = cart.getProducts();
        if (requested == null || requested.isEmpty()) {
            return new BookedProductsDto(0.0, 0.0, false);
        }

        double totalWeight = 0.0;
        double totalVolume = 0.0;
        boolean hasFragile = false;

        for (Map.Entry<String, Long> entry : requested.entrySet()) {
            String productId = entry.getKey();
            long needed = entry.getValue();

            WarehouseProduct p = repository.findById(productId)
                    .orElseThrow(() -> new ProductInShoppingCartNotInWarehouse("Товар " + productId + " не найден на складе"));

            if (p.getQuantity() < needed) {
                throw new ProductInShoppingCartLowQuantityInWarehouse(
                        "Недостаточно товара " + productId + ": есть " + p.getQuantity() + ", нужно " + needed);
            }

            totalWeight += p.getWeight() * needed;

            DimensionEmbeddable d = p.getDimension();
            double volume = d.getWidth() * d.getHeight() * d.getDepth() * needed;
            totalVolume += volume;

            if (p.isFragile()) hasFragile = true;
        }

        return new BookedProductsDto(totalWeight, totalVolume, hasFragile);
    }

    public void addProductToWarehouse(AddProductToWarehouseRequest request) {
        WarehouseProduct p = repository.findById(request.getProductId())
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException(request.getProductId()));

        p.setQuantity(p.getQuantity() + request.getQuantity());
        repository.save(p);
    }

    public AddressDto getWarehouseAddress() {
        AddressDto dto = new AddressDto();
        dto.setCountry(CURRENT_ADDRESS);
        dto.setCity(CURRENT_ADDRESS);
        dto.setStreet(CURRENT_ADDRESS);
        dto.setHouse(CURRENT_ADDRESS);
        dto.setFlat(CURRENT_ADDRESS);
        return dto;
    }

    public BookedProductsDto assemblyProductsForOrder(AssemblyProductsForOrderRequest request) {
        Map<String, Long> products = request.getProducts();

        double totalWeight = 0.0;
        double totalVolume = 0.0;
        boolean hasFragile = false;

        for (Map.Entry<String, Long> entry : products.entrySet()) {
            String productId = entry.getKey();
            long needed = entry.getValue();

            WarehouseProduct p = repository.findById(productId)
                    .orElseThrow(() -> new ProductInShoppingCartLowQuantityInWarehouse("Товар не найден: " + productId));

            if (p.getQuantity() < needed) {
                throw new ProductInShoppingCartLowQuantityInWarehouse("Недостаточно товара " + productId);
            }

            p.setQuantity(p.getQuantity() - needed);
            repository.save(p);

            totalWeight += p.getWeight() * needed;
            DimensionEmbeddable d = p.getDimension();
            totalVolume += d.getWidth() * d.getHeight() * d.getDepth() * needed;

            if (p.isFragile()) hasFragile = true;
        }

        return new BookedProductsDto(totalWeight, totalVolume, hasFragile);
    }

    public void shippedToDelivery(ShippedToDeliveryRequest request) {
        log.info("Товары заказа {} переданы в доставку с id {}", request.getOrderId(), request.getDeliveryId());
    }

    public void acceptReturn(Map<String, Long> products) {
        for (Map.Entry<String, Long> entry : products.entrySet()) {
            String productId = entry.getKey();
            long quantityToReturn = entry.getValue();

            WarehouseProduct p = repository.findById(productId)
                    .orElseThrow(() -> new NoSpecifiedProductInWarehouseException(productId));

            p.setQuantity(p.getQuantity() + quantityToReturn);
            repository.save(p);
        }
    }
}