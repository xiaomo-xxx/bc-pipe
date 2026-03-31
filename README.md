<div align="center">

# ⚙️ BC-Pipe

**经典 BuildCraft 管道系统 — 移植 NeoForge 1.21.1**

[![NeoForge](https://img.shields.io/badge/NeoForge-1.21.1-brightgreen?style=for-the-badge&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAOCAYAAAAfSC3RAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAADhSURBVHgBpZK9DcIwEEXfURQpUjFASsYIjMAIjJAhIzBCRkiRIkVGIJBg+5ycnIQI+ZXP2e/52WcLwD+HJR4i4hGR4mxiZp+ZfWa2MrNVRC4i8hSRS0QuIrKOyC0iz8zs3uR7J9NJJavUd/s8m8xmYHqT7w1M7/K9geldvjcwfcj3BqZP+d7A9CXfG5i+5XsD0498b2D6le8NTH/yvYHpX743MA3I9wamQ/newDQk3xuYhuV7A9OIfG9gGpXvDUxj8r2BaVy+NzBNyPcGpkn53sA0Jd8bmKblewPTjHxvYJqV7w1Mc/K9gWlevjcwLcj3BqZF+d7AtCTfG5iW5XsD04p8b2D6BuG3QIx+c8yOAAAAAElFTkSuQmCC)](https://neoforged.net)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-green?style=for-the-badge&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAOCAYAAAAfSC3RAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAAEBSURBVHgBdZO7DcIwEEXf0lAxFR0jMEKmYARESMcIjJApI1AwAiMQLH6d+MmJFcW3fu/svX0O4J+DuoKIuIjIICI3ETlGZBeRdUTWETlE5BKRq4isIrKKyDEiZxF5ZGa7mX27zvLf3WcmZraZ2W5muZl1ZtaaWW1mlZmVZlaYWW5muZllZpaZWWpmsZnFZhaZWWhmgZn5ZuaZmWtmjpk5ZmabmWVmpl48if0i8w/NrGdmpZkVZpaZWWZmqZmlZhb/vxv5ReZHZlabWWVmpZmVZlaZWWVmtZk1ZtaaWfeVX7+Z9WY2mNloZpOZzWa2mNlqZpuZ7WZ2mNn5ld9+MzvN7DKz28weM3vN7DOz38wBMwfNHDRzyMxhM0fMHDVz7Jd/ATjRUIn8U2ZGAAAAAElFTkSuQmCC)](https://www.minecraft.net)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](LICENSE)
[![Version](https://img.shields.io/badge/Version-1.3.0-orange?style=for-the-badge)]()

[![Java](https://img.shields.io/badge/Java-21-red?style=flat-square&logo=data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAOCAYAAAAfSC3RAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAADUSURBVHgBpZK9DcIwEEXf0kR0jMAIjMIIjMAIGSEFIzACI1CQ4tSJT05ChPzK5+z3/OyzA/hn0CgQETcROUXkFJGjiOwjcheRfUT2ETlE5BKRc0QuIrKIyCIi94hcI3Jzz5uZvXe+J5jZdOc7wczG4DvBzMbgO8HMxuA7wczG4DvBzMbgO8HMxuA7wczG4DvBzMbgO8HMxuA7wczG4DvBzMbgO8HMxuA7wczG4DvBzMbgO8HMxuA7wczG4DvBzMbgO8HMxuA7wczG4DvBzMbgO8HMxuA7wczG4DvBzMbgO+EJxIBAjFe9SbwAAAAASUVORK5CYII=)](https://www.oracle.com/java/)
[![Gradle](https://img.shields.io/badge/Gradle-8.x-02303A?style=flat-square&logo=gradle)](https://gradle.org/)

**[🇺🇸 English](#features) · [📖 文档](#详细功能) · [🔨 构建](#构建)**

</div>

---

> ⚡ 基于 [Buildcraft-Legacy](https://github.com/Thepigcat76/Buildcraft-Legacy) 修改，针对个人使用进行优化

## ✨ Features / 功能概览

| 模块 | 功能 |
|------|------|
| 🔧 **管道系统** | 11 种物品运输管道，无需能源，插上即用 |
| 🛢️ **储罐** | 大容量流体存储，支持堆叠与桶交互 |
| 📦 **板条箱** | 超大容量物品存储 |
| 🔩 **材料** | 经典齿轮（木/石/铁/金）+ 扳手 |
| ⚙️ **高度可配置** | 管道速度、储罐容量等均可自定义 |

---

## 📖 详细功能

### 🔧 管道系统 (Pipes)

物品运输管道，**无需能源**，插上即用。

#### 抽取管道（从容器中拉出物品）

| 管道 | 功能 | 速度 |
|------|------|------|
| 🪵 **木质管道** | 从相邻容器中抽取物品 | 0.25 blocks/s |
| 💎 **绿宝石管道** | 快速抽取（速度是木质的 2 倍） | 0.5 blocks/s |
| 💠 **钻石管道** | 抽取 + **6 面过滤**（每面 9 格过滤槽） | 0.3 blocks/s |

#### 传输管道（物品运送）

| 管道 | 功能 | 速度 |
|------|------|------|
| 🪨 **圆石管道** | 基础传输 | 0.2 blocks/s |
| ⬜ **石质管道** | 基础传输 | 0.2 blocks/s |
| ⬛ **铁质管道** | 基础传输 | 0.2 blocks/s |
| 💠 **石英管道** | 基础传输 | 0.2 blocks/s |
| 🏜️ **砂岩管道** | 基础传输 | 0.2 blocks/s |
| 🧱 **粘土管道** | 基础传输 | 0.2 blocks/s |
| ✨ **金质管道** | 快速传输（速度 ×2） | 0.4 blocks/s |

#### 特殊管道

| 管道 | 功能 | 速度 |
|------|------|------|
| ⬛ **虚空管道** | 直接销毁物品 | 1.0 blocks/s |

> 💡 所有管道速度可通过配置文件调整（单位：每秒经过的管道段数）

#### 钻石管道过滤系统

钻石管道是管道系统的核心 — 支持 **6 面独立过滤**：
- 每个面有 **9 个过滤槽**
- 可以精确控制物品走向
- 支持从容器中按过滤规则抽取

---

### 🛢️ 储罐 (Tank)

大容量流体存储方块。

- **容量**: 8,000 mB（可通过配置调整，范围 1 ~ 1,000,000）
- **堆叠**: 垂直放置多个储罐自动合并，容量叠加
- **桶交互**: 直接用桶右键装/卸流体
- **可搬运**: 破坏时保留流体内含物（需配置 `tankRetainFluids: true`）
- **管道连接**: 可与管道系统连接，实现自动流体输入/输出
- **流体显示**: 带有实时流体渲染，可直观查看储罐内容量

---

### 📦 板条箱 (Crate)

超大容量物品存储方块。

- **容量**: 4,096 个物品（可通过配置调整，范围 1 ~ 1,000,000）
- **快速取物**: 左键取出 1 个 / 潜行+左键取出一组
- **可搬运**: 破坏时保留所有物品（需配置 `crateRetainItems: true`）

---

### 🔩 材料物品

| 物品 | 用途 |
|------|------|
| 🔧 **扳手 (Wrench)** | 拆卸和调整方块 |
| 🪵 **木质齿轮** | 基础合成材料 |
| 🪨 **石质齿轮** | 进阶合成材料 |
| ⬛ **铁质齿轮** | 高级合成材料 |
| ✨ **金质齿轮** | 顶级合成材料 |

---

## ⚙️ 配置

所有配置项均可在 `config/buildcraft-common.toml` 中调整：

### 管道速度
```toml
basicPipeSpeed = 0.2        # 基础管道（圆石/石/铁/石英/砂岩/粘土）
woodenPipeSpeed = 0.25      # 木质管道（抽取）
goldPipeSpeed = 0.4         # 金质管道（快速）
diamondPipeSpeed = 0.3      # 钻石管道（过滤+抽取）
voidPipeSpeed = 1.0         # 虚空管道（销毁）
emeraldPipeSpeed = 0.5      # 绿宝石管道（快速抽取）
```

### 储罐 & 板条箱
```toml
tankCapacity = 8000         # 储罐容量 (mB)
crateItemCapacity = 4096    # 板条箱容量
tankRetainFluids = true     # 破坏保留流体
crateRetainItems = true     # 破坏保留物品
```

---

## 🔨 构建

### 环境要求
- **Java 21**
- **Gradle 8.x**（已包含 Gradle Wrapper）

### 构建步骤

```bash
git clone https://github.com/xiaomo-xxx/BuildCraft.git
cd BuildCraft
./gradlew build
```

构建产物位于 `build/libs/` 目录。

---

## 📦 依赖

| 依赖 | 版本要求 | 必须？ |
|------|----------|--------|
| **NeoForge** | ≥ 21.1.209 | ✅ 必须 |
| **JEI** | 19.21.0.247 | ❌ 可选（查看配方） |

> 💡 Porting Dead Libs 已内置，无需额外安装

---

## 📋 环境要求

| 项目 | 版本 |
|------|------|
| Minecraft | 1.21.1 |
| NeoForge | ≥ 21.1.209 |
| Java | 21 |

---

## 🙏 致谢

- [Buildcraft-Legacy](https://github.com/Thepigcat76/Buildcraft-Legacy) — 原始项目
- [Porting Dead Mods](https://github.com/Porting-Dead-Mods) — Porting Dead Libs API
- [BuildCraft](https://www.buildcraft.net/) — 经典 BuildCraft 原版 mod

---

<div align="center">

**如果觉得这个项目有用，请给个 ⭐ Star！**

</div>
