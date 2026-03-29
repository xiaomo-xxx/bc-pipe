package com.thepigcat.buildcraft.datagen.assets;

import com.portingdeadmods.portingdeadlibs.api.config.PDLConfigHelper;
import com.thepigcat.buildcraft.BCConfig;
import com.thepigcat.buildcraft.BuildcraftLegacy;
import com.thepigcat.buildcraft.api.blockentities.RedstoneBlockEntity;
import com.thepigcat.buildcraft.registries.BCBlocks;
import com.thepigcat.buildcraft.registries.BCFluids;
import com.thepigcat.buildcraft.registries.BCItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class BCEnUSLangProvider extends LanguageProvider {
    public BCEnUSLangProvider(PackOutput output) {
        super(output, BuildcraftLegacy.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        PDLConfigHelper.generateConfigNames(BCConfig.class, BuildcraftLegacy.MODID, this::add);

        addItem(BCItems.WRENCH, "Wrench");
        addItem(BCItems.WOODEN_GEAR, "Wooden Gear");
        addItem(BCItems.STONE_GEAR, "Stone Gear");
        addItem(BCItems.IRON_GEAR, "Iron Gear");
        addItem(BCItems.GOLD_GEAR, "Gold Gear");
        addItem(BCItems.DIAMOND_GEAR, "Diamond Gear");
        addItem(BCFluids.OIL.getDeferredBucket(), "Oil Bucket");

        addBlock(BCBlocks.CRATE, "Crate");
        addBlock(BCBlocks.TANK, "Tank");
        addBlock(BCFluids.OIL.block, "Oil");

        addBlock(BCBlocks.REDSTONE_ENGINE, "Redstone Engine");
        addBlock(BCBlocks.STIRLING_ENGINE, "Stirling Engine");
        addBlock(BCBlocks.COMBUSTION_ENGINE, "Combustion Engine");

        addFluidType(BCFluids.OIL.fluidType, "Oil");

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

        addRedstoneSignalType(RedstoneBlockEntity.RedstoneSignalType.IGNORED, "Ignored");
        addRedstoneSignalType(RedstoneBlockEntity.RedstoneSignalType.LOW_SIGNAL, "Low Signal");
        addRedstoneSignalType(RedstoneBlockEntity.RedstoneSignalType.HIGH_SIGNAL, "High Signal");
    }

    private void addRedstoneSignalType(RedstoneBlockEntity.RedstoneSignalType signalType, String translation) {
        add("redstone_signal_type."+BuildcraftLegacy.MODID+"."+signalType.getSerializedName(), translation);
    }

    private void addFluidType(Supplier<FluidType> fluidTypeSupplier, String translation) {
        ResourceLocation location = NeoForgeRegistries.FLUID_TYPES.getKey(fluidTypeSupplier.get());
        String fluidTypeName = location.getPath();
        String modid = location.getNamespace();
        add("fluid_type."+modid+"."+fluidTypeName, translation);
    }
}
