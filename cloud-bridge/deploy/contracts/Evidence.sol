pragma solidity ^0.4.24;

contract Evidence {
    struct EvidenceData {
        string hash;
        address owner;
        uint256 timestamp;
        string metadata;
        string evidenceType;
        string signerId;
        string signature;
    }

    mapping(string => EvidenceData) private evidenceStore;
    event EvidenceStored(string hash, address indexed owner, uint256 timestamp);

    function storeEvidence(string memory hash, string memory metadata, string memory evidenceType, string memory signerId, string memory signature) public {
        require(bytes(evidenceStore[hash].hash).length == 0, "Evidence already exists");

        evidenceStore[hash] = EvidenceData({
            hash: hash,
            owner: msg.sender,
            timestamp: block.timestamp,
            metadata: metadata,
            evidenceType: evidenceType,
            signerId: signerId,
            signature: signature
        });

        emit EvidenceStored(hash, msg.sender, block.timestamp);
    }

    function getEvidence(string memory hash) public view returns (address, uint256, string memory, string memory, string memory, string memory) {
        require(bytes(evidenceStore[hash].hash).length != 0, "Evidence not found");
        EvidenceData memory data = evidenceStore[hash];
        return (data.owner, data.timestamp, data.metadata, data.evidenceType, data.signerId, data.signature);
    }

    function verifyEvidence(string memory hash) public view returns (bool) {
        return bytes(evidenceStore[hash].hash).length != 0;
    }
}
