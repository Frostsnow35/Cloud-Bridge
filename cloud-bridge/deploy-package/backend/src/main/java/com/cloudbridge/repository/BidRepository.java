package com.cloudbridge.repository;

import com.cloudbridge.entity.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @brief 揭榜申请仓库
 */
@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {
    List<Bid> findByDemandId(Long demandId);
    List<Bid> findByBidderId(Long bidderId);
}
