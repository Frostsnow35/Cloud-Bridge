const fs = require('fs');
const path = require('path');

const OUTPUT_DIR = path.join(__dirname, '../data');
if (!fs.existsSync(OUTPUT_DIR)) {
    fs.mkdirSync(OUTPUT_DIR, { recursive: true });
}

// Configuration
const ACHIEVEMENT_COUNT = 120; // 100+
const DEMAND_COUNT = 60;

// Data Pools
const DOMAINS = {
    "Bio-medicine": {
        techs: ["mRNA疫苗", "靶向抗癌药", "基因编辑CRISPR", "纳米载药系统", "干细胞治疗", "医用钛合金", "生物3D打印", "智能假肢", "远程医疗系统", "AI辅助诊断"],
        apps: ["癌症治疗", "罕见病康复", "组织修复", "骨科植入", "心血管监测", "快速病毒检测", "个性化医疗", "术后康复"],
        adjectives: ["高效", "低毒副作用", "高生物相容性", "智能", "微创", "靶向精准", "长效缓释"],
        problems: ["传统药物副作用大", "现有治疗手段复发率高", "植入体排异反应严重", "诊断耗时长且准确率低", "进口设备价格昂贵"],
        solutions: ["采用新型纳米递送技术", "基于深度学习的图像识别算法", "利用诱导多能干细胞技术", "开发新型生物活性材料"],
        benefits: ["提高治愈率30%", "降低治疗成本50%", "缩短康复周期", "实现进口替代", "大幅提升患者生存质量"]
    },
    "New Materials": {
        techs: ["石墨烯导电浆料", "碳纤维复合材料", "气凝胶隔热材", "柔性电子皮肤", "超导材料", "光催化剂", "生物降解塑料", "记忆合金", "高熵合金", "量子点发光材料"],
        apps: ["新能源汽车", "航空航天结构件", "柔性显示屏", "工业废水处理", "智能穿戴设备", "精密仪器制造", "绿色包装"],
        adjectives: ["高强度", "超轻量化", "耐高温", "自修复", "高导电性", "环境友好", "超疏水"],
        problems: ["传统材料强度不足", "耐腐蚀性能差", "导电效率低", "生产过程污染严重", "材料成本过高限制应用"],
        solutions: ["引入纳米增强相", "优化微观晶体结构", "采用绿色合成工艺", "开发新型表面改性技术"],
        benefits: ["延长使用寿命2倍", "减重40%以上", "提升能源转换效率", "完全可生物降解", "耐极端环境性能优异"]
    },
    "Artificial Intelligence": {
        techs: ["计算机视觉算法", "自然语言处理大模型", "边缘计算网关", "强化学习决策系统", "知识图谱构建", "多模态生成模型", "联邦学习框架", "无人驾驶感知系统"],
        apps: ["安防监控", "智能客服", "工业质检", "自动驾驶", "金融风控", "智慧城市管理", "精准营销"],
        adjectives: ["低延迟", "高精度", "鲁棒性强", "轻量化", "可解释性", "隐私保护", "自适应"],
        problems: ["复杂场景识别率低", "数据隐私泄露风险", "模型推理算力需求大", "非结构化数据处理难", "人工审核成本高昂"],
        solutions: ["基于Transformer架构改进", "引入注意力机制", "采用模型剪枝与量化技术", "结合差分隐私算法"],
        benefits: ["识别准确率提升至99.9%", "推理速度提升5倍", "降低人工成本80%", "保障数据安全合规", "实现全天候无人值守"]
    },
    "Intelligent Manufacturing": {
        techs: ["工业机器人协作臂", "数字孪生系统", "柔性生产线", "激光精密加工机", "3D打印增材制造", "工业物联网平台", "AGV智能搬运车", "预测性维护系统"],
        apps: ["汽车总装", "3C电子制造", "航空发动机维修", "仓储物流", "精密模具加工", "化工园区监测"],
        adjectives: ["高柔性", "高精度", "智能化", "自适应", "模块化", "高速", "高稳定性"],
        problems: ["生产线换产周期长", "设备故障难以预测", "加工精度达不到微米级", "人工搬运效率低下", "生产数据孤岛严重"],
        solutions: ["融合5G与工业互联网", "构建全生命周期数字孪生体", "应用视觉伺服控制技术", "开发自适应加工算法"],
        benefits: ["生产效率提升30%", "设备停机时间减少90%", "良品率提升至99.99%", "实现柔性定制化生产", "大幅降低运营成本"]
    },
    "Green Energy": {
        techs: ["钙钛矿太阳能电池", "固态锂电池", "氢燃料电池堆", "风力发电机组控制", "智能微电网系统", "飞轮储能装置", "碳捕获与封存(CCUS)", "地热能利用系统"],
        apps: ["电动汽车动力", "分布式光伏电站", "海上风电场", "工业余热回收", "零碳园区建设", "应急电源保障"],
        adjectives: ["高转化率", "长循环寿命", "高能量密度", "低成本", "清洁无污染", "并网友好"],
        problems: ["光电转化效率瓶颈", "电池安全性隐患", "储能成本居高不下", "新能源并网波动大", "碳排放超标"],
        solutions: ["开发新型电解质材料", "优化电池热管理系统", "采用AI预测发电功率", "创新碳捕捉工艺"],
        benefits: ["能量密度突破500Wh/kg", "度电成本降低20%", "实现全天候稳定供电", "减少碳排放10万吨/年", "提升系统安全性"]
    }
};

const MATURITY_LEVELS = ["实验室阶段", "小试阶段", "中试阶段", "成熟应用"];
const STATUSES = ["PUBLISHED"];

// Helper functions
function getRandomInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}

function getRandomElement(arr) {
    return arr[Math.floor(Math.random() * arr.length)];
}

function generatePrice(domain) {
    // Generate price between 100k and 10m
    return getRandomInt(10, 1000) * 10000 + getRandomInt(0, 9999);
}

function generateDate(startYear = 2025) {
    const year = getRandomInt(startYear, 2026);
    const month = getRandomInt(1, 12);
    const day = getRandomInt(1, 28);
    const hour = getRandomInt(9, 18);
    const minute = getRandomInt(0, 59);
    const second = getRandomInt(0, 59);
    return `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}T${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}:${String(second).padStart(2, '0')}.000000`;
}

// Generate Achievements
const achievements = [];
const domainKeys = Object.keys(DOMAINS);

for (let i = 0; i < ACHIEVEMENT_COUNT; i++) {
    const domainKey = getRandomElement(domainKeys);
    const domain = DOMAINS[domainKey];
    
    const tech = getRandomElement(domain.techs);
    const app = getRandomElement(domain.apps);
    const adj = getRandomElement(domain.adjectives);
    const problem = getRandomElement(domain.problems);
    const solution = getRandomElement(domain.solutions);
    const benefit = getRandomElement(domain.benefits);
    
    const title = `${adj}${tech}在${app}中的应用`;
    const description = `针对当前${app}领域面临的“${problem}”这一核心痛点，本团队经过多年研发，成功推出了${adj}${tech}。该成果${solution}，有效解决了传统技术存在的瓶颈。经测试验证，该技术能够${benefit}，具有极高的推广价值和市场前景。技术指标完全满足行业标准，已在多家企业完成初步验证。`;
    
    achievements.push({
        id: i + 1,
        title: title,
        description: description,
        field: domainKey,
        price: generatePrice(domainKey),
        maturity: getRandomElement(MATURITY_LEVELS),
        status: "PUBLISHED",
        ownerId: getRandomInt(1, 50),
        createdAt: generateDate(2025),
        // Add some random extra fields if needed, but keeping it simple for now
    });
}

// Generate Demands
const demands = [];
for (let i = 0; i < DEMAND_COUNT; i++) {
    const domainKey = getRandomElement(domainKeys);
    const domain = DOMAINS[domainKey];
    
    const tech = getRandomElement(domain.techs);
    const app = getRandomElement(domain.apps);
    const problem = getRandomElement(domain.problems);
    const adj = getRandomElement(domain.adjectives);
    
    const title = `寻求${tech}研发合作与技术攻关`;
    const description = `我司正在开发${app}相关产品，急需引进${adj}${tech}技术。当前面临主要问题是：${problem}。希望寻找具有成熟技术积累的高校或科研机构进行合作开发或技术转让。要求技术成熟度达到${getRandomElement(MATURITY_LEVELS)}，指标需达到国内领先水平。`;
    
    demands.push({
        id: i + 1,
        title: title,
        description: description,
        field: domainKey,
        budget: generatePrice(domainKey),
        deadline: generateDate(2027).split('T')[0], // YYYY-MM-DD
        status: "PUBLISHED",
        ownerId: getRandomInt(100, 200),
        type: getRandomElement(["NORMAL", "REWARD"]),
        createdAt: generateDate(2026)
    });
}

// Write to files
fs.writeFileSync(path.join(OUTPUT_DIR, 'smart_achievements.json'), JSON.stringify(achievements, null, 2));
fs.writeFileSync(path.join(OUTPUT_DIR, 'smart_demands.json'), JSON.stringify(demands, null, 2));

console.log(`Successfully generated ${achievements.length} achievements and ${demands.length} demands.`);
