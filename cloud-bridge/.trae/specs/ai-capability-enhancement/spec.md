# AI Capability Enhancement Spec

## Why
The current AI implementation lacks precision in categorization, logical reasoning, and depth of analysis required for the "Cloud Bridge" platform (as detailed in the competition report). Specifically:
- **Categorization Errors**: Technologies are often misclassified (e.g., "Carbon Fiber" as "Construction Material" instead of "New Material").
- **Weak Reasoning**: The inference paths (Chain of Thought) are unclear or disordered.
- **Generic Analysis**: Analysis of technical achievements lacks domain-specific depth.
- **Missing Data**: No labeled data exists for Supervised Fine-Tuning (SFT).

To address this, we will adopt a **Prompt Engineering + RAG (Retrieval-Augmented Generation)** approach (Option A) as the primary solution, while simultaneously generating synthetic data to enable future SFT (Option B).

## What Changes
- **Implement Comprehensive Data Schemas**: Define strict JSON schemas for all resource types (Policies, Funds, Equipment, Experts) and Knowledge Graph entities to guide data collection and AI output.
- **Enhance AIService with RAG**: Integrate Elasticsearch retrieval to provide relevant context (e.g., similar technologies, policy documents) to the LLM before generation.
- **Optimize Prompts with CoT**: Rewrite prompts to include "Chain of Thought" reasoning steps and domain-specific Few-Shot examples (e.g., specific to "New Materials", "Bio-industry").
- **Develop Data Synthesis Pipeline**: Create a script to generate high-quality "Input-Output" pairs using the improved RAG+Prompt pipeline, serving as the dataset for future SFT.

## Impact
- **Backend**: `AIService.java` will be significantly refactored to support RAG and advanced prompting.
- **Data Layer**: New Elasticsearch indices and data loading logic.
- **Future**: Prepared dataset for local model fine-tuning.

## Comprehensive Data Requirements (Based on Report)
The following data fields are required to support the platform's "One-Stop Resource Navigation" and "Deep Matching" capabilities.

### 1. Policy (政策)
- **id**: Unique Identifier
- **title**: Policy Title (e.g., "关于支持新材料产业发展的若干措施")
- **publish_date**: Date of publication
- **department**: Issuing Department (e.g., "工信部", "广东省科技厅")
- **content**: Full text or detailed summary
- **policy_type**: Type (e.g., "Subsidy/补贴", "Tax/税收", "License/资质", "Talent/人才")
- **industry**: Target Industries (Array, e.g., ["New Materials", "Bio-medicine"])
- **region**: Applicable Region (e.g., "Guangzhou", "Nationwide")
- **conditions**: Eligibility Conditions (Array of strings)
- **support_content**: Specific support details (e.g., "Maximum 5 million RMB grant")
- **deadline**: Application deadline
- **source_url**: Link to original document

### 2. Fund/Financial Product (资金/金融产品)
- **id**: Unique Identifier
- **name**: Product Name (e.g., "科创贷")
- **provider**: Institution (Bank, VC, Government)
- **fund_type**: Type (e.g., "Loan/贷款", "Equity/股权", "Grant/无偿资助")
- **amount_range**: Min/Max Amount
- **industry_focus**: Preferred Industries
- **requirements**: Application Requirements (Revenue, IP, Team size)
- **interest_rate**: Interest rate or equity share (if applicable)
- **contact_info**: Contact person/department

### 3. Equipment/Facility (设备/共享设施)
- **id**: Unique Identifier
- **name**: Equipment Name (e.g., "Cryo-EM/冷冻电镜")
- **facility_name**: Lab/Center Name (e.g., "Jiangmen Dual Carbon Lab")
- **owner**: Owning Institution (University, Company)
- **category**: Equipment Category (e.g., "Analysis", "Manufacturing", "Testing")
- **specs**: Technical Specifications (Key parameters)
- **availability**: Status (Available, Booked, Maintenance)
- **location**: Physical Address
- **service_content**: Services provided (e.g., "Sample analysis", "Self-service")
- **booking_url**: Link to booking system

### 4. Expert/Talent (专家/人才)
- **id**: Unique Identifier
- **name**: Full Name
- **title**: Job Title (e.g., "Professor", "Chief Engineer")
- **institution**: Affiliation
- **research_area**: Fields of expertise (Array)
- **achievements**: Key publications, patents, projects (Array)
- **collaboration_history**: Past industry collaborations (Summary)

### 5. Knowledge Graph Entity (图谱实体 - Tech/Demand)
- **id**: Unique Identifier
- **name**: Entity Name
- **type**: Entity Type (Demand, Category, SubCategory, Technology, Application, Organization, Person)
- **attributes**: JSON object for dynamic attributes (e.g., Maturity Level, TRL, Cost)
- **industry_chain**: Position in value chain (Upstream, Midstream, Downstream)

## ADDED Requirements
### Requirement: Enhanced RAG-based Analysis
The system SHALL retrieve relevant Policy/Fund/Equipment data from Elasticsearch based on the user's input and inject it into the LLM prompt to generate comprehensive analysis reports.

#### Scenario: User asks for "Carbon Fiber Funding"
- **WHEN** user inputs "Searching for funding for T800 carbon fiber production line"
- **THEN** system retrieves "New Material Industry Subsidy" and "Tech Loan" data
- **AND** system generates a response listing specific matching funds with application advice.

### Requirement: Synthetic Data Generation
The system SHALL provide a utility to generate `(User Input, Ideal Output)` pairs by running the enhanced RAG pipeline on a set of seed queries, saving the results in JSONL format for future SFT.
