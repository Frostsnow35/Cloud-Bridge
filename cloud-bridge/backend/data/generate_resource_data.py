import json
import random
from datetime import datetime, timedelta

# 配置项
FIELDS = ["新材料", "生物医药", "高端装备", "人工智能", "新能源", "集成电路"]
LOCATIONS = ["上海", "北京", "深圳", "杭州", "苏州", "成都", "武汉"]
SCALES = ["初创型", "成长型", "成熟型", "行业巨头"]

def generate_enterprises(count=30):
    enterprises = []
    for i in range(1, count + 1):
        field = random.choice(FIELDS)
        loc = random.choice(LOCATIONS)
        scale = random.choice(SCALES)
        ent = {
            "id": i,
            "name": f"{loc}{field}{random.choice(['科技', '智能', '制造', '研发'])}有限公司",
            "industry": field,
            "scale": scale,
            "location": loc,
            "description": f"致力于{field}领域的前沿技术开发与产业化应用，拥有核心技术团队。",
            "products": [f"{field}核心组件", f"{field}系统解决方案", "定制化研发服务"],
            "website": f"http://www.{random.choice(['tech', 'info', 'service'])}{i}.com",
            "contact": f"联系人{i} | 021-{random.randint(10000000, 99999999)}"
        }
        enterprises.append(ent)
    return enterprises

def generate_experts(count=50, enterprises=[]):
    experts = []
    for i in range(1, count + 1):
        field = random.choice(FIELDS)
        ent = random.choice(enterprises) if enterprises else None
        expert = {
            "id": i,
            "name": f"{random.choice(['王', '李', '张', '陈', '刘', '赵'])}{random.choice(['教授', '博士', '总工', '主任'])}{random.randint(1, 100)}",
            "title": random.choice(["首席科学家", "技术总监", "高级研究员", "特聘专家"]),
            "institution": ent["name"] if ent and random.random() > 0.5 else f"{random.choice(LOCATIONS)}{field}研究院",
            "researchArea": [field, f"{field}应用技术", "前沿材料探索"],
            "achievements": [f"获得{field}领域专利{random.randint(5, 20)}项", f"主持国家级{field}课题{random.randint(1, 3)}项"],
            "collaborationHistory": f"曾与{random.choice(enterprises)['name'] if enterprises else '多家企业'}开展产学研合作，推动技术成果转化。"
        }
        experts.append(expert)
    return experts

def generate_policies(count=40):
    policies = []
    for i in range(1, count + 1):
        field = random.choice(FIELDS)
        loc = random.choice(LOCATIONS)
        policy = {
            "id": i,
            "title": f"关于促进{loc}{field}产业高质量发展的若干政策措施",
            "publishDate": (datetime.now() - timedelta(days=random.randint(1, 365))).strftime("%Y-%m-%d"),
            "department": f"{loc}科技创新委员会",
            "content": f"为贯彻落实国家关于{field}产业发展的战略部署，进一步优化产业环境，特制定本政策。",
            "policyType": random.choice(["财政补贴", "税收优惠", "人才激励", "土地支持"]),
            "industry": [field, "战略性新兴产业"],
            "region": loc,
            "conditions": ["在该地区注册的企业", "拥有自主知识产权", "上一年度研发投入占比不低于5%"],
            "supportContent": f"对符合条件的{field}项目给予最高{random.randint(100, 1000)}万元的资金支持。",
            "deadline": (datetime.now() + timedelta(days=random.randint(30, 180))).strftime("%Y-%m-%d"),
            "sourceUrl": "http://www.gov.cn/policy/example"
        }
        policies.append(policy)
    return policies

def generate_funds(count=30):
    funds = []
    for i in range(1, count + 1):
        field = random.choice(FIELDS)
        fund = {
            "id": i,
            "name": f"{field}产业引导基金 - {random.choice(['先锋', '卓越', '创投'])}系列",
            "provider": f"{random.choice(['建设', '工商', '招商', '中信'])}银行 / 科技金融部",
            "fundType": random.choice(["股权投资", "信用贷款", "科技贷", "融资租赁"]),
            "amountRange": f"{random.randint(100, 500)}万 - {random.randint(1000, 5000)}万",
            "industryFocus": [field, "先进制造", "数字化转型"],
            "requirements": ["企业信用评级A级以上", "具有核心竞争力的产品", "年营收增长率不低于20%"],
            "interestRate": f"{random.uniform(3.5, 5.5):.2f}%",
            "contactInfo": f"400-{random.randint(100, 999)}-{random.randint(1000, 9999)}"
        }
        funds.append(fund)
    return funds

def generate_equipments(count=40, enterprises=[]):
    equipments = []
    categories = ["分析检测", "加工制造", "研发中试", "计算模拟"]
    for i in range(1, count + 1):
        field = random.choice(FIELDS)
        ent = random.choice(enterprises) if enterprises else None
        equip = {
            "id": i,
            "name": f"高精度{field}{random.choice(['检测仪', '加工中心', '模拟器', '实验台'])}",
            "facilityName": f"{field}公共服务平台",
            "owner": ent["name"] if ent else f"{field}实验室",
            "category": random.choice(categories),
            "specs": f"精度：{random.uniform(0.001, 0.1):.3f}mm | 运行环境：万级无尘 | 最大负荷：{random.randint(100, 1000)}kg",
            "availability": random.choice(["空闲", "预约中", "维护中"]),
            "location": random.choice(LOCATIONS),
            "serviceContent": f"提供{field}相关的专业检测、数据分析及技术咨询服务。",
            "bookingUrl": "http://www.sharing-platform.com/book"
        }
        equipments.append(equip)
    return equipments

def generate_patents(count=60, enterprises=[]):
    patents = []
    for i in range(1, count + 1):
        field = random.choice(FIELDS)
        ent = random.choice(enterprises) if enterprises else None
        patent = {
            "id": i,
            "title": f"一种基于{field}的{random.choice(['高效合成方法', '智能控制系统', '核心算法', '结构组件'])}",
            "patentNumber": f"ZL{datetime.now().year}1{random.randint(10000000, 99999999)}.X",
            "assignee": ent["name"] if ent else f"{random.choice(LOCATIONS)}大学",
            "inventor": f"{random.choice(['王', '李', '张', '刘'])}博士",
            "applicationDate": (datetime.now() - timedelta(days=random.randint(500, 1000))).strftime("%Y-%m-%d"),
            "publicationDate": (datetime.now() - timedelta(days=random.randint(10, 300))).strftime("%Y-%m-%d"),
            "abstractText": f"本发明涉及{field}技术领域，通过优化流程提高了效率，降低了成本，具有显著的社会效益和经济效益。",
            "status": random.choice(["已授权", "实质审查中", "初审合格"]),
            "classifications": [field, "发明专利", "高新尖端技术"]
        }
        patents.append(patent)
    return patents

def save_to_json(data, filename):
    path = f"E:/数据要素大赛作品/cloud-bridge/backend/data/{filename}"
    with open(path, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=2)
    print(f"Saved {len(data)} items to {path}")

def main():
    print("Starting data generation for resource center...")
    
    enterprises = generate_enterprises(40)
    save_to_json(enterprises, "res_enterprises.json")
    
    experts = generate_experts(60, enterprises)
    save_to_json(experts, "res_experts.json")
    
    policies = generate_policies(50)
    save_to_json(policies, "res_policies.json")
    
    funds = generate_funds(35)
    save_to_json(funds, "res_funds.json")
    
    equipments = generate_equipments(45, enterprises)
    save_to_json(equipments, "res_equipments.json")
    
    patents = generate_patents(70, enterprises)
    save_to_json(patents, "res_patents.json")
    
    print("All resource center data generated successfully.")

if __name__ == "__main__":
    main()
