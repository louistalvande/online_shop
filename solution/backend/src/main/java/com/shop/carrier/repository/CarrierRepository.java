package com.shop.carrier.repository;

import com.shop.carrier.entity.Carrier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/** Repository for {@link Carrier} entities. */
public interface CarrierRepository extends JpaRepository<Carrier, UUID> {
}
