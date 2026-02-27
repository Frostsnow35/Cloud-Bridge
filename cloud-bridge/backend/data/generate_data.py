import json
import os

# Data Paths
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
ACHIEVEMENTS_PATH = os.path.join(BASE_DIR, "raw_achievements.json")
TECHNOLOGIES_PATH = os.path.join(BASE_DIR, "raw_technologies.json")
CYPHER_PATH = os.path.join(BASE_DIR, "../src/main/resources/cypher/import.cypher")

# New Data to Add (Biomedicine & New Materials focus)
NEW_ACHIEVEMENTS = [
    {
        "title": "高性能PEEK骨科植入材料",
        "description": "开发了新型碳纤维增强聚醚醚酮（CFR-PEEK）复合材料，具有优异的生物相容性和力学性能，弹性模量接近人体皮质骨，有效避免应力遮挡效应。适用于脊柱融合器、关节置换等骨科植入物。",
        "field": "新材料",
        "maturity": "TRL7",
        "price": 650000.0,
        "ownerId": 201,
        "status": "PUBLISHED"
    },
    {
        "title": "mRNA疫苗脂质纳米颗粒递送系统",
        "description": "针对mRNA疫苗的不稳定性，研发了新一代可电离脂质纳米颗粒（LNP）递送系统。该系统具有高包封率、低细胞毒性和优异的体内转染效率，并在小鼠模型中诱导了强烈的免疫应答，为传染病疫苗和肿瘤疫苗开发提供了关键技术支撑。",
        "field": "生物医药",
        "maturity": "TRL6",
        "price": 1200000.0,
        "ownerId": 202,
        "status": "PUBLISHED"
    },
    {
        "title": "可降解镁合金心血管支架",
        "description": "研制了一种具有良好生物相容性和可控降解速率的镁稀土合金心血管支架。通过表面微弧氧化和聚合物涂层改性，显著提高了支架的耐腐蚀性能和血液相容性。动物实验表明，支架植入后6个月内保持结构完整，12个月完全降解吸收。",
        "field": "新材料",
        "maturity": "TRL8",
        "price": 950000.0,
        "ownerId": 201,
        "status": "PUBLISHED"
    },
    {
        "title": "基于类器官的药物筛选平台",
        "description": "建立了包含肺癌、结直肠癌等多种肿瘤类型的患者来源类器官（PDO）库。利用高通量自动化成像系统，实现了抗肿瘤药物的快速筛选和药敏测试。该平台能更准确地预测药物临床疗效，为个性化精准医疗提供指导。",
        "field": "生物医药",
        "maturity": "TRL7",
        "price": 800000.0,
        "ownerId": 203,
        "status": "PUBLISHED"
    },
    {
        "title": "柔性钙钛矿太阳能电池",
        "description": "开发了一种基于低温溶液法制备的柔性钙钛矿太阳能电池。通过引入功能化添加剂优化钙钛矿结晶过程，显著提高了薄膜质量和器件稳定性。电池光电转换效率达到22%，且在弯曲半径5mm下循环1000次后效率保持率超过90%。",
        "field": "新能源",
        "maturity": "TRL5",
        "price": 550000.0,
        "ownerId": 204,
        "status": "PUBLISHED"
    },
    {
        "title": "智能响应水凝胶伤口敷料",
        "description": "设计了一种对pH值和温度双重响应的智能水凝胶伤口敷料。该敷料能根据伤口微环境变化自动调节药物释放速率，并具有良好的保湿、抗菌和止血功能。动物实验显示，该敷料能显著缩短伤口愈合时间，减少疤痕形成。",
        "field": "新材料",
        "maturity": "TRL6",
        "price": 300000.0,
        "ownerId": 202,
        "status": "PUBLISHED"
    },
    {
        "title": "阿尔茨海默病多靶点创新药物",
        "description": "针对阿尔茨海默病复杂的发病机制，设计合成了一类兼具乙酰胆碱酯酶抑制和抗氧化活性的多靶点化合物。体外和体内实验表明，候选药物DL-203能改善认知功能障碍，减少脑内Aβ沉积和Tau蛋白磷酸化，具有良好的成药潜力。",
        "field": "生物医药",
        "maturity": "TRL4",
        "price": 2500000.0,
        "ownerId": 205,
        "status": "PUBLISHED"
    },
    {
        "title": "超高纯度电子级多晶硅生产技术",
        "description": "优化了改良西门子法生产工艺，通过高效提纯和闭环循环利用技术，实现了11N级（99.999999999%）超高纯度电子级多晶硅的稳定量产。产品杂质含量极低，满足大规模集成电路制造对硅基材料的严苛要求。",
        "field": "新材料",
        "maturity": "TRL9",
        "price": 1500000.0,
        "ownerId": 204,
        "status": "PUBLISHED"
    },
    {
        "title": "单细胞测序数据分析云平台",
        "description": "开发了一站式单细胞测序数据分析云平台，集成了数据质控、降维聚类、差异表达分析、细胞轨迹推断等核心功能。平台采用并行计算架构，能快速处理海量单细胞数据，并提供交互式可视化报告，降低了数据分析门槛。",
        "field": "生物医药",
        "maturity": "TRL8",
        "price": 400000.0,
        "ownerId": 203,
        "status": "PUBLISHED"
    },
    {
        "title": "生物基可降解聚酯弹性体",
        "description": "以生物质来源的二元酸和二元醇为原料，合成了一系列生物基可降解聚酯弹性体。材料具有优异的回弹性、柔韧性和生物降解性，可替代传统石油基热塑性弹性体，广泛应用于鞋材、包装和软组织工程支架等领域。",
        "field": "新材料",
        "maturity": "TRL6",
        "price": 450000.0,
        "ownerId": 206,
        "status": "PUBLISHED"
    },
    {
        "title": "AI辅助医疗影像诊断系统",
        "description": "利用深度学习算法分析CT/MRI影像，辅助医生早期发现肺结节、乳腺癌等病灶，准确率超过95%。",
        "field": "人工智能",
        "maturity": "TRL8",
        "price": 1200000.0,
        "ownerId": 207,
        "status": "PUBLISHED"
    },
    {
        "title": "工业设备预测性维护平台",
        "description": "基于物联网传感器数据和大数据分析，实时监测设备状态，预测故障发生，减少意外停机时间。",
        "field": "智能制造",
        "maturity": "TRL7",
        "price": 850000.0,
        "ownerId": 208,
        "status": "PUBLISHED"
    },
    {
        "title": "基于区块链的供应链金融平台",
        "description": "利用区块链不可篡改特性，记录供应链各环节交易数据，为中小企业提供可信融资凭证。",
        "field": "金融科技",
        "maturity": "TRL9",
        "price": 1500000.0,
        "ownerId": 209,
        "status": "PUBLISHED"
    },
    {
        "title": "城市污水智能处理控制系统",
        "description": "结合传感器监测与AI算法，自动调节曝气量和药剂投加量，降低能耗并确保出水达标。",
        "field": "环保科技",
        "maturity": "TRL6",
        "price": 600000.0,
        "ownerId": 210,
        "status": "PUBLISHED"
    }
]

# Read existing achievements
with open(ACHIEVEMENTS_PATH, "r", encoding="utf-8") as f:
    achievements = json.load(f)

# Append new achievements (avoid duplicates based on title)
existing_titles = {a["title"] for a in achievements}
for new_a in NEW_ACHIEVEMENTS:
    if new_a["title"] not in existing_titles:
        achievements.append(new_a)

# Write back to raw_achievements.json
with open(ACHIEVEMENTS_PATH, "w", encoding="utf-8") as f:
    json.dump(achievements, f, ensure_ascii=False, indent=2)

print(f"Updated achievements. Total count: {len(achievements)}")

# Generate Cypher Script
# We need to ensure we cover technologies mentioned in achievements + fields

# Define Fields
FIELDS = [
    "生物医药", "新材料", "新能源", 
    "人工智能", "大数据", "物联网", 
    "环保科技", "智能制造", "金融科技", 
    "数字孪生", "区块链", "量子通信"
]

# Define Tech Categories and their Field mapping
TECH_MAP = {
    # New Energy / Battery
    "锂离子电池": "新能源", "正极材料": "新能源", "碳纳米管": "新材料", "硫": "新能源",
    "固态电池": "新能源", "固态电解质": "新能源", "钙钛矿太阳能电池": "新能源",
    
    # Bio / Med
    "基因编辑": "生物医药", "CRISPR-Cas9": "生物医药", "地中海贫血": "生物医药",
    "蛋白质结构预测": "生物医药", "PD-1抑制剂": "生物医药",
    "免疫治疗": "生物医药", "抗体偶联药物": "生物医药", "CAR-T": "生物医药",
    "实体瘤": "生物医药", "阿尔茨海默病": "生物医药", "生物标志物": "生物医药",
    "外泌体": "生物医药", "干细胞": "生物医药", "循环肿瘤细胞": "生物医药",
    "微流控": "生物医药", "mRNA疫苗": "生物医药", "脂质纳米颗粒": "生物医药",
    "类器官": "生物医药", "药物筛选": "生物医药", "单细胞测序": "生物医药",
    "多靶点药物": "生物医药",
    
    # Materials
    "石墨烯": "新材料", "防腐涂层": "新材料", "碳纤维": "新材料", "热塑性复合材料": "新材料",
    "轻量化": "新材料", "导电墨水": "新材料", "纳米银": "新材料", "柔性电路": "新材料",
    "聚乳酸": "新材料", "氮化镓": "新材料", "气凝胶": "新材料", "隔热材料": "新材料",
    "3D打印": "新材料", "骨科植入物": "生物医药", "形状记忆聚合物": "新材料",
    "超高分子量聚乙烯": "新材料", "PEEK": "新材料", "镁合金": "新材料",
    "心血管支架": "生物医药", "水凝胶": "新材料", "伤口敷料": "生物医药",
    "电子级多晶硅": "新材料", "生物基聚酯": "新材料",

    # AI
    "人工智能": "人工智能", "深度学习": "人工智能", "机器学习": "人工智能", 
    "计算机视觉": "人工智能", "自然语言处理": "人工智能", "NLP": "人工智能",
    "强化学习": "人工智能", "大模型": "人工智能", "生成式AI": "人工智能", 
    "神经网络": "人工智能", "知识图谱": "人工智能", "联邦学习": "人工智能",

    # Big Data
    "大数据": "大数据", "数据挖掘": "大数据", "分布式存储": "大数据", 
    "Hadoop": "大数据", "Spark": "大数据", "数据湖": "大数据", 
    "实时计算": "大数据", "Flink": "大数据", "数据治理": "大数据",

    # IoT
    "物联网": "物联网", "传感器": "物联网", "RFID": "物联网", 
    "ZigBee": "物联网", "边缘计算": "物联网", "智能家居": "物联网", 
    "工业互联网": "物联网", "NB-IoT": "物联网", "LoRa": "物联网",

    # Environmental
    "环保科技": "环保科技", "污水处理": "环保科技", "废气治理": "环保科技", 
    "碳捕获": "环保科技", "CCUS": "环保科技", "固废处理": "环保科技", 
    "环境监测": "环保科技", "土壤修复": "环保科技",

    # Smart Manufacturing
    "智能制造": "智能制造", "工业机器人": "智能制造", "自动化产线": "智能制造", 
    "数控机床": "智能制造", "柔性制造": "智能制造", "预测性维护": "智能制造", 
    "PLM": "智能制造", "MES": "智能制造",

    # Fintech
    "金融科技": "金融科技", "移动支付": "金融科技", "智能投顾": "金融科技", 
    "风险控制": "金融科技", "征信系统": "金融科技", "算法交易": "金融科技",

    # Digital Twin
    "数字孪生": "数字孪生", "三维建模": "数字孪生", "仿真模拟": "数字孪生", 
    "虚拟现实": "数字孪生", "VR": "数字孪生", "增强现实": "数字孪生", "AR": "数字孪生",

    # Blockchain
    "区块链": "区块链", "智能合约": "区块链", "分布式账本": "区块链", 
    "共识机制": "区块链", "跨链技术": "区块链", "零知识证明": "区块链", 
    "联盟链": "区块链", "BaaS": "区块链",

    # Quantum
    "量子通信": "量子通信", "量子密钥分发": "量子通信", "QKD": "量子通信", 
    "量子纠缠": "量子通信", "量子隐形传态": "量子通信", "量子网络": "量子通信", 
    "量子计算": "量子通信"
}

# Relationships (Tech -> Tech)
RELATIONS = [
    ("正极材料", "锂离子电池"), ("碳纳米管", "正极材料"), ("硫", "正极材料"),
    ("固态电解质", "固态电池"),
    ("CRISPR-Cas9", "基因编辑"), ("地中海贫血", "基因编辑"),
    ("深度学习", "蛋白质结构预测"),
    ("PD-1抑制剂", "免疫治疗"), ("CAR-T", "免疫治疗"), ("实体瘤", "CAR-T"),
    ("生物标志物", "阿尔茨海默病"),
    ("干细胞", "外泌体"),
    ("微流控", "循环肿瘤细胞"),
    ("防腐涂层", "石墨烯"),
    ("热塑性复合材料", "碳纤维"), ("轻量化", "热塑性复合材料"),
    ("纳米银", "导电墨水"), ("柔性电路", "导电墨水"),
    ("隔热材料", "气凝胶"),
    ("骨科植入物", "3D打印"), ("PEEK", "骨科植入物"),
    ("脂质纳米颗粒", "mRNA疫苗"),
    ("药物筛选", "类器官"),
    ("多靶点药物", "阿尔茨海默病"),
    ("伤口敷料", "水凝胶"), ("镁合金", "心血管支架"),
    ("单细胞测序", "生物标志物"), ("生物基聚酯", "聚乳酸"),
    
    # AI Relations
    ("深度学习", "机器学习"), ("神经网络", "深度学习"),
    ("计算机视觉", "深度学习"), ("自然语言处理", "深度学习"), ("NLP", "自然语言处理"),
    ("大模型", "NLP"), ("大模型", "深度学习"), ("生成式AI", "大模型"),
    ("强化学习", "机器学习"), ("联邦学习", "机器学习"),
    ("知识图谱", "人工智能"),

    # Big Data Relations
    ("数据挖掘", "大数据"), ("分布式存储", "大数据"), ("Hadoop", "分布式存储"),
    ("Spark", "大数据"), ("Flink", "实时计算"), ("实时计算", "大数据"),
    ("数据湖", "大数据"),

    # IoT Relations
    ("传感器", "物联网"), ("RFID", "物联网"), ("ZigBee", "物联网"),
    ("NB-IoT", "物联网"), ("LoRa", "物联网"),
    ("边缘计算", "物联网"), ("边缘计算", "5G"), # 5G added implicitly if not in map? careful
    ("智能家居", "物联网"), ("工业互联网", "物联网"),

    # Cross Domain
    ("预测性维护", "智能制造"), ("预测性维护", "物联网"), ("预测性维护", "大数据"),
    ("数字孪生", "智能制造"), ("数字孪生", "物联网"),
    ("三维建模", "数字孪生"), ("仿真模拟", "数字孪生"),
    ("智能合约", "区块链"), ("分布式账本", "区块链"),
    ("量子密钥分发", "量子通信"), ("QKD", "量子密钥分发"),
    ("碳捕获", "环保科技"), ("CCUS", "碳捕获"),
    ("工业机器人", "智能制造"), ("柔性制造", "智能制造"),
    ("风险控制", "金融科技"), ("算法交易", "金融科技")
]

cypher_lines = [
    "// Clean up",
    "MATCH (n) DETACH DELETE n;",
    "",
    "// 1. Create Fields"
]

# Create Field nodes
for field_name in FIELDS:
    var_name = "f_" + str(hash(field_name)).replace("-", "_")
    cypher_lines.append(f"CREATE ({var_name}:Field {{name: '{field_name}'}});")

cypher_lines.append("")
cypher_lines.append("// 2. Create Technologies")

# Generate Tech Create statements
for tech, field in TECH_MAP.items():
    var_name = "t_" + str(hash(tech)).replace("-", "_")
    cypher_lines.append(f"CREATE ({var_name}:Technology {{name: '{tech}', category: '{field}'}});")

cypher_lines.append("")
cypher_lines.append("// 3. Link Tech to Field")

# Link Tech to Field
for tech, field in TECH_MAP.items():
    var_name = "t_" + str(hash(tech)).replace("-", "_")
    if field in FIELDS:
        field_var = "f_" + str(hash(field)).replace("-", "_")
        cypher_lines.append(f"CREATE ({var_name})-[:BELONGS_TO]->({field_var});")
    # Also keep RELATED_TO for compatibility if needed, but BELONGS_TO is better semantically. 
    # The java code uses RELATED_TO for tech-tech. Let's add RELATED_TO for tech-field too as a fallback for simple traversal?
    # Actually, let's stick to the plan: (Technology)-[:BELONGS_TO]->(Field)
    # But wait, the repository query `MATCH (t:Technology {name: $name})-[:RELATED_TO*1..2]-(r:Technology)`
    # requires RELATED_TO. If we want to find siblings via Field, we need RELATED_TO or change the query.
    # Changing the query is better. But for now, let's add both or just RELATED_TO for simplicity in MVP.
    # Plan says: (Technology)-[:BELONGS_TO]->(Field).
    # So I should update the Repository query to `MATCH (t:Technology {name: $name})-[:RELATED_TO|BELONGS_TO*1..2]-(r:Technology)`?
    # Or just use RELATED_TO for everything for now to minimize code changes?
    # The Plan explicitly said: "建立 (Technology)-[:BELONGS_TO]->(Field)".
    # So I will use BELONGS_TO. And I will update the Java query to support it.
    
cypher_lines.append("")
cypher_lines.append("// 4. Link Tech to Tech")

# Link Tech to Tech
for src, dst in RELATIONS:
    if src in TECH_MAP and dst in TECH_MAP:
        src_var = "t_" + str(hash(src)).replace("-", "_")
        dst_var = "t_" + str(hash(dst)).replace("-", "_")
        cypher_lines.append(f"CREATE ({src_var})-[:RELATED_TO]->({dst_var});")

with open(CYPHER_PATH, "w", encoding="utf-8") as f:
    f.write("\n".join(cypher_lines))

print("Updated import.cypher")
