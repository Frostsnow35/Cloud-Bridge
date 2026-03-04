package com.cloudbridge.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DomainHierarchyUtil {
    // Define Hierarchy
    public static final Map<String, List<String>> DOMAIN_HIERARCHY = new HashMap<>();
    static {
        DOMAIN_HIERARCHY.put("生物医药", Arrays.asList("细胞", "基因", "药物", "疫苗", "抗体", "蛋白", "免疫", "试剂", "诊断", "治疗"));
        DOMAIN_HIERARCHY.put("新材料", Arrays.asList("石墨烯", "纳米", "高分子", "复合材料", "金属", "陶瓷", "纤维", "涂层"));
        DOMAIN_HIERARCHY.put("人工智能", Arrays.asList("深度学习", "机器学习", "神经网络", "图像识别", "自然语言处理", "机器人", "智能", "算法"));
        DOMAIN_HIERARCHY.put("电子信息", Arrays.asList("大数据", "云计算", "物联网", "区块链", "5G", "通信", "芯片", "半导体"));
        DOMAIN_HIERARCHY.put("智能制造", Arrays.asList("自动化", "数控", "3D打印", "传感器", "工业互联网", "机床"));
    }
}