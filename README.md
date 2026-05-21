# Eve Assistant — Mindustry 调试辅助模组

为 AI Agent 提供与 Mindustry 游戏进行交互开发的辅助 Mod。

## 仓库结构

```
eve-assistant/
├── mod.hjson                    # Mod 元信息
├── build.gradle                 # Gradle 构建配置
├── settings.gradle
├── src/evai/                    # Kotlin 源码
│   ├── EveMod.kt               # 入口：init() 启动 HTTP 服务 + 注册命令
│   ├── http/                   # HTTP API 层
│   ├── command/                # 命令处理
│   ├── monitor/                # 事件监听
│   ├── inspector/              # 反射检测
│   ├── action/                 # 建造/拆除
│   ├── spawn/                  # 单位生成
│   └── ui/                     # 聊天格式化
└── skill/eve-assistant/        # AI Agent 技能文件
    └── SKILL.md                # Agent 操作手册
```

## 交互方式

Mod 启动时在 `0.0.0.0:18090` 开启 HTTP 服务器，AI Agent 通过 `web_fetch("http://192.168.0.108:18090/api/run?cmd=...")` 调用。

兜底通道：游戏内聊天命令 `/eve xxx`。

## 功能清单

- **内容查询**: 列出所有注册的 blocks/items/liquids/units/bullets，查看任意内容的公开字段
- **世界探测**: 扫描地图区域，检查 Tile/Building 的 items/power/liquid/health 等状态
- **调试规则**: 修改 state.rules（无限资源/瞬间建造/各种倍率/开关火焰等）
- **低级操作**: 反射直接写 Building 任意字段、设置物品栏/液体/电力/血量/队伍
- **事件调试**: 监听 27 种 EventType，手动触发事件
- **建造拆除**: 单点/矩形/直线/环形建造和拆除
- **蓝图系统**: 创建/铺设/列出/删除/导出/导入蓝图
- **单位生成**: 生成/附身/清除单位和敌人
- **JS 控制台**: 执行任意 JavaScript / Logic 代码
- **存档管理**: 快速存档/读档/列出/删除
- **数据导出**: JSON 格式导出 blocks/state/tile/units

## 编译部署

```bash
cd eve-assistant/
./gradlew jar
cp build/libs/evaiDesktop.jar ~/文档/Mindustry/server/config/mods/
# 重启服务端
```

## 许可

MIT