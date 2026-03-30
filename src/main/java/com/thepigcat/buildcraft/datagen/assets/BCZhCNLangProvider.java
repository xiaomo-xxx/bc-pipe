package com.thepigcat.buildcraft.datagen.assets;

import com.thepigcat.buildcraft.BuildcraftLegacy;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class BCZhCNLangProvider extends LanguageProvider {
    public BCZhCNLangProvider(PackOutput output) {
        super(output, BuildcraftLegacy.MODID, "zh_cn");
    }

    @Override
    protected void addTranslations() {
        // 物品
        add("item.buildcraft.wrench", "扳手");
        add("item.buildcraft.wooden_gear", "木质齿轮");
        add("item.buildcraft.stone_gear", "石质齿轮");
        add("item.buildcraft.iron_gear", "铁质齿轮");
        add("item.buildcraft.gold_gear", "金质齿轮");

        // 方块（BlockItem使用 block. 前缀）
        add("block.buildcraft.crate", "板条箱");
        add("block.buildcraft.tank", "储罐");

        // 管道（BlockItem使用 block. 前缀）
        add("block.buildcraft.wooden_pipe", "木质管道");
        add("block.buildcraft.cobblestone_pipe", "圆石管道");
        add("block.buildcraft.stone_pipe", "石质管道");
        add("block.buildcraft.quartz_pipe", "石英管道");
        add("block.buildcraft.sandstone_pipe", "砂岩管道");
        add("block.buildcraft.gold_pipe", "金质管道");
        add("block.buildcraft.iron_pipe", "铁质管道");
        add("block.buildcraft.clay_pipe", "粘土管道");
        add("block.buildcraft.diamond_pipe", "钻石管道");
        add("block.buildcraft.void_pipe", "虚空管道");
        add("block.buildcraft.emerald_pipe", "绿宝石管道");

        // 创造模式标签页
        add("itemGroup.buildcraft.bc_tab", "BuildCraft");

        // 红石信号类型
        add("redstone_signal_type.buildcraft.ignored", "忽略");
        add("redstone_signal_type.buildcraft.low_signal", "低信号");
        add("redstone_signal_type.buildcraft.high_signal", "高信号");

        // 配置项
        add("buildcraft.configuration.capacity.fluid", "流体容量");
        add("buildcraft.configuration.capacity.energy", "能量容量");
        add("buildcraft.configuration.capacity.items", "物品容量");
        add("buildcraft.configuration.pipes.speed", "管道速度");
        add("buildcraft.configuration.tank_capacity", "储罐容量");
        add("buildcraft.configuration.tank_retain_fluids", "储罐保留流体");
        add("buildcraft.configuration.crate_item_capacity", "板条箱容量");
        add("buildcraft.configuration.crate_retain_items", "板条箱保留物品");
        add("buildcraft.configuration.basic_pipe_speed", "基础管道速度");
        add("buildcraft.configuration.wooden_pipe_speed", "木质管道速度");
        add("buildcraft.configuration.gold_pipe_speed", "金质管道速度");
        add("buildcraft.configuration.diamond_pipe_speed", "钻石管道速度");
        add("buildcraft.configuration.void_pipe_speed", "虚空管道速度");
    }
}
