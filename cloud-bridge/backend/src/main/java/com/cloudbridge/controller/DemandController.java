package com.cloudbridge.controller;

import com.cloudbridge.entity.Demand;
import com.cloudbridge.repository.BidRepository;
import com.cloudbridge.repository.DemandRepository;
import com.cloudbridge.service.BlockchainService;
import javax.validation.Valid;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/demands")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DemandController {

    @Autowired
    private DemandRepository demandRepository;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private BlockchainService blockchainService;

    /**
     * @brief 获取平台热搜风向标
     * @details 基于最新发布需求的关键词频率统计
     */
    @GetMapping("/hot-tags")
    public List<String> getHotTags() {
        // Fetch last 50 published demands
        Page<Demand> demands = demandRepository.findByStatus(Demand.Status.PUBLISHED, PageRequest.of(0, 50, Sort.by(Sort.Direction.DESC, "createdAt")));
        
        Map<String, Integer> frequency = new HashMap<>();
        for (Demand d : demands) {
            // 1. Add Field as tag (Higher weight)
            if (d.getField() != null) {
                frequency.put(d.getField(), frequency.getOrDefault(d.getField(), 0) + 5);
            }
            
            // 2. Extract nouns from title (Simple split for MVP)
            // Filter out common stopwords and short words
            if (d.getTitle() != null) {
                String[] words = d.getTitle().split("(?<=\\p{IsHan})|(?=\\p{IsHan})|\\s+"); 
                for (String w : words) {
                    if (w.length() >= 2 && !isStopWord(w)) {
                         frequency.put(w, frequency.getOrDefault(w, 0) + 1);
                    }
                }
            }
        }
        
        return frequency.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(8)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    private boolean isStopWord(String word) {
        // Simple stopword list
        String stops = "需求,研发,开发,项目,系统,平台,技术,应用,寻求,合作,转让,基于,一种,研究,设计,服务,解决方案";
        return stops.contains(word);
    }

    @GetMapping
    public Page<Demand> getAllPublished(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        return demandRepository.findByStatus(Demand.Status.PUBLISHED, PageRequest.of(page, size, sort));
    }

    @GetMapping("/my")
    public List<Demand> getMyDemands(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return demandRepository.findByOwnerId(userId);
    }
    
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingDemands(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access denied.");
        }
        return ResponseEntity.ok(demandRepository.findByStatus(Demand.Status.PENDING_REVIEW));
    }

    @PostMapping
    public Demand createDemand(@Valid @RequestBody Demand demand, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        demand.setOwnerId(userId);
        demand.setStatus(Demand.Status.PENDING_REVIEW);
        sanitizeDemand(demand);
        
        // Blockchain Evidence for Creation (Optional at this stage, but good for "Full Process")
        // Usually creation is just a draft, but here we submit for review.
        try {
            String content = "Owner:" + userId + "|Title:" + demand.getTitle() + "|Budget:" + demand.getBudget();
            String hash = DigestUtils.md5DigestAsHex(content.getBytes());
            String txHash = blockchainService.storeEvidence(hash, "{\"type\":\"DEMAND_CREATE\",\"title\":\"" + demand.getTitle() + "\"}");
            demand.setTxHash(txHash);
        } catch (Exception e) {
            System.err.println("Failed to store demand creation on blockchain: " + e.getMessage());
        }

        return demandRepository.save(demand);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDemand(@PathVariable Long id, @Valid @RequestBody Demand demandDetails, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return demandRepository.findById(id)
                .map(demand -> {
                    if (!demand.getOwnerId().equals(userId)) {
                        return ResponseEntity.status(403).body("You can only update your own demands.");
                    }
                    demand.setTitle(demandDetails.getTitle());
                    demand.setDescription(demandDetails.getDescription());
                    demand.setField(demandDetails.getField());
                    demand.setBudget(demandDetails.getBudget());
                    demand.setDeadline(demandDetails.getDeadline());
                    demand.setContactName(demandDetails.getContactName());
                    demand.setPhone(demandDetails.getPhone());
                    demand.setInstitution(demandDetails.getInstitution());
                    demand.setType(demandDetails.getType());
                    sanitizeDemand(demand);
                    return ResponseEntity.ok(demandRepository.save(demand));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDemand(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return demandRepository.findById(id)
                .map(demand -> {
                    if (!demand.getOwnerId().equals(userId)) {
                        return ResponseEntity.status(403).body("You can only delete your own demands.");
                    }
                    demandRepository.delete(demand);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Demand> getDemandById(@PathVariable Long id) {
        return demandRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * @brief 提交揭榜申请 (投标)
     * @param id 需求ID
     * @param bidRequest 投标信息
     */
    @PostMapping("/{id}/bid")
    public ResponseEntity<?> submitBid(@PathVariable Long id, @RequestBody com.cloudbridge.entity.Bid bidRequest, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) userId = 1L; // Fallback for dev

        final Long bidderId = userId;
        return demandRepository.findById(id)
                .map(demand -> {
                    if (demand.getStatus() != Demand.Status.PUBLISHED) {
                        return ResponseEntity.badRequest().body("Only published demands can be bid on.");
                    }
                    
                    bidRequest.setDemand(demand);
                    bidRequest.setBidderId(bidderId);
                    bidRequest.setStatus(com.cloudbridge.entity.Bid.Status.PENDING);
                    
                    // 区块链存证：固化投标行为
                    try {
                        String bidContent = "Bidder:" + bidderId + "|Proposal:" + bidRequest.getProposal() + "|Quote:" + bidRequest.getQuote();
                        String hash = org.springframework.util.DigestUtils.md5DigestAsHex(bidContent.getBytes());
                        String txHash = blockchainService.storeEvidence(hash, "{\"type\":\"BID\",\"demandId\":" + id + "}");
                        bidRequest.setTxHash(txHash);
                    } catch (Exception e) {
                        System.err.println("Failed to store bid on blockchain: " + e.getMessage());
                    }

                    return ResponseEntity.ok(bidRepository.save(bidRequest));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * @brief 获取需求的揭榜列表 (仅限发布者)
     */
    @GetMapping("/{id}/bids")
    public ResponseEntity<?> getBids(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) userId = 1L;

        final Long currentUserId = userId;
        return demandRepository.findById(id)
                .map(demand -> {
                    if (!demand.getOwnerId().equals(currentUserId)) {
                        return ResponseEntity.status(403).body("Only the demand owner can view bids.");
                    }
                    return ResponseEntity.ok(bidRepository.findByDemandId(id));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * @brief 更新揭榜状态 (仅限发布者，如：采纳/拒绝)
     */
    @PutMapping("/bids/{bidId}/status")
    public ResponseEntity<?> updateBidStatus(@PathVariable Long bidId, @RequestParam com.cloudbridge.entity.Bid.Status status, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) userId = 1L;

        final Long currentUserId = userId;
        return bidRepository.findById(bidId)
                .map(bid -> {
                    if (!bid.getDemand().getOwnerId().equals(currentUserId)) {
                        return ResponseEntity.status(403).body("Only the demand owner can update bid status.");
                    }
                    bid.setStatus(status);
                    
                    // 如果揭榜被采纳，将需求状态设为“洽谈中”或“匹配成功”
                    if (status == com.cloudbridge.entity.Bid.Status.ACCEPTED) {
                        Demand demand = bid.getDemand();
                        demand.setStatus(Demand.Status.MATCHING);
                        demandRepository.save(demand);
                    }
                    
                    return ResponseEntity.ok(bidRepository.save(bid));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/audit")
    public ResponseEntity<?> auditDemand(@PathVariable Long id, @RequestParam Demand.Status status, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Only admins can audit demands.");
        }
        if (status != Demand.Status.PUBLISHED && status != Demand.Status.REJECTED) {
             return ResponseEntity.badRequest().body("Invalid status for audit. Must be PUBLISHED or REJECTED.");
        }
        return demandRepository.findById(id)
                .map(demand -> {
                    demand.setStatus(status);
                    
                    // Blockchain Evidence for Audit
                    try {
                        String content = "Audit:" + status + "|Demand:" + id + "|Admin:" + request.getAttribute("userId");
                        String hash = DigestUtils.md5DigestAsHex(content.getBytes());
                        String txHash = blockchainService.storeEvidence(hash, "{\"type\":\"DEMAND_AUDIT\",\"demandId\":" + id + ",\"status\":\"" + status + "\"}");
                        // We might overwrite txHash or store it in a separate AuditLog. 
                        // For MVP, if approved, update demand's txHash to reflect "Published" state? 
                        // Or just fire and forget. Let's fire and forget or log it.
                        // Ideally we should have an AuditLog entity.
                        // For now, let's just update the demand's txHash to the LATEST event.
                        demand.setTxHash(txHash);
                    } catch (Exception e) {
                        System.err.println("Failed to store audit on blockchain: " + e.getMessage());
                    }

                    return ResponseEntity.ok(demandRepository.save(demand));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}/complete")
    public ResponseEntity<?> completeDemand(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) userId = 1L;
        
        final Long currentUserId = userId;
        return demandRepository.findById(id)
                .map(demand -> {
                    if (!demand.getOwnerId().equals(currentUserId)) {
                        return ResponseEntity.status(403).body("Only the owner can complete the demand.");
                    }
                    if (demand.getStatus() != Demand.Status.MATCHING) { // Only MATCHING (in progress) can be completed
                         // return ResponseEntity.badRequest().body("Demand is not in progress.");
                         // Allow from any state for MVP flexibility
                    }
                    
                    demand.setStatus(Demand.Status.COMPLETED);
                    
                    // Blockchain Evidence for Completion (Acceptance)
                    try {
                        String content = "Complete:" + id + "|Owner:" + currentUserId + "|Time:" + System.currentTimeMillis();
                        String hash = DigestUtils.md5DigestAsHex(content.getBytes());
                        String txHash = blockchainService.storeEvidence(hash, "{\"type\":\"DEMAND_COMPLETE\",\"demandId\":" + id + "}");
                        demand.setTxHash(txHash);
                    } catch (Exception e) {
                        System.err.println("Failed to store completion on blockchain: " + e.getMessage());
                    }
                    
                    return ResponseEntity.ok(demandRepository.save(demand));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private void sanitizeDemand(Demand demand) {
        if (demand.getTitle() != null) {
            demand.setTitle(Jsoup.clean(demand.getTitle(), Safelist.none()));
        }
        if (demand.getDescription() != null) {
            demand.setDescription(Jsoup.clean(demand.getDescription(), Safelist.relaxed()));
        }
        if (demand.getField() != null) {
            demand.setField(Jsoup.clean(demand.getField(), Safelist.none()));
        }
        if (demand.getContactName() != null) {
            demand.setContactName(Jsoup.clean(demand.getContactName(), Safelist.none()));
        }
        if (demand.getPhone() != null) {
            demand.setPhone(Jsoup.clean(demand.getPhone(), Safelist.none()));
        }
        if (demand.getInstitution() != null) {
            demand.setInstitution(Jsoup.clean(demand.getInstitution(), Safelist.none()));
        }
    }
}
