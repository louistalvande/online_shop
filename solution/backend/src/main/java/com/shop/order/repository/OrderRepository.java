package com.shop.order.repository;

import com.shop.order.entity.Order;
import com.shop.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** JPA repository for {@link Order} entities. */
public interface OrderRepository extends JpaRepository<Order, UUID> {

    /**
     * Returns all orders for a given buyer, newest first.
     *
     * @param buyerId the buyer account UUID
     * @return buyer's orders ordered by creation date descending
     */
    List<Order> findByBuyerIdOrderByCreatedAtDesc(UUID buyerId);

    /**
     * Finds an order by its UUID and buyer, enforcing ownership.
     *
     * @param id      the order UUID
     * @param buyerId the buyer account UUID
     * @return the order, or empty if not found or not owned by this buyer
     */
    Optional<Order> findByIdAndBuyerId(UUID id, UUID buyerId);

    /**
     * Returns all orders for a given buyer in a given set of statuses.
     *
     * @param buyerId  the buyer account UUID
     * @param statuses the statuses to filter by
     * @return matching orders ordered by creation date descending
     */
    List<Order> findByBuyerIdAndStatusInOrderByCreatedAtDesc(UUID buyerId, List<OrderStatus> statuses);

    /**
     * Returns all orders that reference a given carrier.
     *
     * @param carrierId the carrier UUID
     * @return orders using that carrier
     */
    List<Order> findByCarrierId(UUID carrierId);
}
