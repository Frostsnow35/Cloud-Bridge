package com.cloudbridge.service.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @brief Agent 工具调用链日志记录器，输出统一格式的 [AGENT_TOOLCHAIN] 日志
 */
@Component
public class ToolChainLogger {

    private static final Logger log = LoggerFactory.getLogger(ToolChainLogger.class);

    /**
     * @brief 记录工具调用开始
     * @param sessionId 会话标识
     * @param toolName  工具名称
     * @param params    序列化后的参数摘要
     */
    public void logToolStart(String sessionId, String toolName, String params) {
        log.info("[AGENT_TOOLCHAIN] [{}] START {}({})", sessionId, toolName, params);
    }

    /**
     * @brief 记录工具调用结束
     * @param sessionId    会话标识
     * @param toolName     工具名称
     * @param durationMs   耗时（毫秒）
     * @param resultSummary 结果摘要（前100字符）
     */
    public void logToolEnd(String sessionId, String toolName, long durationMs, String resultSummary) {
        log.info("[AGENT_TOOLCHAIN] [{}] END {} → {} ({} ms)", sessionId, toolName, resultSummary, durationMs);
    }
}
