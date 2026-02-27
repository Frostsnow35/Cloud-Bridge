import json
import random
from datetime import datetime, timedelta

# Configuration
FIELDS = ["增强材料", "生物医药", "人工智能"]
COUNTS_PER_FIELD = 55  # To ensure 150+ total
TOTAL_ACHIEVEMENTS_TARGET = 200

# Rich Templates for Realistic Data
TEMPLATES = {
    "增强材料": {
        "concepts": [
            ("石墨烯导热膜", "用于高性能电子设备散热，热导率需>1500W/mK"),
            ("碳纤维复合材料", "航空航天级轻量化结构件，拉伸强度>3500MPa"),
            ("纳米陶瓷涂层", "耐高温防腐蚀涂层，耐温>1200℃，硬度>9H"),
            ("柔性显示基板", "高透光率聚酰亚胺薄膜，透光率>90%，耐弯折20万次"),
            ("生物可降解塑料", "PLA/PBAT共混改性材料，降解率>95%，用于环保包装"),
            ("高镍三元正极材料", "高能量密度锂电池正极，比容量>200mAh/g"),
            ("气凝胶绝热毡", "超低导热系数保温材料，导热系数<0.02W/mK"),
            ("液态金属热界面材料", "高性能CPU/GPU散热介质，界面热阻<0.005"),
            ("超导线材", "高温超导带材，临界电流>500A，用于强磁场设备"),
            ("光刻胶", "KrF/ArF半导体光刻胶，分辨率<100nm，纯度99.999%")
        ],
        "pain_points": [
            "目前良品率不足60%，急需提升制备工艺稳定性。",
            "现有材料耐候性差，户外使用寿命不足2年，需改性提升。",
            "国外技术垄断，急需实现国产化替代，降低成本50%。",
            "批次一致性差，无法满足车规级认证要求。",
            "生产能耗过高，寻求新型低能耗合成路径。"
        ],
        "cases": [
            "已应用于华为Mate系列手机散热系统。",
            "在C919大飞机机身壁板中得到验证。",
            "成功应用于某型号火箭发动机喷管。",
            "某知名新能源汽车电池包隔热垫供应商。",
            "国内某头部显示面板厂柔性屏产线导入。"
        ],
        "institutions": ["先进材料研究院", "未来科技材料公司", "理工大学材料学院", "航空航天材料所", "纳米技术应用中心"]
    },
    "生物医药": {
        "concepts": [
            ("PD-1/L1抗体药物", "针对实体瘤的免疫检查点抑制剂，亲和力KD<1nM"),
            ("mRNA疫苗递送系统", "高效LNP脂质纳米颗粒，包封率>90%，细胞转染效率>80%"),
            ("CAR-T细胞治疗", "针对血液瘤的靶向治疗，扩增倍数>100倍，杀伤率>70%"),
            ("阿尔茨海默症早筛试剂", "基于血液生物标志物的超敏检测试剂盒，灵敏度>95%"),
            ("骨科手术机器人", "高精度脊柱手术辅助系统，定位精度<0.5mm"),
            ("3D打印人工骨", "个性化定制钛合金植入物，孔隙率可控，生物相容性好"),
            ("干细胞培养基", "无血清间充质干细胞培养基，倍增时间<30小时"),
            ("AI药物筛选平台", "基于深度学习的化合物虚拟筛选，预测准确率>85%"),
            ("便携式核酸检测仪", "微流控芯片技术，30分钟出结果，检出限100copies/mL"),
            ("抗体偶联药物(ADC)", "新一代ADC药物，DAR值均一，旁观者效应显著")
        ],
        "pain_points": [
            "药物溶解度差，生物利用度低，需开发新型制剂配方。",
            "临床试验受试者招募困难，需精准患者画像分析系统。",
            "生产工艺放大困难，杂质去除率不达标。",
            "检测灵敏度不够，无法满足早期筛查需求。",
            "现有治疗方案副作用大，急需开发靶向性更强的新药。"
        ],
        "cases": [
            "在瑞金医院临床试验中表现优异。",
            "与某大型药企达成联合开发协议。",
            "获得FDA突破性疗法认定。",
            "已在全国30家三甲医院推广使用。",
            "帮助某新药研发周期缩短40%。"
        ],
        "institutions": ["生命科学研究所", "创新生物医药公司", "医科大学附属医院", "基因工程实验室", "精准医疗中心"]
    },
    "人工智能": {
        "concepts": [
            ("工业缺陷检测算法", "基于深度学习的表面缺陷检测，漏检率<0.1%，速度>100FPS"),
            ("自动驾驶感知系统", "多传感器融合(Lidar+Camera)，支持L3级自动驾驶，全天候运行"),
            ("智能客服大模型", "垂直领域微调LLM，意图识别准确率>95%，支持多轮对话"),
            ("医疗影像诊断辅助", "肺结节自动识别系统，敏感度>98%，假阳性率<2个/例"),
            ("智慧城市交通大脑", "实时交通流预测与信号灯优化，通行效率提升20%"),
            ("金融风控反欺诈", "基于图神经网络的实时交易反欺诈，误报率<0.01%"),
            ("语音识别转写引擎", "高噪音环境下语音转文字，字错率WER<5%"),
            ("安防人脸识别系统", "百万级底库1:N比对，首位命中率>99%，支持戴口罩识别"),
            ("物流路径规划算法", "多车多仓动态路径优化，配送成本降低15%"),
            ("虚拟数字人生成", "高保真口型驱动与动作生成，实时渲染延迟<50ms")
        ],
        "pain_points": [
            "模型在边缘端部署推理速度慢，需进行轻量化剪枝量化。",
            "小样本场景下泛化能力差，需提升少样本学习能力。",
            "数据隐私保护合规性不足，需引入联邦学习技术。",
            "复杂场景下鲁棒性差，易受环境光照干扰。",
            "算力成本过高，需优化模型架构降低FLOPS。"
        ],
        "cases": [
            "已部署于富士康产线进行质检。",
            "某新能源车企L3级自动驾驶量产搭载。",
            "服务于某国有银行智能客服系统。",
            "协助交警部门优化城市拥堵指数。",
            "在某头部电商平台大促期间保障物流效率。"
        ],
        "institutions": ["人工智能研究院", "智算科技公司", "大学计算机学院", "智慧城市实验室", "大数据创新中心"]
    }
}

def generate_demand(id_counter, field):
    template = TEMPLATES[field]
    concept_name, concept_desc = random.choice(template["concepts"])
    pain_point = random.choice(template["pain_points"])
    institution = random.choice(template["institutions"])
    
    budget_base = random.randint(50, 500) * 10000 # 50万 - 500万
    
    return {
        "id": id_counter,
        "title": f"寻求{concept_name}研发合作与技术攻关",
        "description": f"我司正在开发{concept_name}，{concept_desc}。当前面临主要问题是：{pain_point}。现诚邀高校或科研机构合作，指标要求达到行业领先水平，需具备成熟技术积累。",
        "field": field,
        "budget": budget_base,
        "deadline": (datetime.now() + timedelta(days=random.randint(30, 365))).strftime("%Y-%m-%d"),
        "status": "PUBLISHED",
        "ownerId": random.randint(100, 200), # Mock User IDs
        "type": "REWARD",
        "createdAt": datetime.now().isoformat(),
        "institution": institution,
        "contactName": f"张经理{random.randint(1, 100)}",
        "phone": f"138{random.randint(1000, 9999)}{random.randint(1000, 9999)}"
    }

def generate_achievement(id_counter, demand):
    # Match Logic: 
    # 30% chance Perfect Match (Same field, keywords, good price)
    # 40% chance Good Match (Same field, slightly different price)
    # 30% chance Weak/No Match (Different field or way off price - mainly for other demands)
    
    # But here we generate achievements specifically TO MATCH the demand for demo purposes
    # The matching algorithm will filter them later.
    
    field = demand["field"]
    concept_name = demand["title"].replace("寻求", "").replace("研发合作与技术攻关", "")
    
    # 80% chance to generate a matching achievement for this demand
    is_match = random.random() < 0.8
    
    if is_match:
        ach_field = field
        ach_title = f"{concept_name}成熟解决方案"
        ach_desc = f"本团队长期深耕{field}领域，已成功研发{concept_name}。{demand['description'][10:30]}... 技术指标完全满足要求，已在多家企业验证应用。"
        
        # Price logic: 
        # 50% chance price <= budget (Good match)
        # 50% chance price > budget (Negotiable)
        price_factor = random.uniform(0.8, 1.5)
        ach_price = int(demand["budget"] * price_factor)
        
        template = TEMPLATES[field]
        cases = random.sample(template["cases"], 2)
        institution = random.choice(template["institutions"])
        
    else:
        # Generate random other achievement
        other_field = random.choice([f for f in FIELDS if f != field])
        other_template = TEMPLATES[other_field]
        c_name, c_desc = random.choice(other_template["concepts"])
        ach_field = other_field
        ach_title = f"{c_name}核心技术转让"
        ach_desc = f"自主研发{c_name}，{c_desc}。技术成熟度高，欢迎咨询。"
        ach_price = random.randint(50, 800) * 10000
        cases = random.sample(other_template["cases"], 2)
        institution = random.choice(other_template["institutions"])

    return {
        "id": id_counter,
        "title": ach_title,
        "description": ach_desc,
        "field": ach_field,
        "price": ach_price,
        "maturity": random.choice(["实验室阶段", "小试阶段", "中试阶段", "成熟应用"]),
        "status": "PUBLISHED",
        "ownerId": random.randint(1, 50), # Mock Expert IDs
        "createdAt": (datetime.now() - timedelta(days=random.randint(1, 60))).isoformat(),
        "applicationCases": f"1. {cases[0]}\n2. {cases[1]}",
        "patentInfo": f"ZL{datetime.now().year}{random.randint(100000, 999999)}.X",
        "institution": institution,
        "contactName": f"李教授{random.randint(1, 100)}",
        "phone": f"139{random.randint(1000, 9999)}{random.randint(1000, 9999)}",
        "image": f"https://source.unsplash.com/random/800x600/?{ach_field}" # Random image placeholder
    }

def main():
    demands = []
    achievements = []
    
    d_id = 1
    a_id = 1
    
    print("Generating Demands...")
    for field in FIELDS:
        for _ in range(COUNTS_PER_FIELD):
            demand = generate_demand(d_id, field)
            demands.append(demand)
            
            # For each demand, generate 1-3 achievements
            # Some match, some don't (but we rely on the loop to fill pool)
            num_ach = random.randint(1, 3)
            for _ in range(num_ach):
                ach = generate_achievement(a_id, demand)
                achievements.append(ach)
                a_id += 1
            
            d_id += 1
            
    print(f"Generated {len(demands)} demands and {len(achievements)} achievements.")
    
    # Save files
    with open('smart_demands.json', 'w', encoding='utf-8') as f:
        json.dump(demands, f, ensure_ascii=False, indent=2)
        
    with open('smart_achievements.json', 'w', encoding='utf-8') as f:
        json.dump(achievements, f, ensure_ascii=False, indent=2)
        
    print("Data saved successfully.")

if __name__ == "__main__":
    main()
