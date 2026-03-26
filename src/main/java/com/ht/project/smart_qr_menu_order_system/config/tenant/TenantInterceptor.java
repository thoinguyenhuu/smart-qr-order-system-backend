package com.ht.project.smart_qr_menu_order_system.config.tenant;

import com.ht.project.smart_qr_menu_order_system.exception.AppErrorCode;
import com.ht.project.smart_qr_menu_order_system.exception.AppException;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.Filter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;
import java.util.UUID;

/**
 * Resolve restaurant UUID from request and enable Hibernate tenant filter.
 */
@Component
@RequiredArgsConstructor
public class TenantInterceptor implements HandlerInterceptor {
    private final EntityManager entityManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String restaurantId = resolveRestaurantId(request);
        if (restaurantId == null || restaurantId.isBlank()) {
            return true;
        }

        validateUuid(restaurantId);
        TenantContext.setTenantId(restaurantId);

        Session session = entityManager.unwrap(Session.class);
        Filter filter = session.enableFilter("tenantFilter");
        filter.setParameter("tenantId", restaurantId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) {
        try {
            Session session = entityManager.unwrap(Session.class);
            if (session.getEnabledFilter("tenantFilter") != null) {
                session.disableFilter("tenantFilter");
            }
        } finally {
            TenantContext.clear();
        }
    }

    @SuppressWarnings("unchecked")
    private String resolveRestaurantId(HttpServletRequest request) {
        Object pathVariables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (pathVariables instanceof Map<?, ?> variables) {
            Object restaurantId = variables.get("restaurantId");
            if (restaurantId != null) {
                return restaurantId.toString();
            }
        }
        return request.getHeader("X-Restaurant-Id");
    }

    private void validateUuid(String value) {
        try {
            UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            throw new AppException(AppErrorCode.INVALID_TENANT);
        }
    }
}
