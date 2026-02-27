import json
import random

# Fields and their keywords for generation
FIELDS_DATA = {
    "人工智能": ["深度学习", "神经网络", "计算机视觉", "NLP", "强化学习", "大模型", "知识图谱", "智能客服", "人脸识别", "自动驾驶"],
    "新材料": ["石墨烯", "碳纤维", "纳米材料", "柔性电子", "超导材料", "生物降解塑料", "气凝胶", "液态金属", "高分子"],
    "新能源": ["锂电池", "固态电池", "氢能", "太阳能", "风能", "储能系统", "燃料电池", "智慧电网", "光伏组件"],
    "智能制造": ["工业机器人", "3D打印", "数字孪生", "柔性制造", "自动化产线", "预测性维护", "工业物联网", "智能传感器"],
    "大数据": ["数据挖掘", "分布式存储", "实时计算", "数据湖", "隐私计算", "数据治理", "Hadoop", "Spark", "数据可视化"],
    "环保科技": ["污水处理", "废气治理", "碳捕获", "土壤修复", "环境监测", "固废回收", "节能减排", "绿色建筑"],
    "生物医药": ["基因编辑", "靶向药物", "免疫治疗", "干细胞", "疫苗研发", "医疗器械", "生物芯片", "精准医疗"],
    "物联网": ["边缘计算", "5G通信", "RFID", "智能家居", "智慧城市", "车联网", "传感器网络", "NB-IoT"],
    "金融科技": ["区块链", "智能合约", "移动支付", "量化交易", "风控模型", "供应链金融", "数字货币", "征信系统"],
    "量子通信": ["量子密钥", "量子计算", "量子纠缠", "量子网络", "量子加密", "量子传感"]
}

ADJECTIVES = ["高性能", "新一代", "基于AI的", "智能", "分布式", "高精度", "低成本", "绿色", "自适应", "超大规模", "可信", "跨平台"]
NOUNS = ["系统", "平台", "解决方案", "装置", "关键技术", "模组", "机器人", "工具包", "服务框架", "检测仪"]

def generate_achievements(count=600):
    achievements = []
    
    for i in range(count):
        field = random.choice(list(FIELDS_DATA.keys()))
        keywords = FIELDS_DATA[field]
        main_keyword = random.choice(keywords)
        sub_keyword = random.choice(keywords)
        
        adj = random.choice(ADJECTIVES)
        noun = random.choice(NOUNS)
        
        title = f"{adj}{main_keyword}{noun}"
        if random.random() > 0.7:
             title = f"{main_keyword}与{sub_keyword}融合{noun}"
             
        description = f"本成果基于{main_keyword}技术，结合{sub_keyword}算法，解决了传统{field}领域的痛点。具有{adj}、高效率等特点，适用于多种场景。已在多个企业试点应用，效果显著。"
        
        achievement = {
            "title": title,
            "description": description,
            "field": field,
            "maturity": random.choice(["实验室研发", "中试阶段", "产业化阶段", "成熟应用"]),
            "price": random.randint(10, 1000) * 10000,
            "ownerId": random.randint(1, 100),
            "status": "PUBLISHED"
        }
        achievements.append(achievement)
        
    return achievements

if __name__ == "__main__":
    data = generate_achievements(600)
    
    # Save to file
    file_path = "e:/数据要素大赛作品/cloud-bridge/backend/data/raw_achievements.json"
    with open(file_path, "w", encoding="utf-8") as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
        
    print(f"Successfully generated {len(data)} achievements to {file_path}")
