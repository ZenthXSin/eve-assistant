# Eve Assistant Mod — 辅助 Mod 完整规范

> 一个 Kotlin 调试辅助模组，用于**游戏内测试 Kotlin Mod**。
>
> **交互模式**：HTTP API。Mod 启动时在 `0.0.0.0:18090` 开启内嵌 HTTP 服务器。
> 我（CowAgent）通过 `web_fetch("http://192.168.0.108:18090/api/run?cmd=...")` 调用，JSON 响应。
>
> **兜底通道**：游戏内聊天命令 `/eve xxx`，Mod 处理后聊天框返回结果。和 HTTP API 走同一套后端。
>
> **防火墙/网络**：目前容器能打通 `192.168.0.108:3000`，说明同端口段可通。冷启动时 Mod 未运行则连接被拒，正常。

---

## 一、来源确认（源码已验证，下略）

> 同 v2 版本，API 路径照旧。

---

## 二、HTTP API 端点

### 基础端点

| 端点 | 方法 | 说明 |
|------|------|------|
| `GET /api/run?cmd=<urlencoded_command>` | GET | 执行任意命令，返回 JSON |
| `GET /api/status` | GET | 游戏状态快照 |
| `GET /api/inspect?x=<x>&y=<y>` | GET | 方块结构化数据 |
| `GET /api/events?since=<lastId>` | GET | 事件轮询 |
| `GET /api/state` | GET | 全部游戏规则 |

### 统一响应格式

```json
// 成功
{"ok": true, "data": {...}, "message": "Sandbox mode enabled", "timestamp": 12345}

// 失败
{"ok": false, "error": "Block not found: my-fake-block", "timestamp": 12345}
```

### `GET /api/run` 命令清单

和聊天命令完全一致，参数以 URL 编码放在 `cmd` 参数里。

---

## 三、功能清单

### 3.1 基础功能

| 命令 | HTTP 调用 | 说明 |
|------|-----------|------|
| `help [cmd]` | `GET /api/run?cmd=help` | 帮助 |
| `status` | `GET /api/run?cmd=status` | 状态概览 |
| `whereami` | `GET /api/run?cmd=whereami` | 坐标 |
| `clear` | `GET /api/run?cmd=clear` | 清聊天 |
| `version` | `GET /api/run?cmd=version` | 版本号 |

### 3.2 内容查询

| 命令 | HTTP 调用 |
|------|-----------|
| `list blocks [filter]` | `GET /api/run?cmd=list%20blocks%20crystal` |
| `list items` | `GET /api/run?cmd=list%20items` |
| `list liquids` | `GET /api/run?cmd=list%20liquids` |
| `list units` | `GET /api/run?cmd=list%20units` |
| `list bullets` | `GET /api/run?cmd=list%20bullets` |
| `list status` | `GET /api/run?cmd=list%20status` |
| `list turrets` | `GET /api/run?cmd=list%20turrets` |
| `list storage` | `GET /api/run?cmd=list%20storage` |
| `list crafter` | `GET /api/run?cmd=list%20crafter` |
| `list walls` | `GET /api/run?cmd=list%20walls` |
| `list conveyors` | `GET /api/run?cmd=list%20conveyors` |
| `list drills` | `GET /api/run?cmd=list%20drills` |
| `list mod` | `GET /api/run?cmd=list%20mod` |
| `info [name]` | `GET /api/run?cmd=info%20my-block` |

### 3.3 世界探测

| 命令 | HTTP 调用 |
|------|-----------|
| `scan [radius]` | `GET /api/run?cmd=scan%2020` |
| `scan resources` | `GET /api/run?cmd=scan%20resources` |
| `scan liquids` | `GET /api/run?cmd=scan%20liquids` |
| `check x y` | `GET /api/run?cmd=check%2050%2050` |
| `check tile x y` | `GET /api/run?cmd=check%20tile%2050%2050` |
| `check items x y` | `GET /api/run?cmd=check%20items%2050%2050` |
| `check power x y` | `GET /api/run?cmd=check%20power%2050%2050` |
| `check liquid x y` | `GET /api/run?cmd=check%20liquid%2050%2050` |
| `check health x y` | `GET /api/run?cmd=check%20health%2050%2050` |
| `check enemies [r]` | `GET /api/run?cmd=check%20enemies%2020` |
| `check allies [r]` | `GET /api/run?cmd=check%20allies%2020` |
| `map` | `GET /api/run?cmd=map` |

### 3.4 调试规则

| 命令 | HTTP 调用 |
|------|-----------|
| `debug sandbox` | `GET /api/run?cmd=debug%20sandbox` |
| `debug instant` | `GET /api/run?cmd=debug%20instant` |
| `debug speed [n]` | `GET /api/run?cmd=debug%20speed%205` |
| `debug damage [n]` | `GET /api/run?cmd=debug%20damage%203` |
| `debug health [n]` | `GET /api/run?cmd=debug%20health%2010` |
| `debug mine [n]` | `GET /api/run?cmd=debug%20mine%205` |
| `debug buildcost [n]` | `GET /api/run?cmd=debug%20buildcost%200.1` |
| `debug refund [n]` | `GET /api/run?cmd=debug%20refund%201` |
| `debug unitdmg [n]` | `GET /api/run?cmd=debug%20unitdmg%202` |
| `debug unithp [n]` | `GET /api/run?cmd=debug%20unithp%202` |
| `debug unitspeed [n]` | `GET /api/run?cmd=debug%20unitspeed%202` |
| `debug fire on/off` | `GET /api/run?cmd=debug%20fire%20on` |
| `debug reactor on/off` | `GET /api/run?cmd=debug%20reactor%20off` |
| `debug waves on/off` | `GET /api/run?cmd=debug%20waves%20off` |
| `debug nospawn on/off` | `GET /api/run?cmd=debug%20nospawn%20on` |
| `debug edit` | `GET /api/run?cmd=debug%20edit` |
| `debug reset` | `GET /api/run?cmd=debug%20reset` |

### 3.5 低级操作

| 命令 | HTTP 调用 |
|------|-----------|
| `set x y [field] [val]` | `GET /api/run?cmd=set%2050%2050%20powerCapacity%205000` |
| `set items x y [item] [amt]` | `GET /api/run?cmd=set%20items%2050%2050%20copper%20999` |
| `set liquid x y [liq] [amt]` | `GET /api/run?cmd=set%20liquid%2050%2050%20water%20500` |
| `set power x y [amt]` | `GET /api/run?cmd=set%20power%2050%2050%205000` |
| `set health x y [amt]` | `GET /api/run?cmd=set%20health%2050%2050%20500` |
| `set team x y [team]` | `GET /api/run?cmd=set%20team%2050%2050%20blue` |
| `set config x y [cfg]` | `GET /api/run?cmd=set%20config%2050%2050%20@router` |
| `set enabled x y on/off` | `GET /api/run?cmd=set%20enabled%2050%2050%20on` |
| `set warmup x y [0-1]` | `GET /api/run?cmd=set%20warmup%2050%2050%200.5` |
| `set progress x y [amt]` | `GET /api/run?cmd=set%20progress%2050%2050%200.5` |
| `nuke x y [radius]` | `GET /api/run?cmd=nuke%2050%2050%2010` |

### 3.6 事件调试

| 命令 | HTTP 调用 |
|------|-----------|
| `listen [event]` | `GET /api/run?cmd=listen%20BlockBuildEnd` |
| `listen all` | `GET /api/run?cmd=listen%20all` |
| `listen stop [event]` | `GET /api/run?cmd=listen%20stop%20BlockBuildEnd` |
| `listen stop all` | `GET /api/run?cmd=listen%20stop%20all` |
| `listen list` | `GET /api/run?cmd=listen%20list` |
| `fire [event] [args]` | `GET /api/run?cmd=fire%20BlockBuildEnd` |

### 3.7 建造拆除

| 命令 | HTTP 调用 |
|------|-----------|
| `build [block] x y [rotate]` | `GET /api/run?cmd=build%20my-block%2050%2050` |
| `build area [b] x1 y1 x2 y2` | `GET /api/run?cmd=build%20area%20copper-wall%2040%2040%2060%2060` |
| `build line [b] x1 y1 x2 y2` | `GET /api/run?cmd=build%20line%20conveyor%2040%2050%2080%2050` |
| `build ring [b] cx cy r` | `GET /api/run?cmd=build%20ring%20scatter%2050%2050%205` |
| `break x y` | `GET /api/run?cmd=break%2050%2050` |
| `break area x1 y1 x2 y2` | `GET /api/run?cmd=break%20area%2040%2040%2060%2060` |
| `build queue [file]` | `GET /api/run?cmd=build%20queue%20test-layout` |

### 3.8 蓝图

| 命令 | HTTP 调用 |
|------|-----------|
| `schem create [name] x1 y1 x2 y2` | `GET /api/run?cmd=schem%20create%20test-rig%2040%2040%2060%2060` |
| `schem place [name] x y [rotate]` | `GET /api/run?cmd=schem%20place%20test-rig%20100%20100` |
| `schem list` | `GET /api/run?cmd=schem%20list` |
| `schem delete [name]` | `GET /api/run?cmd=schem%20delete%20test-rig` |
| `schem rename [old] [new]` | `GET /api/run?cmd=schem%20rename%20test-rig%20rig-v2` |
| `schem export [name]` | `GET /api/run?cmd=schem%20export%20test-rig` |
| `schem import [base64] [name]` | `GET /api/run?cmd=schem%20import%20bXNjaA...` |

### 3.9 单位生成

| 命令 | HTTP 调用 |
|------|-----------|
| `spawn [unit] x y [team] [amt]` | `GET /api/run?cmd=spawn%20my-unit%20100%20100%20blue%205` |
| `spawn enemy [unit] x y [amt]` | `GET /api/run?cmd=spawn%20enemy%20eradicator%20100%20100%203` |
| `spawn ally [unit] x y [amt]` | `GET /api/run?cmd=spawn%20ally%20flare%2050%2050` |
| `spawn at [unit] [amt]` | `GET /api/run?cmd=spawn%20at%20flare%205` |
| `control [unit]` | `GET /api/run?cmd=control%20my-unit` |
| `control` | `GET /api/run?cmd=control` |
| `kill all [radius]` | `GET /api/run?cmd=kill%20all%2050` |
| `kill enemies [radius]` | `GET /api/run?cmd=kill%20enemies%2050` |
| `kill allies [radius]` | `GET /api/run?cmd=kill%20allies%2050` |

### 3.10 JS 控制台

| 命令 | HTTP 调用 |
|------|-----------|
| `js [code]` | `GET /api/run?cmd=js%20Vars.state.wave` |
| `logic [code]` | `GET /api/run?cmd=logic%20op%20add%20result%201%202` |
| `eval [expr]` | `GET /api/run?cmd=eval%202%2B2` |

### 3.11 存档

| 命令 | HTTP 调用 |
|------|-----------|
| `save [name]` | `GET /api/run?cmd=save%20before-test` |
| `load [name]` | `GET /api/run?cmd=load%20before-test` |
| `save list` | `GET /api/run?cmd=save%20list` |
| `save delete [name]` | `GET /api/run?cmd=save%20delete%20before-test` |
| `save current` | `GET /api/run?cmd=save%20current` |

### 3.12 数据导出

| 命令 | HTTP 调用 |
|------|-----------|
| `export blocks [filter]` | `GET /api/run?cmd=export%20blocks%20crystal` |
| `export state` | `GET /api/run?cmd=export%20state` |
| `export tile x y` | `GET /api/run?cmd=export%20tile%2050%2050` |
| `export units [r]` | `GET /api/run?cmd=export%20units%2050` |

---

## 四、模块结构 ~31 Kotlin 文件

```
project/evai/
├── build.gradle
├── mod.hjson
└── src/evai/
    ├── EveMod.kt                # init() 启动 HTTP 服务 + 注册命令
    │
    ├── http/
    │   ├── HttpServer.kt        # HTTP 服务器 (com.sun.net.httpserver)
    │   ├── Handlers.kt          # /api/run /api/status /api/inspect /api/events /api/state
    │   └── JsonUtil.kt          # JSON 构建（手动，无 Gson）
    │
    ├── command/
    │   ├── CommandRegistry.kt   # 路由 + 参数解析
    │   ├── CommandResult.kt     # 返回结构
    │   ├── BaseCommands.kt      # help/status/whereami/clear/version
    │   ├── InfoCommands.kt      # list/info
    │   ├── ScanCommands.kt      # scan/check/map
    │   ├── DebugCommands.kt     # debug 系列
    │   ├── SetCommands.kt       # set 系列 + nuke
    │   ├── BuildCommands.kt     # build/break
    │   ├── SchemCommands.kt     # schem 系列
    │   ├── SpawnCommands.kt     # spawn/control/kill
    │   ├── EventCommands.kt     # listen/fire
    │   ├── JsCommands.kt        # js/logic/eval
    │   ├── SaveCommands.kt      # save/load
    │   └── ExportCommands.kt    # export 系列
    │
    ├── monitor/
    │   ├── EventMonitor.kt      # 事件注册 + 缓冲
    │   └── EventBuffer.kt       # 环形缓冲区（供 /api/events 轮询）
    │
    ├── inspector/
    │   ├── TileInspector.kt     # Tile/Building 反射
    │   ├── ContentInspector.kt  # Content 查询
    │   └── Exporter.kt          # 序列化导出
    │
    ├── action/
    │   ├── BuildAction.kt       # 建造/拆除
    │   └── AreaOps.kt           # 区域计算
    │
    ├── spawn/
    │   └── Spawner.kt           # 单位生成
    │
    └── ui/
        └── EveChatFormat.kt     # 聊天格式化
```

---

## 五、开发路线

| Phase | 内容 | 文件数 | 交付 |
|-------|------|--------|------|
| **P1 骨架** | Gradle + mod.hjson + EveMod + HTTP 服务 + CommandRegistry + help/status/whereami | 8 | `GET /api/run?cmd=status` 可调通 |
| **P2 内容查询** | InfoCommands + ContentInspector: list + info | 4 | `/api/run?cmd=list%20blocks` 返回列表 |
| **P3 世界探测** | ScanCommands + TileInspector: scan/check 系列 + /api/inspect | 5 | 方块反射数据 |
| **P4 调试规则** | DebugCommands: debug 系列 | 2 | 远程改规则 |
| **P5 低级操作** | SetCommands: set 系列 + nuke | 2 | 反射写字段 |
| **P6 事件调试** | EventCommands + EventMonitor + EventBuffer: listen/fire + /api/events | 4 | 事件轮询 |
| **P7 建造拆除** | BuildCommands + BuildAction + AreaOps | 3 | 远程铺方块 |
| **P8 蓝图** | SchemCommands | 2 | 蓝图 CRUD |
| **P9 单位生成** | SpawnCommands + Spawner | 2 | 远程生单位 |
| **P10 JS+存档+导出** | JsCommands + SaveCommands + ExportCommands | 4 | 远程执行代码 |

总计 ~36 个 Kotlin 文件，~3500 行。

---

## 六、部署方式

```bash
cd ~/文档/Mindustry/EveAssistant    # 或 project/evai/
./gradlew jar
cp build/libs/EveAssistant.jar ~/文档/Mindustry/server/config/mods/
# 重启服务端
```

`mod.hjson` 需要：
```hjson
displayName: "Eve Assistant"
name: "evai"
author: "zxs"
main: "evai.EveMod"
version: 1.0
minGameVersion: 154
multiplayerCompatible: true
```