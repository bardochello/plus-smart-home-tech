package ru.yandex.practicum.warehouse.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.interaction.dto.DimensionDto;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DimensionEmbeddable {
    private double width;
    private double height;
    private double depth;

    public DimensionEmbeddable(DimensionDto dto) {
        this.width = dto.getWidth();
        this.height = dto.getHeight();
        this.depth = dto.getDepth();
    }

    public DimensionDto toDto() {
        DimensionDto dto = new DimensionDto();
        dto.setWidth(width);
        dto.setHeight(height);
        dto.setDepth(depth);
        return dto;
    }
}
