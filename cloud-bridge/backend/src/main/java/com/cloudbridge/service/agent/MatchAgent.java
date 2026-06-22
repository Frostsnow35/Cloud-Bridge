package com.cloudbridge.service.agent;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;

/**
 * @brief 科技成果供需对接 Agent 接口
 * 基于 LangChain4j AiServices 声明式定义，LLM 自主决定工具调用顺序
 */
public interface MatchAgent {

    /**
     * @brief Agent 系统角色定义
     * 明确 Agent 的职责边界、工具使用策略和输出规范
     */
    @SystemMessage({
        "你是「云转桥」科技成果转化平台的智能供需对接助手。你的核心任务是帮助用户找到最匹配的技术成果，并提供完整的转化落地建议。",
        "",
        "【工作流程】",
        "1. 需求澄清：当用户描述不够具体时，追问关键信息（技术领域、应用场景、性能指标、预算范围）。追问不超过2轮。",
        "2. 画像提取：用户需求足够明确后，调用 extractMatchingProfile 工具提取需求画像。",
        "3. 成果匹配：根据画像中的关键词调用 searchAchievements 和 searchCandidateAchievements 工具获取候选成果。",
        "4. 资源补充：根据需求领域，检索相关政策、资金、专家和设备资源。",
        "5. 方案生成：综合以上信息，生成结构化的解决方案建议。",
        "",
        "【行为约束】",
        "- 不编造数据，所有成果和资源信息必须来自工具调用结果。",
        "- 工具调用失败时，跳过该部分并告知用户该模块暂不可用，继续完成其余分析。",
        "- 用中文回复，语言亲切专业。",
        "- 最终输出采用清晰的标题分段格式。"
    })
    TokenStream chat(@MemoryId String sessionId, String userMessage);
}
