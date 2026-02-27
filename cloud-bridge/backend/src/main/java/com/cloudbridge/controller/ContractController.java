package com.cloudbridge.controller;

import com.cloudbridge.entity.Bid;
import com.cloudbridge.entity.Contract;
import com.cloudbridge.entity.Demand;
import com.cloudbridge.repository.BidRepository;
import com.cloudbridge.repository.ContractRepository;
import com.cloudbridge.repository.DemandRepository;
import com.cloudbridge.service.BlockchainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/contracts")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ContractController {

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private DemandRepository demandRepository;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private BlockchainService blockchainService;

    /**
     * @brief 为指定需求生成合同草稿
     * @details 仅限需求发布者调用，需存在已采纳的揭榜方案
     */
    @PostMapping("/demand/{demandId}")
    public ResponseEntity<?> createContract(@PathVariable Long demandId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) userId = 1L;

        final Long currentUserId = userId;
        
        Optional<Demand> demandOpt = demandRepository.findById(demandId);
        if (!demandOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        Demand demand = demandOpt.get();
        if (!demand.getOwnerId().equals(currentUserId)) {
            return ResponseEntity.status(403).body("Only the demand owner can create a contract.");
        }

        // Check if contract already exists
        if (contractRepository.findByDemandId(demandId).isPresent()) {
            return ResponseEntity.badRequest().body("Contract already exists for this demand.");
        }

        // Find accepted bid
        Optional<Bid> acceptedBidOpt = bidRepository.findByDemandId(demandId).stream()
                .filter(b -> b.getStatus() == Bid.Status.ACCEPTED)
                .findFirst();

        if (!acceptedBidOpt.isPresent()) {
            return ResponseEntity.badRequest().body("No accepted bid found for this demand.");
        }

        Bid acceptedBid = acceptedBidOpt.get();

        // Generate contract content
        String contractContent = generateContractTemplate(demand, acceptedBid);

        Contract contract = new Contract();
        contract.setDemand(demand);
        contract.setBid(acceptedBid);
        contract.setContent(contractContent);
        contract.setStatus(Contract.Status.PENDING_SIGN);
        
        return ResponseEntity.ok(contractRepository.save(contract));
    }

    /**
     * @brief 获取合同详情
     */
    @GetMapping("/demand/{demandId}")
    public ResponseEntity<?> getContractByDemand(@PathVariable Long demandId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) userId = 1L;
        
        // In a real app, check permissions (Owner or Bidder)
        // For MVP, allow access if contract exists
        return contractRepository.findByDemandId(demandId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * @brief 签署合同
     * @details 双方签署后自动上链存证
     */
    @PostMapping("/{contractId}/sign")
    public ResponseEntity<?> signContract(@PathVariable Long contractId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) userId = 1L;

        final Long currentUserId = userId;

        return contractRepository.findById(contractId)
                .map(contract -> {
                    boolean isOwner = contract.getDemand().getOwnerId().equals(currentUserId);
                    boolean isBidder = contract.getBid().getBidderId().equals(currentUserId);

                    if (!isOwner && !isBidder) {
                        return ResponseEntity.status(403).body("You are not a party to this contract.");
                    }

                    if (isOwner) {
                        if (contract.getOwnerSignedAt() != null) {
                             return ResponseEntity.badRequest().body("Owner already signed.");
                        }
                        contract.setOwnerSignedAt(LocalDateTime.now());
                    } else {
                        if (contract.getBidderSignedAt() != null) {
                             return ResponseEntity.badRequest().body("Bidder already signed.");
                        }
                        contract.setBidderSignedAt(LocalDateTime.now());
                    }

                    // Check if both signed
                    if (contract.getOwnerSignedAt() != null && contract.getBidderSignedAt() != null) {
                        contract.setStatus(Contract.Status.SIGNED);
                        
                        // Blockchain Evidence
                        try {
                            String contentHash = DigestUtils.md5DigestAsHex(contract.getContent().getBytes());
                            String metadata = String.format("{\"type\":\"CONTRACT\",\"demandId\":%d,\"bidId\":%d,\"status\":\"SIGNED\"}", 
                                    contract.getDemand().getId(), contract.getBid().getId());
                            String txHash = blockchainService.storeEvidence(contentHash, metadata);
                            contract.setTxHash(txHash);
                        } catch (Exception e) {
                            System.err.println("Failed to store contract on blockchain: " + e.getMessage());
                        }
                        
                        // Update Demand Status to MATCHING (or COMPLETED? Let's keep MATCHING as in progress)
                        // Or maybe move to COMPLETED if signing implies immediate effect for MVP?
                        // Let's stick to MATCHING as "In Progress"
                    }

                    return ResponseEntity.ok(contractRepository.save(contract));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private String generateContractTemplate(Demand demand, Bid bid) {
        StringBuilder sb = new StringBuilder();
        sb.append("技术开发委托合同\n\n");
        sb.append("甲方（委托方）：").append(demand.getInstitution() != null ? demand.getInstitution() : "发布方").append("\n");
        sb.append("乙方（受托方）：").append("揭榜专家/团队 (ID: ").append(bid.getBidderId()).append(")\n\n");
        sb.append("第一条 项目名称：").append(demand.getTitle()).append("\n");
        sb.append("第二条 技术目标：").append(demand.getDescription()).append("\n");
        sb.append("第三条 履行期限：").append(demand.getDeadline()).append("之前\n");
        sb.append("第四条 费用及支付：本合同总金额为人民币 ").append(bid.getQuote() != null ? bid.getQuote() : "面议").append(" 元。\n");
        sb.append("第五条 验收标准：以甲方发布的详细技术指标及乙方投标方案为准。\n\n");
        sb.append("本合同一式两份，甲乙双方各执一份，自双方电子签署之日起生效。\n");
        sb.append("签署日期：").append(java.time.LocalDate.now()).append("\n");
        return sb.toString();
    }
}
