# BuildCraft Legacy (NeoForge 1.21.1)

> ⚠️ 仅自用，基于 [Buildcraft-Legacy](https://github.com/Thepigcat76/Buildcraft-Legacy) 修改，只保留管道系统

## 管道

物品运输管道，无需能源，插上即用。

| 管道 | 功能 | 速度 |
|------|------|------|
| 🪵 木质管道 | 从容器中抽取物品 | 0.25 blocks/s |
| 🪨 圆石管道 | 运输物品 | 0.2 blocks/s |
| ⬜ 石质管道 | 运输物品 | 0.2 blocks/s |
| ⬛ 铁质管道 | 运输物品 | 0.2 blocks/s |
| 💎 石英管道 | 运输物品 | 0.2 blocks/s |
| 🏜️ 砂岩管道 | 运输物品 | 0.2 blocks/s |
| 🧱 粘土管道 | 运输物品 | 0.2 blocks/s |
| ✨ 金质管道 | 运输物品 | 0.4 blocks/s |
| 💠 钻石管道 | 过滤 + 抽取（6面，每面 9 格过滤） | 0.3 blocks/s |
| ⬛ 虚空管道 | 直接销毁物品 | 1.0 blocks/s |

速度可通过配置文件调整（单位：每秒经过的管道段数）。

## 依赖

- Minecraft 1.21.1
- NeoForge 21.1.209+

## 构建

```bash
./gradlew build
```
