import json
import random
import os
from datetime import datetime, timedelta

# Configuration
FIELDS = [
    "生物医药", "新材料", "新能源", "新一代信息技术", "智能制造", 
    "高端装备", "绿色环保", "数字创意", "未来产业"
]

# Field Mapping for variety
FIELD_SUB_MAP = {
    "新一代信息技术": ["人工智能", "大数据", "云计算", "物联网", "区块链", "5G/6G"],
    "未来产业": ["量子信息", "类脑智能", "深海空天", "氢能储能"]
}

# Rich Templates for Realistic Data
TEMPLATES = {
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
        "cases": ["瑞金医院临床试验", "某大型药企联合开发", "FDA突破性疗法认定", "全国30家三甲医院推广", "新药研发周期缩短40%"],
        "institutions": ["生命科学研究所", "创新生物医药公司", "医科大学附属医院", "基因工程实验室", "精准医疗中心"],
        "equipment_names": ["高通量基因测序仪", "冷冻电镜", "流式细胞仪", "生物反应器", "核磁共振波谱仪"]
    },
    "新材料": {
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
            "良品率不足60%，需提升制备工艺稳定性。",
            "耐候性差，户外使用寿命不足，需改性提升。",
            "国外技术垄断，急需国产化替代。",
            "批次一致性差，无法满足车规级认证。",
            "生产能耗过高，寻求低能耗合成路径。"
        ],
        "cases": ["华为Mate系列手机散热", "C919大飞机机身", "火箭发动机喷管", "新能源汽车电池包", "柔性屏产线导入"],
        "institutions": ["先进材料研究院", "未来科技材料公司", "理工大学材料学院", "航空航天材料所", "纳米技术应用中心"],
        "equipment_names": ["扫描电子显微镜(SEM)", "X射线衍射仪(XRD)", "磁控溅射镀膜机", "热等静压机", "万能材料试验机"]
    },
    "新能源": {
        "concepts": [
            ("固态电池电解质", "高离子电导率硫化物电解质，室温电导率>10^-3 S/cm"),
            ("钙钛矿太阳能电池", "大面积制备工艺，光电转换效率>24%"),
            ("氢燃料电池质子交换膜", "低成本长寿命PEM，运行寿命>5000小时"),
            ("智慧储能BMS系统", "主动均衡技术，电池组循环寿命提升30%"),
            ("海上风电叶片", "100米级超长叶片设计，抗台风等级17级"),
            ("光伏逆变器", "高效率组串式逆变器，最大转换效率99%"),
            ("地热能发电技术", "中低温地热发电系统，热电转换效率>15%"),
            ("无线充电桩", "大功率电动汽车无线充电，传输效率>92%"),
            ("钠离子电池正极", "普鲁士蓝类正极材料，循环寿命>2000次"),
            ("飞轮储能装置", "磁悬浮飞轮储能，响应时间<5ms")
        ],
        "pain_points": ["能量密度遇到瓶颈", "安全性有待提升", "成本过高限制商业化", "并网稳定性问题", "低温环境下性能衰减严重"],
        "cases": ["宁德时代电池产线", "隆基绿能光伏电站", "国家电网储能示范", "特斯拉充电网络", "海上风电场项目"],
        "institutions": ["新能源技术研究院", "电力科学研究院", "太阳能光伏实验室", "储能技术研究中心", "氢能工程中心"],
        "equipment_names": ["电池充放电测试柜", "太阳能模拟器", "电化学工作站", "风洞实验室", "燃料电池测试台"]
    },
    "新一代信息技术": {
        "concepts": [
            ("工业缺陷检测算法", "基于深度学习的表面缺陷检测，漏检率<0.1%"),
            ("自动驾驶感知系统", "多传感器融合(Lidar+Camera)，支持L3级自动驾驶"),
            ("智能客服大模型", "垂直领域微调LLM，意图识别准确率>95%"),
            ("医疗影像诊断辅助", "肺结节自动识别系统，敏感度>98%"),
            ("智慧城市交通大脑", "实时交通流预测与信号灯优化，通行效率提升20%"),
            ("金融风控反欺诈", "基于图神经网络的实时交易反欺诈，误报率<0.01%"),
            ("语音识别转写引擎", "高噪音环境下语音转文字，字错率WER<5%"),
            ("安防人脸识别系统", "百万级底库1:N比对，首位命中率>99%"),
            ("物流路径规划算法", "多车多仓动态路径优化，配送成本降低15%"),
            ("虚拟数字人生成", "高保真口型驱动与动作生成，实时渲染延迟<50ms")
        ],
        "pain_points": ["边缘端推理速度慢", "小样本场景泛化差", "数据隐私保护不足", "复杂场景鲁棒性差", "算力成本过高"],
        "cases": ["富士康产线质检", "新能源车企L3量产", "国有银行客服系统", "城市交通大脑", "电商物流优化"],
        "institutions": ["人工智能研究院", "智算科技公司", "大学计算机学院", "大数据创新中心", "物联网工程实验室"],
        "equipment_names": ["GPU超算服务器", "5G基站测试仪", "工业相机", "激光雷达", "VR开发套件"]
    },
    "智能制造": {
        "concepts": [
            ("协作机器人", "人机协作安全控制，重复定位精度±0.02mm"),
            ("工业互联网平台", "设备上云与边缘计算，支持协议>100种"),
            ("柔性制造产线", "多品种小批量混线生产，换型时间<10分钟"),
            ("数字孪生工厂", "虚实映射与实时监控，同步延迟<100ms"),
            ("预测性维护系统", "基于振动分析的故障预测，准确率>90%"),
            ("AGV物流小车", "激光SLAM导航，定位精度±10mm"),
            ("机器视觉引导系统", "3D视觉抓取引导，识别速度<0.5s"),
            ("MES生产管理系统", "全流程生产追溯，生产效率提升20%"),
            ("智能仓储立体库", "堆垛机自动存取，空间利用率提升300%"),
            ("激光焊接工作站", "高功率光纤激光焊接，焊缝成型美观")
        ],
        "pain_points": ["设备协议不统一", "数据孤岛现象严重", "定制化需求响应慢", "老旧设备改造困难", "专业人才短缺"],
        "cases": ["三一重工灯塔工厂", "海尔互联工厂", "吉利汽车焊装车间", "京东物流亚洲一号", "西门子数字化工厂"],
        "institutions": ["智能制造研究院", "工业自动化研究所", "机器人工程中心", "数字化工厂实验室", "先进制造技术中心"],
        "equipment_names": ["六轴工业机器人", "数控加工中心(CNC)", "激光切割机", "自动导引车(AGV)", "三坐标测量机"]
    },
    "高端装备": {
        "concepts": [
            ("五轴联动数控机床", "航空叶片复杂曲面加工，加工精度<5μm"),
            ("盾构机主轴承", "大直径盾构机核心部件，设计寿命>10000小时"),
            ("燃气轮机叶片", "耐高温单晶高温合金叶片，耐温>1400℃"),
            ("深海潜水器载人舱", "钛合金耐压球壳，下潜深度>10000米"),
            ("高铁动车组转向架", "高速列车走行部，运营时速>350km/h"),
            ("光刻机双工件台", "超精密运动控制，同步精度<10nm"),
            ("大飞机起落架", "高强钢锻件，抗冲击能力强"),
            ("核电站压力容器", "三代核电核心设备，设计寿命60年"),
            ("海洋钻井平台", "深水半潜式钻井平台，作业水深>3000米"),
            ("精密减速器", "机器人关节核心部件，传动效率>95%")
        ],
        "pain_points": ["核心部件依赖进口", "加工精度稳定性不足", "材料性能有待突破", "设计软件被卡脖子", "可靠性验证周期长"],
        "cases": ["C919大飞机首飞", "复兴号高铁运营", "蛟龙号深潜", "华龙一号核电站", "蓝鲸一号钻井平台"],
        "institutions": ["机械科学研究总院", "航空航天动力所", "船舶工业集团", "重型机械研究院", "精密机床工程中心"],
        "equipment_names": ["大型锻压机", "真空电子束焊机", "超声波探伤仪", "精密磨床", "五轴加工中心"]
    },
    "绿色环保": {
        "concepts": [
            ("VOCs废气治理", "吸附浓缩+催化燃烧，去除率>98%"),
            ("工业废水零排放", "膜法水处理集成技术，回收率>95%"),
            ("土壤重金属修复", "植物-微生物联合修复，修复周期缩短30%"),
            ("餐厨垃圾资源化", "厌氧发酵产沼气，资源化利用率>90%"),
            ("碳捕获与封存(CCUS)", "低能耗CO2捕获，捕获成本<200元/吨"),
            ("危险废物等离子焚烧", "高温裂解彻底无害化，减容比>95%"),
            ("生态边坡修复", "植被混凝土生态防护，抗冲刷能力强"),
            ("河道水质在线监测", "多参数自动监测站，数据实时上传"),
            ("除尘脱硫脱硝一体化", "超低排放烟气治理，粉尘<5mg/m3"),
            ("建筑垃圾再生骨料", "移动破碎筛分站，杂质分离率>98%")
        ],
        "pain_points": ["处理成本居高不下", "二次污染风险", "监测数据准确性差", "治理技术标准滞后", "资源化利用率低"],
        "cases": ["雄安新区水环境治理", "某化工园区废气整治", "垃圾焚烧发电厂", "矿山生态修复项目", "碳中和示范园区"],
        "institutions": ["环境科学研究院", "生态环境工程中心", "水处理技术实验室", "固废资源化研究所", "碳中和研究中心"],
        "equipment_names": ["气相色谱质谱联用仪", "水质自动监测站", "等离子焚烧炉", "板框压滤机", "烟气分析仪"]
    },
    "数字创意": {
        "concepts": [
            ("8K超高清视频制作", "全流程8K制播系统，色彩深度12bit"),
            ("沉浸式光影艺术展", "多通道投影融合，互动响应延迟<0.1s"),
            ("云游戏渲染引擎", "实时光线追踪，支持千人同屏"),
            ("数字博物馆", "文物高精度三维扫描，纹理分辨率4K"),
            ("AI音乐作曲", "情感化音乐生成，支持多种风格"),
            ("虚拟拍摄影棚", "LED背景墙实时渲染，所见即所得"),
            ("全息投影舞台", "裸眼3D视觉效果，可视角度>120度"),
            ("数字版权保护(DRM)", "区块链版权确权，监测盗版准确率>99%"),
            ("动漫动作捕捉", "光学动捕系统，捕捉点精度<1mm"),
            ("智慧文旅导览", "AR实景导航与讲解，识别准确率>95%")
        ],
        "pain_points": ["内容制作周期长", "版权保护困难", "互动体验单一", "硬件设备昂贵", "商业模式不清晰"],
        "cases": ["故宫数字博物馆", "春晚8K直播", "原神云游戏", "TeamLab光影展", "数字敦煌项目"],
        "institutions": ["数字创意产业园", "美术学院设计中心", "广播电视科学研究院", "动漫游戏研发中心", "文化遗产数字化所"],
        "equipment_names": ["8K摄影机", "动作捕捉系统", "高性能图形工作站", "专业级无人机", "全息投影仪"]
    },
    "未来产业": {
        "concepts": [
            ("量子密钥分发(QKD)", "城域量子保密通信，密钥生成率>10kbps"),
            ("脑机接口(BCI)", "非侵入式脑电采集，意念控制准确率>80%"),
            ("类脑神经形态芯片", "超低功耗脉冲神经网络，能效比提升100倍"),
            ("深海载人潜水器", "全海深作业能力，耐压壳体安全系数>1.5"),
            ("商业航天运载火箭", "可回收复用火箭，发射成本降低50%"),
            ("氢燃料燃气轮机", "纯氢燃烧低氮排放，热效率>35%"),
            ("6G太赫兹通信", "超高速无线传输，峰值速率>100Gbps"),
            ("合成生物学工厂", "细胞工厂定制化生产，产率提升10倍"),
            ("原子制造技术", "原子级精度加工，表面粗糙度<0.1nm"),
            ("空天互联网", "低轨卫星星座组网，全球无缝覆盖")
        ],
        "pain_points": ["技术成熟度低", "基础理论未突破", "伦理法律风险", "产业链不完善", "投资回报周期长"],
        "cases": ["量子京沪干线", "SpaceX星链计划", "Neuralink脑机接口", "奋斗者号深潜", "中国天眼FAST"],
        "institutions": ["量子信息科学国家实验室", "脑科学与类脑研究中心", "深海科学与工程所", "航天科技集团", "合成生物学中心"],
        "equipment_names": ["单光子探测器", "脑电采集仪", "超高真空扫描隧道显微镜", "卫星地面站", "深海着陆器"]
    }
}

def generate_random_phone():
    return f"1{random.choice(['3','5','7','8','9'])}{random.randint(100000000, 999999999)}"

def generate_data():
    base_dir = os.path.dirname(os.path.abspath(__file__))
    
    # 1. Generate Experts
    experts = []
    expert_id = 1
    for field, template in TEMPLATES.items():
        for i in range(5): # 5 experts per field
            inst = random.choice(template["institutions"])
            experts.append({
                "id": expert_id,
                "name": f"{field[0]}专家{i+1}",
                "title": random.choice(["教授", "研究员", "高级工程师", "博士生导师"]),
                "field": field,
                "institution": inst,
                "description": f"长期从事{field}领域研究，在{template['concepts'][i][0]}方面有深厚造诣。",
                "phone": generate_random_phone(),
                "email": f"expert{expert_id}@example.com",
                "status": "VERIFIED"
            })
            expert_id += 1
            
    # 2. Generate Enterprises
    enterprises = []
    ent_id = 1
    for field, template in TEMPLATES.items():
        for i in range(3): # 3 enterprises per field
            enterprises.append({
                "id": ent_id,
                "name": f"{field}科技创新有限公司{i+1}",
                "field": field,
                "scale": random.choice(["初创", "中小企业", "高新技术企业", "上市公司"]),
                "address": f"白云区科技园{field}大道{random.randint(1,999)}号",
                "contactName": f"王总{i+1}",
                "phone": generate_random_phone(),
                "status": "VERIFIED",
                "description": f"专注于{field}领域的科技创新企业，致力于{template['concepts'][0][0]}的产业化。"
            })
            ent_id += 1

    # 3. Generate Demands
    demands = []
    demand_id = 1
    for field, template in TEMPLATES.items():
        # Generate 15 demands per field
        for i in range(15):
            concept, desc = random.choice(template["concepts"])
            pain = random.choice(template["pain_points"])
            ent = random.choice([e for e in enterprises if e["field"] == field])
            
            demands.append({
                "id": demand_id,
                "title": f"寻求{concept}关键技术合作",
                "description": f"我司正在研发{concept}，{desc}。目前遇到瓶颈：{pain}。希望能找到有相关经验的团队进行联合攻关。",
                "field": field,
                "budget": random.randint(50, 500) * 10000,
                "deadline": (datetime.now() + timedelta(days=random.randint(30, 365))).strftime("%Y-%m-%d"),
                "status": "PUBLISHED",
                "ownerId": ent["id"], # Link to enterprise
                "type": "REWARD",
                "createdAt": datetime.now().isoformat(),
                "institution": ent["name"],
                "contactName": ent["contactName"],
                "phone": ent["phone"]
            })
            demand_id += 1

    # 4. Generate Achievements
    achievements = []
    ach_id = 1
    for field, template in TEMPLATES.items():
        # Generate 20 achievements per field
        for i in range(20):
            concept, desc = random.choice(template["concepts"])
            expert = random.choice([e for e in experts if e["field"] == field])
            
            achievements.append({
                "id": ach_id,
                "title": f"{concept}成熟技术",
                "description": f"自主研发的{concept}，{desc}。技术成熟度高，已有成功应用案例：{random.choice(template['cases'])}。",
                "field": field,
                "price": random.randint(30, 800) * 10000,
                "maturity": random.choice(["实验室阶段", "小试阶段", "中试阶段", "成熟应用"]),
                "status": "PUBLISHED",
                "ownerId": expert["id"],
                "createdAt": (datetime.now() - timedelta(days=random.randint(1, 100))).isoformat(),
                "applicationCases": f"1. {template['cases'][0]}\n2. {template['cases'][1]}",
                "patentInfo": f"ZL{datetime.now().year}{random.randint(100000, 999999)}.X",
                "institution": expert["institution"],
                "contactName": expert["name"],
                "phone": expert["phone"],
                "image": f"https://source.unsplash.com/random/800x600/?{field}"
            })
            ach_id += 1

    # 5. Generate Other Resources (Policies, Funds, Equipments, Patents)
    policies = []
    funds = []
    equipments = []
    patents = []
    
    p_id = 1
    f_id = 1
    e_id = 1
    pat_id = 1
    
    for field, template in TEMPLATES.items():
        # Policies
        policies.append({
            "id": p_id,
            "title": f"{field}产业扶持政策",
            "department": "白云区科信局",
            "publishDate": "2025-01-15",
            "content": f"为支持{field}产业发展，对相关高新技术企业给予税收优惠和研发补贴...",
            "field": field,
            "type": "补贴奖励"
        })
        p_id += 1
        
        # Funds
        funds.append({
            "id": f_id,
            "name": f"{field}产业引导基金",
            "amount": "10亿元",
            "provider": "白云区国资委",
            "field": field,
            "requirements": "注册在白云区的高新技术企业，年营收超过2000万",
            "description": f"专注于{field}领域的早期项目投资。"
        })
        f_id += 1
        
        # Equipments
        for i in range(3):
            equip_name = template["equipment_names"][i] if i < len(template["equipment_names"]) else "通用设备"
            equipments.append({
                "id": e_id,
                "name": equip_name,
                "model": f"Model-{random.randint(100,999)}",
                "provider": random.choice(template["institutions"]),
                "field": field,
                "status": "AVAILABLE",
                "description": f"高性能{equip_name}，可用于{field}领域的科研测试。",
                "price": f"{random.randint(500, 2000)}元/小时"
            })
            e_id += 1
            
        # Patents (derived from achievements)
        for ach in [a for a in achievements if a["field"] == field][:5]:
            patents.append({
                "id": pat_id,
                "name": f"一种{ach['title']}的方法",
                "type": "发明专利",
                "number": ach["patentInfo"],
                "holder": ach["institution"],
                "field": field,
                "status": "AUTHORIZED",
                "applyDate": "2024-06-01"
            })
            pat_id += 1

    # Save all files
    def save_json(filename, data):
        path = os.path.join(base_dir, filename)
        with open(path, 'w', encoding='utf-8') as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
        print(f"Saved {len(data)} items to {filename}")

    save_json("res_experts.json", experts)
    save_json("res_enterprises.json", enterprises)
    save_json("smart_demands.json", demands)
    save_json("smart_achievements.json", achievements)
    save_json("res_policies.json", policies)
    save_json("res_funds.json", funds)
    save_json("res_equipments.json", equipments)
    save_json("res_patents.json", patents)

    # Generate Cypher
    generate_cypher(achievements, demands, experts, enterprises)

def generate_cypher(achievements, demands, experts, enterprises):
    cypher_lines = [
        "// Clear DB",
        "MATCH (n) DETACH DELETE n;",
        "// Create Indexes",
        "CREATE INDEX IF NOT EXISTS FOR (t:Technology) ON (t.name);",
        "CREATE INDEX IF NOT EXISTS FOR (f:Field) ON (f.name);",
        ""
    ]
    
    # 1. Create Fields
    for field in FIELDS:
        cypher_lines.append(f"MERGE (:Field {{name: '{field}'}});")
        
    # 2. Create Experts & Enterprises Nodes (Simplified for Graph)
    # Actually, main graph logic is usually Tech -> Field. 
    # But let's add (Expert)-[:EXPERT_IN]->(Field)
    
    for exp in experts:
        cypher_lines.append(f"CREATE (:Expert {{id: {exp['id']}, name: '{exp['name']}'}})-[:EXPERT_IN]->(:Field {{name: '{exp['field']}'}});")
        
    for ent in enterprises:
        cypher_lines.append(f"CREATE (:Enterprise {{id: {ent['id']}, name: '{ent['name']}'}})-[:OPERATES_IN]->(:Field {{name: '{ent['field']}'}});")

    # 3. Create Technologies (Extract from titles/concepts)
    # We assume the concept name in title is the technology
    
    tech_nodes = set()
    tech_field_map = {}
    
    for ach in achievements:
        # Title format: "{concept}成熟技术"
        tech_name = ach['title'].replace("成熟技术", "").replace("关键技术", "")
        tech_nodes.add(tech_name)
        tech_field_map[tech_name] = ach['field']
        
    for demand in demands:
        # Title format: "寻求{concept}关键技术合作"
        tech_name = demand['title'].replace("寻求", "").replace("关键技术合作", "")
        tech_nodes.add(tech_name)
        tech_field_map[tech_name] = demand['field']
        
    for tech in tech_nodes:
        field = tech_field_map[tech]
        cypher_lines.append(f"MERGE (t:Technology {{name: '{tech}'}}) MERGE (f:Field {{name: '{field}'}}) MERGE (t)-[:BELONGS_TO]->(f);")
        
    # 4. Create Relationships (Mock Logic)
    # Link random technologies within same field
    for field in FIELDS:
        field_techs = [t for t in tech_nodes if tech_field_map[t] == field]
        if len(field_techs) > 1:
            for i in range(len(field_techs) - 1):
                t1 = field_techs[i]
                t2 = field_techs[i+1]
                cypher_lines.append(f"MATCH (a:Technology {{name: '{t1}'}}), (b:Technology {{name: '{t2}'}}) MERGE (a)-[:RELATED_TO]->(b);")

    path = os.path.join(os.path.dirname(os.path.abspath(__file__)), "../src/main/resources/cypher/import.cypher")
    # Ensure dir exists
    os.makedirs(os.path.dirname(path), exist_ok=True)
    
    with open(path, 'w', encoding='utf-8') as f:
        f.write("\n".join(cypher_lines))
    print(f"Saved Cypher script to {path}")

if __name__ == "__main__":
    generate_data()
