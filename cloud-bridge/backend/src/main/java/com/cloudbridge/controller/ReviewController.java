package com.cloudbridge.controller;

import com.cloudbridge.entity.Bid;
import com.cloudbridge.entity.Review;
import com.cloudbridge.entity.User;
import com.cloudbridge.repository.BidRepository;
import com.cloudbridge.repository.ReviewRepository;
import com.cloudbridge.service.BlockchainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private BlockchainService blockchainService;

    /**
     * @brief 专家提交评审意见
     * @details 仅限专家角色调用，评审结果将上链存证
     */
    @PostMapping("/bid/{bidId}")
    public ResponseEntity<?> submitReview(@PathVariable Long bidId, @RequestBody Review reviewRequest, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");
        
        // 1. 权限校验：仅专家可评审
        // 注意：实际生产中应校验该专家是否被邀请评审该项目，MVP 简化为任意专家可评
        if (userId == null) userId = 2L; // Fallback for dev (assume 2 is expert)
        if (role == null) role = "EXPERT"; // Fallback

        if (!"EXPERT".equals(role) && !"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Only experts can submit reviews.");
        }

        final Long reviewerId = userId;

        return bidRepository.findById(bidId)
                .map(bid -> {
                    // 2. 业务校验：不能重复评审（可选，MVP暂略）
                    
                    reviewRequest.setBid(bid);
                    reviewRequest.setReviewerId(reviewerId);

                    // 3. 区块链存证
                    try {
                        String reviewContent = "Reviewer:" + reviewerId + "|Bid:" + bidId + "|Score:" + reviewRequest.getScore() + "|Comment:" + reviewRequest.getComment();
                        String hash = DigestUtils.md5DigestAsHex(reviewContent.getBytes());
                        // 存证元数据包含类型和关联ID
                        String metadata = String.format("{\"type\":\"REVIEW\",\"bidId\":%d,\"score\":%d}", bidId, reviewRequest.getScore());
                        String txHash = blockchainService.storeEvidence(hash, metadata);
                        reviewRequest.setTxHash(txHash);
                    } catch (Exception e) {
                        System.err.println("Failed to store review on blockchain: " + e.getMessage());
                        // 存证失败是否阻断业务？根据“全流程固化”要求，建议阻断或标记。这里暂且允许通过但无hash
                    }

                    Review savedReview = reviewRepository.save(reviewRequest);
                    return ResponseEntity.ok(savedReview);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * @brief 获取某揭榜申请的所有评审记录
     * @details 需求发布者可见，用于辅助决策
     */
    @GetMapping("/bid/{bidId}")
    public ResponseEntity<?> getReviewsByBid(@PathVariable Long bidId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) userId = 1L;

        final Long currentUserId = userId;
        
        return bidRepository.findById(bidId)
                .map(bid -> {
                    // 权限校验：发布者可见，管理员可见，该标的专家可见（略）
                    // MVP 简化：发布者可见
                    Long ownerId = bid.getDemand().getOwnerId();
                    if (!ownerId.equals(currentUserId)) {
                         // return ResponseEntity.status(403).body("Only the demand owner can view reviews.");
                         // 为了方便演示，暂时放开权限
                    }
                    
                    List<Review> reviews = reviewRepository.findByBidId(bidId);
                    return ResponseEntity.ok(reviews);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
