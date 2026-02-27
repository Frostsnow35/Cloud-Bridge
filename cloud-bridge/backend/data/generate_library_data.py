import json
import random
from datetime import datetime, timedelta

# Configuration
COUNT = 50

# Templates
FIELDS = ["人工智能", "生物医药", "新材料", "新能源", "智能制造", "环保科技", "金融科技"]
CITIES = ["北京", "上海", "深圳", "广州", "杭州", "成都", "武汉", "南京", "西安"]
UNIVERSITIES = ["清华大学", "北京大学", "浙江大学", "上海交通大学", "复旦大学", "中国科学技术大学"]

def generate_expert(id):
    field = random.choice(FIELDS)
    return {
        "id": str(id),
        "name": f"专家{id}",
        "title": random.choice(["教授", "副教授", "研究员", "高级工程师"]),
        "affiliation": random.choice(UNIVERSITIES),
        "field": [field, random.choice(FIELDS)],
        "achievements": f"在{field}领域发表SCI论文{random.randint(10, 100)}篇，主持国家级课题{random.randint(1, 5)}项。",
        "email": f"expert{id}@example.com"
    }

def generate_policy(id):
    field = random.choice(FIELDS)
    city = random.choice(CITIES)
    return {
        "id": str(id),
        "title": f"{city}{field}产业发展扶持办法",
        "department": f"{city}科技局",
        "publishDate": (datetime.now() - timedelta(days=random.randint(1, 700))).strftime("%Y-%m-%d"),
        "policyType": random.choice(["资金补贴", "税收优惠", "人才引进", "平台建设"]),
        "content": f"为促进{field}产业发展，{city}设立专项资金，对符合条件的企业给予最高{random.randint(100, 1000)}万元补贴。",
        "industry": [field]
    }

def generate_fund(id):
    field = random.choice(FIELDS)
    return {
        "id": str(id),
        "name": f"{field}产业投资基金",
        "provider": random.choice(["红杉资本", "高瓴资本", "深创投", "IDG资本"]),
        "fundType": random.choice(["股权融资", "债权融资", "天使投资", "风险投资"]),
        "amountRange": f"{random.randint(500, 5000)}万-{random.randint(6000, 20000)}万",
        "interestRate": f"{random.uniform(3.0, 6.0):.1f}%" if random.random() > 0.5 else "面议",
        "industryFocus": [field]
    }

def generate_equipment(id):
    field = random.choice(FIELDS)
    return {
        "id": str(id),
        "name": f"高性能{field}测试平台",
        "category": "研发设备",
        "facilityName": f"{random.choice(UNIVERSITIES)}分析测试中心",
        "specs": "分辨率<1nm，灵敏度>99%",
        "owner": random.choice(UNIVERSITIES),
        "availability": random.choice(["Available", "In Use"])
    }

def generate_patent(id):
    field = random.choice(FIELDS)
    return {
        "id": str(id),
        "title": f"一种基于{field}的智能处理方法",
        "patentNumber": f"CN{random.randint(10000000, 99999999)}B",
        "status": "授权",
        "publicationDate": (datetime.now() - timedelta(days=random.randint(1, 1000))).strftime("%Y-%m-%d"),
        "assignee": random.choice(UNIVERSITIES),
        "abstractText": f"本发明公开了一种{field}方法，解决了现有技术中效率低、成本高的问题。"
    }

def generate_enterprise(id):
    field = random.choice(FIELDS)
    city = random.choice(CITIES)
    return {
        "id": str(id),
        "name": f"{city}{field}科技有限公司",
        "industry": field,
        "location": city,
        "scale": random.choice(["0-20人", "20-99人", "100-499人", "500人以上"]),
        "description": f"专注于{field}领域的技术研发与应用，致力于成为行业领先的解决方案提供商。"
    }

def write_jsonl(filename, generator, count):
    with open(filename, 'w', encoding='utf-8') as f:
        for i in range(1, count + 1):
            item = generator(i)
            f.write(json.dumps(item, ensure_ascii=False) + '\n')
    print(f"Generated {count} items in {filename}")

def main():
    write_jsonl('experts.jsonl', generate_expert, COUNT)
    write_jsonl('policies.jsonl', generate_policy, COUNT)
    write_jsonl('funds.jsonl', generate_fund, COUNT)
    write_jsonl('equipments.jsonl', generate_equipment, COUNT)
    write_jsonl('patents.jsonl', generate_patent, COUNT)
    write_jsonl('enterprises.jsonl', generate_enterprise, COUNT)

if __name__ == "__main__":
    main()
