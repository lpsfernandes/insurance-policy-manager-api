package io.manager.policy.domain.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_products")
public class Product {

    @Id
    private Long id;

}
