package io.manager.policy.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_clients")
public class Client {
    @Id
    private String id;
}
