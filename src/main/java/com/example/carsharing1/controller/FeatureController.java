package com.example.carsharing1.controller;

import com.example.carsharing1.dto.FeatureDto;
import com.example.carsharing1.entity.Feature;
import com.example.carsharing1.repository.FeatureRepository;
import jakarta.validation.Valid;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/features")
@RequiredArgsConstructor
public class FeatureController {

    private final FeatureRepository featureRepository;

    @GetMapping
    public ResponseEntity<List<FeatureDto>> getAllFeatures() {
        List<FeatureDto> features = featureRepository.findAll().stream()
                .sorted(Comparator.comparing(Feature::getId))
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(features);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeatureDto> getFeatureById(@PathVariable Long id) {
        return featureRepository.findById(id)
                .map(feature -> ResponseEntity.ok(toDto(feature)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<FeatureDto> createFeature(@Valid @RequestBody FeatureDto featureDto) {
        Feature feature = toEntity(featureDto);
        feature.setId(null);
        Feature savedFeature = featureRepository.save(feature);
        return new ResponseEntity<>(toDto(savedFeature), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FeatureDto> updateFeature(
            @PathVariable Long id,
            @Valid @RequestBody FeatureDto featureDto) {
        return featureRepository.findById(id)
                .map(feature -> {
                    feature.setName(featureDto.getName());
                    feature.setDescription(featureDto.getDescription());
                    feature.setIcon(featureDto.getIcon());
                    return ResponseEntity.ok(toDto(featureRepository.save(feature)));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deleteFeature(@PathVariable Long id) {
        return featureRepository.findById(id)
                .map(feature -> {
                    feature.getCars().forEach(car -> car.getFeatures().remove(feature));
                    featureRepository.delete(feature);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());

    }

    private FeatureDto toDto(Feature feature) {
        return new FeatureDto(
                feature.getId(),
                feature.getName(),
                feature.getDescription(),
                feature.getIcon());
    }

    private Feature toEntity(FeatureDto dto) {
        Feature feature = new Feature();
        feature.setId(dto.getId());
        feature.setName(dto.getName());
        feature.setDescription(dto.getDescription());
        feature.setIcon(dto.getIcon());
        return feature;
    }
}
