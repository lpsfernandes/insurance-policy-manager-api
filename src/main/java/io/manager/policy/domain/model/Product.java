package io.manager.policy.domain.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tb_products")
public class Product {

    @Id
    private Long id;

    @Column(name = "product_name", nullable = false, length = 150)
    private String name;

}
