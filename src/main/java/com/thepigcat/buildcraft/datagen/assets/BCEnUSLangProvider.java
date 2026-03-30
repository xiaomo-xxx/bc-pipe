package com.thepigcat.buildcraft.datagen.assets;

import com.thepigcat.buildcraft.BuildcraftLegacy;
import com.thepigcat.buildcraft.api.blockentities.RedstoneBlockEntity;
import com.thepigcat.buildcraft.registries.BCBlocks;
import com.thepigcat.buildcraft.registries.BCItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class BCEnUSLangProvider extends LanguageProvider {
    public BCEnUSLangProvider(PackOutput output) {
        super(output, BuildcraftLegacy.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        addItem(BCItems.WRENCH, "Wrench");
        addItem(BCItems.WOODEN_GEAR, "Wooden Gear");
        addItem(BCItems.STONE_GEAR, "Stone Gear");
        addItem(BCItems.IRON_GEAR, "Iron Gear");
        addItem(BCItems.GOLD_GEAR, "Gold Gear");
        addBlock(BCBlocks.CRATE, "Crate");
        addBlock(BCBlocks.TANK, "Tank");

        add("itemGroup.buildcraft.bc_tab", "Buildcraft");

        // Pipe display names (dynamically registered - use block. prefix for BlockItem)
        add("block.buildcraft.wooden_pipe", "Wooden Pipe");
        add("block.buildcraft.cobblestone_pipe", "Cobblestone Pipe");
        add("block.buildcraft.stone_pipe", "Stone Pipe");
        add("block.buildcraft.quartz_pipe", "Quartz Pipe");
        add("block.buildcraft.sandstone_pipe", "Sandstone Pipe");
        add("block.buildcraft.gold_pipe", "Gold Pipe");
        add("block.buildcraft.iron_pipe", "Iron Pipe");
        add("block.buildcraft.clay_pipe", "Clay Pipe");
        add("block.buildcraft.diamond_pipe", "Diamond Pipe");
        add("block.buildcraft.void_pipe", "Void Pipe");
        add("block.buildcraft.emerald_pipe", "Emerald Pipe");

        addRedstoneSignalType(RedstoneBlockEntity.RedstoneSignalType.IGNORED, "Ignored");
        addRedstoneSignalType(RedstoneBlockEntity.RedstoneSignalType.LOW_SIGNAL, "Low Signal");
        addRedstoneSignalType(RedstoneBlockEntity.RedstoneSignalType.HIGH_SIGNAL, "High Signal");
    }

    private void addRedstoneSignalType(RedstoneBlockEntity.RedstoneSignalType signalType, String translation) {
        add("redstone_signal_type."+BuildcraftLegacy.MODID+"."+signalType.getSerializedName(), translation);
    }
}

