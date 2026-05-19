package com.shop.order.repository;

import com.shop.order.entity.OrderLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/** JPA repository for {@link OrderLine} entities. */
public interface OrderLineRepository extends JpaRepository<OrderLine, UUID> {
}
