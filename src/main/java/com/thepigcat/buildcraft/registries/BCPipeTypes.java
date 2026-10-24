package com.thepigcat.buildcraft.registries;

import com.thepigcat.buildcraft.BuildcraftLegacy;
import com.thepigcat.buildcraft.api.pipes.PipeTypeHolder;
import com.thepigcat.buildcraft.content.blocks.DiamondItemPipeBlock;
import com.thepigcat.buildcraft.content.blocks.ExtractingFluidPipeBlock;
import com.thepigcat.buildcraft.content.blocks.ExtractingItemPipeBlock;
import com.thepigcat.buildcraft.content.blocks.FluidPipeBlock;
import com.thepigcat.buildcraft.content.blocks.ItemPipeBlock;
import com.thepigcat.buildcraft.content.blocks.VoidFluidPipeBlock;
import com.thepigcat.buildcraft.content.blocks.VoidItemPipeBlock;
import com.thepigcat.buildcraft.content.items.blocks.ItemPipeBlockItem;
import com.thepigcat.buildcraft.util.ModelUtils;
import com.thepigcat.buildcraft.util.PipeRegistrationHelper;

public final class BCPipeTypes {
    public static final PipeRegistrationHelper HELPER = new PipeRegistrationHelper(BuildcraftLegacy.MODID);

    public static final PipeTypeHolder<ItemPipeBlock, ItemPipeBlockItem> DEFAULT = HELPER.registerPipeType("default", ItemPipeBlock::new, ItemPipeBlockItem::new,
            ModelUtils.DEFAULT_BLOCK_MODEL_DEFINITION, ModelUtils.DEFAULT_BLOCK_MODEL_FILE, ModelUtils.DEFAULT_ITEM_MODEL_FILE,
            "base", "connection");
    public static final PipeTypeHolder<ExtractingItemPipeBlock, ItemPipeBlockItem> EXTRACTING = HELPER.registerPipeType("extracting", ExtractingItemPipeBlock::new, ItemPipeBlockItem::new,
            ModelUtils.EXTRACTING_BLOCK_MODEL_DEFINITION, ModelUtils.DEFAULT_BLOCK_MODEL_FILE, ModelUtils.DEFAULT_ITEM_MODEL_FILE,
            "base", "connection", "connection_extracting");
    public static final PipeTypeHolder<VoidItemPipeBlock, ItemPipeBlockItem> VOID = HELPER.registerPipeType("void", VoidItemPipeBlock::new, ItemPipeBlockItem::new,
            ModelUtils.DEFAULT_BLOCK_MODEL_DEFINITION, ModelUtils.DEFAULT_BLOCK_MODEL_FILE, ModelUtils.DEFAULT_ITEM_MODEL_FILE,
            "base", "connection");

    public static final PipeTypeHolder<DiamondItemPipeBlock, ItemPipeBlockItem> DIAMOND = HELPER.registerPipeType("diamond", DiamondItemPipeBlock::new, ItemPipeBlockItem::new,
            ModelUtils.EXTRACTING_BLOCK_MODEL_DEFINITION, ModelUtils.DEFAULT_BLOCK_MODEL_FILE, ModelUtils.DEFAULT_ITEM_MODEL_FILE,
            "base", "connection", "connection_extracting");

    // === 液体管道 ===
    public static final PipeTypeHolder<FluidPipeBlock, ItemPipeBlockItem> FLUID = HELPER.registerPipeType("fluid", FluidPipeBlock::new, ItemPipeBlockItem::new,
            ModelUtils.DEFAULT_BLOCK_MODEL_DEFINITION, ModelUtils.DEFAULT_BLOCK_MODEL_FILE, ModelUtils.DEFAULT_ITEM_MODEL_FILE,
            "base", "connection");

    public static final PipeTypeHolder<ExtractingFluidPipeBlock, ItemPipeBlockItem> FLUID_EXTRACTING = HELPER.registerPipeType("fluid_extracting", ExtractingFluidPipeBlock::new, ItemPipeBlockItem::new,
            ModelUtils.EXTRACTING_BLOCK_MODEL_DEFINITION, ModelUtils.DEFAULT_BLOCK_MODEL_FILE, ModelUtils.DEFAULT_ITEM_MODEL_FILE,
            "base", "connection", "connection_extracting");

    public static final PipeTypeHolder<VoidFluidPipeBlock, ItemPipeBlockItem> FLUID_VOID = HELPER.registerPipeType("fluid_void", VoidFluidPipeBlock::new, ItemPipeBlockItem::new,
            ModelUtils.DEFAULT_BLOCK_MODEL_DEFINITION, ModelUtils.DEFAULT_BLOCK_MODEL_FILE, ModelUtils.DEFAULT_ITEM_MODEL_FILE,
            "base", "connection");

    public static void init() {
    }
}
