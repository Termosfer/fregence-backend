package com.fregence.fregence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "perfume_variants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerfumeVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer ml;          // 30, 50, 75, 100, 150
    private Double price;        // Bu ölçünün əsas qiyməti
    private Double discountPrice; // Bu ölçünün endirimli qiyməti
    private Integer stock;       // Bu ölçüdən neçə dənə qalıb

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfume_id")
    @JsonBackReference // Perfume ilə sonsuz döngü yaranmasın deyə
    private Perfume perfume;
}