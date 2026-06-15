#!/bin/bash
# install-ik-analyzer.sh
# 安装 IK 分词器插件到 Elasticsearch
# 适用于 Docker 部署和本地开发环境

set -e

ES_VERSION="8.11.0"
IK_PLUGIN_VERSION="8.11.0"
IK_PLUGIN_URL="https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v${IK_PLUGIN_VERSION}/elasticsearch-analysis-ik-${IK_PLUGIN_VERSION}.zip"
ES_HOME="${ES_HOME:-./elasticsearch-8.11.0}"
PLUGIN_DIR="${ES_HOME}/plugins/analysis-ik"

echo "=== 安装 IK 分词器 for Elasticsearch ${ES_VERSION} ==="

# 检查 ES 是否运行中
if curl -s http://localhost:9200 > /dev/null 2>&1; then
    echo "警告: Elasticsearch 正在运行，建议先停止后再安装插件"
    read -p "是否继续? (y/N): " confirm
    if [ "$confirm" != "y" ] && [ "$confirm" != "Y" ]; then
        echo "安装取消"
        exit 0
    fi
fi

# 创建插件目录
mkdir -p "${PLUGIN_DIR}"

# 下载 IK 插件
echo "下载 IK 分词器..."
if command -v wget &> /dev/null; then
    wget -q --show-progress -O /tmp/ik-plugin.zip "${IK_PLUGIN_URL}"
elif command -v curl &> /dev/null; then
    curl -L -o /tmp/ik-plugin.zip "${IK_PLUGIN_URL}"
else
    echo "错误: 需要 wget 或 curl 来下载插件"
    exit 1
fi

# 解压插件
echo "解压插件..."
if command -v unzip &> /dev/null; then
    unzip -q -o /tmp/ik-plugin.zip -d "${PLUGIN_DIR}"
else
    echo "错误: 需要 unzip 来解压插件"
    exit 1
fi

# 清理临时文件
rm -f /tmp/ik-plugin.zip

# 验证安装
if [ -f "${PLUGIN_DIR}/elasticsearch-analysis-ik-${IK_PLUGIN_VERSION}.jar" ]; then
    echo "✅ IK 分词器安装成功!"
    echo "   插件目录: ${PLUGIN_DIR}"
    echo ""
    echo "请重启 Elasticsearch 使插件生效"
else
    echo "❌ 安装失败，请检查插件文件"
    exit 1
fi

# 列出插件内容
echo ""
echo "插件内容:"
ls -la "${PLUGIN_DIR}/"
