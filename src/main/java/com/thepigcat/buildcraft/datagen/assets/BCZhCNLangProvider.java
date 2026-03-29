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
        add("item.buildcraft.diamond_gear", "钻石齿轮");
        add("item.buildcraft.oil_bucket", "石油桶");

        // 方块
        add("block.buildcraft.crate", "板条箱");
        add("block.buildcraft.tank", "储罐");
        add("block.buildcraft.oil", "石油");

        // 引擎
        add("block.buildcraft.redstone_engine", "红石引擎");
        add("block.buildcraft.stirling_engine", "斯特林引擎");
        add("block.buildcraft.combustion_engine", "燃烧引擎");

        // 管道
        add("item.buildcraft.wooden_pipe", "木质管道");
        add("item.buildcraft.cobblestone_pipe", "圆石管道");
        add("item.buildcraft.stone_pipe", "石质管道");
        add("item.buildcraft.quartz_pipe", "石英管道");
        add("item.buildcraft.sandstone_pipe", "砂岩管道");
        add("item.buildcraft.gold_pipe", "金质管道");
        add("item.buildcraft.iron_pipe", "铁质管道");
        add("item.buildcraft.clay_pipe", "粘土管道");
        add("item.buildcraft.diamond_pipe", "钻石管道");
        add("item.buildcraft.void_pipe", "虚空管道");

        // 创造模式标签页
        add("itemGroup.buildcraft.bc_tab", "BuildCraft");

        // 流体
        add("fluid_type.buildcraft.oil", "石油");

        // 红石信号类型
        add("redstone_signal_type.buildcraft.ignored", "忽略");
        add("redstone_signal_type.buildcraft.low_signal", "低信号");
        add("redstone_signal_type.buildcraft.high_signal", "高信号");
    }
}
