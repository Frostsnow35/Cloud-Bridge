import json
import random
import datetime

FIELDS = ["新材料", "人工智能", "生物医药", "物联网", "金融科技", "智能制造", "航空航天", "新能源"]
MATURITY_LEVELS = ["成熟应用", "中试阶段", "小试阶段"]
STATUS_DEMANDS = ["PUBLISHED", "PENDING_REVIEW", "MATCHING", "COMPLETED"]
STATUS_ACHIEVEMENTS = ["PUBLISHED", "PENDING_REVIEW", "TRADED"]

TITLES = {
    "新材料": ["石墨烯导热膜", "碳纤维复合材料", "高性能锂电池", "柔性显示屏", "新型生物降解塑料"],
    "人工智能": ["工业缺陷检测算法", "自动驾驶感知系统", "智慧城市交通预测", "医疗影像诊断AI", "智能客服机器人"],
    "生物医药": ["PD-1/L1抗体药物", "mRNA疫苗递送系统", "基因编辑工具", "新型靶向药物", "高端医疗器械"],
    "物联网": ["边缘计算网关", "智能传感器网络", "工业物联网平台", "车联网通信模块", "智慧农业监测系统"],
    "金融科技": ["区块链供应链金融", "智能投顾系统", "反欺诈风控模型", "数字货币支付网关", "量化交易策略"],
    "智能制造": ["工业机器人控制系统", "智能仓储物流", "数字化工厂平台", "3D打印设备", "精密加工数控系统"],
    "航空航天": ["卫星通信终端", "无人机编队控制", "火箭发动机材料", "航空航天导航系统", "空间站生命保障"],
    "新能源": ["氢燃料电池", "高效光伏组件", "风力发电机组", "智能微电网", "储能系统集成"]
}

INSTITUTIONS = ["清华大学", "北京大学", "浙江大学", "中国科学院", "上海交通大学", "华为技术有限公司", "阿里巴巴达摩院", "腾讯AI Lab"]
CONTACTS = ["张教授", "李博士", "王总监", "刘经理", "赵工", "陈研究员", "周老师", "吴主任"]

def generate_demands(count=50):
    demands = []
    for i in range(1, count + 1):
        field = random.choice(FIELDS)
        product = random.choice(TITLES[field])
        title = f"寻求{product}研发合作与技术攻关"
        desc = f"我司正在开发{product}，遇到关键技术瓶颈，急需解决。现有指标尚未达到预期，希望能与高校或科研机构合作，共同攻克难题。预算充足，欢迎联系。"
        
        # Ensure deadline is in the future (> 2026-02-23)
        deadline = (datetime.date(2027, 1, 1) + datetime.timedelta(days=random.randint(0, 730))).isoformat()
        
        demand = {
            "id": i,
            "title": title,
            "description": desc,
            "field": field,
            "budget": random.randint(50, 1000) * 10000,
            "deadline": deadline,
            "status": "PUBLISHED", # Mostly published
            "ownerId": random.randint(100, 200),
            "type": random.choice(["NORMAL", "REWARD"]),
            "createdAt": (datetime.datetime.now() - datetime.timedelta(days=random.randint(0, 365))).isoformat()
        }
        demands.append(demand)
    return demands

def generate_achievements(count=50):
    achievements = []
    for i in range(1, count + 1):
        field = random.choice(FIELDS)
        product = random.choice(TITLES[field])
        title = f"{product}成熟解决方案"
        desc = f"本团队长期深耕{field}领域，已成功研发{product}。技术指标完全满足行业要求，已在多家企业验证应用。拥有自主知识产权，欢迎咨询合作。"
        
        achievement = {
            "id": i,
            "title": title,
            "description": desc,
            "field": field,
            "price": random.randint(100, 2000) * 10000,
            "maturity": random.choice(MATURITY_LEVELS),
            "status": "PUBLISHED", # Mostly published
            "ownerId": random.randint(1, 100),
            "institution": random.choice(INSTITUTIONS),
            "contactName": random.choice(CONTACTS),
            "phone": f"13{random.randint(0,9)}{random.randint(10000000, 99999999)}",
            "createdAt": (datetime.datetime.now() - datetime.timedelta(days=random.randint(0, 365))).isoformat()
        }
        achievements.append(achievement)
    return achievements

if __name__ == "__main__":
    print("Generating mock data...")
    demands = generate_demands(60) # Generate 60 demands
    achievements = generate_achievements(60) # Generate 60 achievements
    
    with open("E:\\数据要素大赛作品\\cloud-bridge\\backend\\data\\smart_demands.json", "w", encoding="utf-8") as f:
        json.dump(demands, f, ensure_ascii=False, indent=2)
    print(f"Generated {len(demands)} demands.")
    
    with open("E:\\数据要素大赛作品\\cloud-bridge\\backend\\data\\smart_achievements.json", "w", encoding="utf-8") as f:
        json.dump(achievements, f, ensure_ascii=False, indent=2)
    print(f"Generated {len(achievements)} achievements.")
