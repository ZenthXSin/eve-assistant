# Eve Assistant 技能

## 用途

用户在游戏（Mindustry 服务端）中运行了 **Eve Assistant Mod**。当用户需要测试自定义 Mod 内容、修改游戏参数、查询方块状态时，通过本技能与游戏交互。

## 触发条件

用户说以下类似的话时激活本技能：
- "测一下我的 xxx"
- "帮我建个 xxx"
- "看看 xxx 在坐标"
- "调 xxx 参数"
- "给 xx 方块加 xx"

不需要每次都问用户服务端开了没——调不通会有连接失败，告知用户即可。

## 通信方式

Mod 在 `192.168.0.108:18090` 上监听 HTTP 请求。

**调用方式**：`web_fetch("http://192.168.0.108:18090/api/run?cmd=<URL_ENCODED_COMMAND>")`

**响应格式**：`{"ok":true,"data":{...},"message":"...","timestamp":12345}`

## 端点

| 端点 | 说明 |
|------|------|
| `GET /api/run?cmd=<cmd>` | 执行任意命令，主入口 |
| `GET /api/status` | 游戏状态快照 |
| `GET /api/inspect?x=<x>&y=<y>` | 方块结构化数据 |
| `GET /api/state` | 全部游戏规则 |

## 命令速查

见项目 `PLAN.md` 第三节完整清单。

## 典型工作流

1. 检查注册: `GET /api/run?cmd=list%20blocks%20<name>`
2. 查看字段: `GET /api/run?cmd=info%20<name>`
3. 开沙盒: `GET /api/run?cmd=debug%20sandbox`
4. 铺地图: `GET /api/run?cmd=build%20<name>%2050%2050`
5. 看状态: `GET /api/run?cmd=check%2050%2050`
6. 改参数: `GET /api/run?cmd=set%2050%2050%20<field>%20<value>`

## 处理失败

- 连接被拒 → 告诉用户 Mod 没运行
- `ok: false` → 输出 `error` 字段给用户
- 命令不存在 → 检查是否未实现