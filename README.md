<div align="center">

# ⚙️ BC-Pipe

**经典 BuildCraft 管道系统 — 移植 NeoForge 1.21.1**

[![NeoForge](https://img.shields.io/badge/NeoForge-1.21.1-brightgreen?style=flat-square)](https://neoforged.net)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-green?style=flat-square)](https://www.minecraft.net)
[![Java](https://img.shields.io/badge/Java-21-red?style=flat-square)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE)
[![Version](https://img.shields.io/badge/Version-1.5.5-orange?style=flat-square)]()

</div>

---

> ⚡ 基于 [Buildcraft-Legacy](https://github.com/Thepigcat76/Buildcraft-Legacy) 修改，针对个人使用进行优化

## 功能概览

| 模块 | 功能 |
|------|------|
| 🔧 管道系统 | 11 种物品运输管道，无需能源，插上即用 |
| 🛢️ 储罐 | 大容量流体存储，支持堆叠与桶交互 |
| 📦 板条箱 | 超大容量物品存储 |
| 🔩 材料 | 经典齿轮（木/石/铁/金）+ 扳手 |
| ⚙️ 高度可配置 | 管道速度、储罐容量等均可自定义 |

---

## 管道系统

物品运输管道，**无需能源**，插上即用。

### 抽取管道（从容器中拉出物品）

| 管道 | 功能 | 速度 |
|------|------|------|
| 🪵 木质 | 从相邻容器中抽取物品 | 0.4 b/s |
| 💎 绿宝石 | 快速抽取（3.5× 木质） | 1.4 b/s |
| 💠 钻石 | 抽取 + 6 面过滤（每面 9 格） | 1.0 b/s |

### 传输管道

| 管道 | 速度 | 备注 |
|------|------|------|
| 🪨 圆石 / ⬜ 石质 / 💠 石英 / 🏜️ 砂岩 / 🧱 粘土 | 0.6 b/s | 基础传输 |
| ⬛ 铁质 | 0.96 b/s | 中速 |
| ✨ 金质 | 1.2 b/s | 快速 |

### 特殊管道

| 管道 | 功能 |
|------|------|
| ⬛ 虚空 | 直接销毁物品 |

> 💡 所有管道速度可通过配置文件自定义（单位：blocks/s）

### 钻石管道过滤系统

钻石管道是管道系统的核心 — 支持 **6 面独立过滤**：

- 每个面 **9 个过滤槽**
- 精确控制物品走向
- 空过滤槽 = 该面接受所有物品
- 支持从容器中按过滤规则抽取

---

## 储罐

大容量流体存储方块。

- **容量**：8,000 mB（可配置，范围 1 ~ 1,000,000）
- **堆叠**：垂直放置多个自动合并，容量叠加
- **桶交互**：右键用桶装/卸流体
- **可搬运**：破坏时保留流体内含物（需配置 `tankRetainFluids: true`）
- **管道连接**：支持自动流体输入/输出
- **流体渲染**：实时显示内容量

---

## 板条箱

超大容量物品存储方块。

- **容量**：4,096 个物品（可配置，范围 1 ~ 1,000,000）
- **快速取物**：左键取出 1 个 / 潜行+左键取出一组
- **可搬运**：破坏时保留所有物品（需配置 `crateRetainItems: true`）

---

## 材料物品

| 物品 | 用途 |
|------|------|
| 🔧 扳手 | 拆卸和调整方块 |
| 🪵 木质齿轮 | 基础合成材料 |
| 🪨 石质齿轮 | 进阶合成材料 |
| ⬛ 铁质齿轮 | 高级合成材料 |
| ✨ 金质齿轮 | 顶级合成材料 |

---

## 配置

所有配置项在 `config/buildcraft-common.toml` 中调整：

```toml
# 管道速度 (blocks/s)
basicPipeSpeed     = 0.6    # 基础管道
woodenPipeSpeed    = 0.4    # 木质管道
goldPipeSpeed      = 1.2    # 金质管道
diamondPipeSpeed   = 1.0    # 钻石管道
ironPipeSpeed      = 0.96   # 铁质管道
voidPipeSpeed      = 1.0    # 虚空管道
emeraldPipeSpeed   = 1.4    # 绿宝石管道

# 储罐 & 板条箱
tankCapacity       = 8000   # 储罐容量 (mB)
crateItemCapacity  = 4096   # 板条箱容量
tankRetainFluids   = true   # 破坏保留流体
crateRetainItems   = true   # 破坏保留物品
```

---

## 构建

### 环境要求

| 项目 | 版本 |
|------|------|
| Minecraft | 1.21.1 |
| NeoForge | ≥ 21.1.209 |
| Java | 21 |

### 构建步骤

```bash
git clone https://github.com/xiaomo-xxx/bc-pipe.git
cd bc-pipe
./gradlew build
```

构建产物位于 `build/libs/` 目录。

---

## 依赖

| 依赖 | 版本 | 必须？ |
|------|------|--------|
| NeoForge | ≥ 21.1.209 | ✅ |
| JEI | 19.21.0.247 | ❌ 可选 |

> 💡 Porting Dead Libs 已内置，无需额外安装

---

## 致谢

- [Buildcraft-Legacy](https://github.com/Thepigcat76/Buildcraft-Legacy) — 原始项目
- [Porting Dead Mods](https://github.com/Porting-Dead-Mods) — Porting Dead Libs API
- [BuildCraft](https://www.buildcraft.net/) — 经典 BuildCraft 原版 mod

---

<div align="center">

**觉得有用就给个 ⭐ Star 吧！**

</div>
