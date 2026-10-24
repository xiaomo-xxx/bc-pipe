package com.thepigcat.buildcraft.registries;

import com.thepigcat.buildcraft.BuildcraftLegacy;
import com.thepigcat.buildcraft.content.blockentities.CrateBE;
import com.thepigcat.buildcraft.content.blockentities.DiamondItemPipeBE;
import com.thepigcat.buildcraft.content.blockentities.ExtractItemPipeBE;
import com.thepigcat.buildcraft.content.blockentities.ExtractingFluidPipeBE;
import com.thepigcat.buildcraft.content.blockentities.FluidPipeBE;
import com.thepigcat.buildcraft.content.blockentities.ItemPipeBE;
import com.thepigcat.buildcraft.content.blockentities.TankBE;
import com.thepigcat.buildcraft.content.blockentities.VoidFluidPipeBE;
import com.thepigcat.buildcraft.content.blockentities.VoidItemPipeBE;
import com.thepigcat.buildcraft.content.blocks.ExtractingFluidPipeBlock;
import com.thepigcat.buildcraft.content.blocks.ExtractingItemPipeBlock;
import com.thepigcat.buildcraft.content.blocks.FluidPipeBlock;
import com.thepigcat.buildcraft.content.blocks.ItemPipeBlock;
import com.thepigcat.buildcraft.content.blocks.DiamondItemPipeBlock;
import com.thepigcat.buildcraft.content.blocks.VoidFluidPipeBlock;
import com.thepigcat.buildcraft.content.blocks.VoidItemPipeBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class BCBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, BuildcraftLegacy.MODID);

    public static final Supplier<BlockEntityType<ItemPipeBE>> ITEM_PIPE = BLOCK_ENTITIES.register("item_pipe",
            () -> BlockEntityType.Builder.of(ItemPipeBE::new, collectBlocks(ItemPipeBlock.class)).build(null));

    public static final Supplier<BlockEntityType<ExtractItemPipeBE>> EXTRACTING_ITEM_PIPE = BLOCK_ENTITIES.register("extracting_item_pipe",
            () -> BlockEntityType.Builder.of(ExtractItemPipeBE::new, collectBlocks(ExtractingItemPipeBlock.class)).build(null));

    public static final Supplier<BlockEntityType<VoidItemPipeBE>> VOID_ITEM_PIPE = BLOCK_ENTITIES.register("void_item_pipe",
            () -> BlockEntityType.Builder.of(VoidItemPipeBE::new, collectBlocks(VoidItemPipeBlock.class)).build(null));

    public static final Supplier<BlockEntityType<DiamondItemPipeBE>> DIAMOND_ITEM_PIPE = BLOCK_ENTITIES.register("diamond_item_pipe",
            () -> BlockEntityType.Builder.of(DiamondItemPipeBE::new, collectDiamondPipeBlocks()).build(null));

    public static final Supplier<BlockEntityType<TankBE>> TANK = BLOCK_ENTITIES.register("tank",
            () -> BlockEntityType.Builder.of(TankBE::new, BCBlocks.TANK.get()).build(null));
    public static final Supplier<BlockEntityType<CrateBE>> CRATE = BLOCK_ENTITIES.register("crate",
            () -> BlockEntityType.Builder.of(CrateBE::new, BCBlocks.CRATE.get()).build(null));

    // === 液体管道 ===
    public static final Supplier<BlockEntityType<FluidPipeBE>> FLUID_PIPE = BLOCK_ENTITIES.register("fluid_pipe",
            () -> BlockEntityType.Builder.of(FluidPipeBE::new, collectBlocks(FluidPipeBlock.class)).build(null));

    public static final Supplier<BlockEntityType<ExtractingFluidPipeBE>> EXTRACTING_FLUID_PIPE = BLOCK_ENTITIES.register("extracting_fluid_pipe",
            () -> BlockEntityType.Builder.of(ExtractingFluidPipeBE::new, collectBlocks(ExtractingFluidPipeBlock.class)).build(null));

    public static final Supplier<BlockEntityType<VoidFluidPipeBE>> VOID_FLUID_PIPE = BLOCK_ENTITIES.register("void_fluid_pipe",
            () -> BlockEntityType.Builder.of(VoidFluidPipeBE::new, collectBlocks(VoidFluidPipeBlock.class)).build(null));

    private static Block[] collectBlocks(Class<? extends Block> clazz) {
        return BuiltInRegistries.BLOCK.stream().filter(clazz::isInstance).toList().toArray(Block[]::new);
    }

    private static Block[] collectDiamondPipeBlocks() {
        return BuiltInRegistries.BLOCK.stream().filter(b -> b instanceof DiamondItemPipeBlock).toList().toArray(Block[]::new);
    }
}
