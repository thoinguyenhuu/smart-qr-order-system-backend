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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Dish belongs to one category and one restaurant tenant.
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "dishes")
@Entity
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
public class Dish extends TenantAwareEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    Category category;

    @Column(name = "name", nullable = false)
    String name;

    @Column(name = "description", columnDefinition = "TEXT")
    String description;

    @Column(name = "price", nullable = false)
    BigDecimal price;

    @Column(name = "image_url")
    String imageUrl;

    @Column(name = "is_available", nullable = false)
    Boolean available;

    @Column(name = "sold_out_on")
    LocalDate soldOutOn;

    public boolean isSoldOutToday(ZoneId zoneId) {
        return soldOutOn != null && soldOutOn.equals(LocalDate.now(zoneId));
    }

    public boolean isAvailableForOrder(ZoneId zoneId) {
        return Boolean.TRUE.equals(available) && !isSoldOutToday(zoneId);
    }
}
