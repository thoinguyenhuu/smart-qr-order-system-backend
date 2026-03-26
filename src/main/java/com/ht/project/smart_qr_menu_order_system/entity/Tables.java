package com.ht.project.smart_qr_menu_order_system.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Filter;

/**
 * Restaurant table contains QR metadata for public ordering.
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "tables")
@Entity
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class Tables extends TenantAwareEntity {

    @Column(name = "table_number", nullable = false)
    int tableNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    Restaurant restaurant;

    @Column(name = "qr_code")
    String qrCode;

    @Column(name = "is_available", nullable = false)
    Boolean available;
}
