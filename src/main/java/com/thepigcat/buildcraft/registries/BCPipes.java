package com.thepigcat.buildcraft.registries;

import com.mojang.datafixers.util.Either;
import com.thepigcat.buildcraft.BuildcraftLegacy;
import com.thepigcat.buildcraft.api.pipes.PipeHolder;
import com.thepigcat.buildcraft.util.PipeRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.Tags;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public final class BCPipes {
    public static final PipeRegistrationHelper HELPER = new PipeRegistrationHelper(BuildcraftLegacy.MODID);

    // Wooden pipes - extracting (pull items from containers)
    public static final PipeHolder WOODEN = HELPER.registerPipe("wooden", BCPipeTypes.EXTRACTING, "Wooden Pipe", 0.25f, List.of(
            BuildcraftLegacy.rl("block/wooden_pipe"),
            BuildcraftLegacy.rl("block/wooden_pipe_extracting")
    ), Either.right(ResourceLocation.parse("oak_planks")), Ingredient.of(ItemTags.PLANKS), List.of(BlockTags.MINEABLE_WITH_AXE), 0);

    // Cobblestone pipes - basic item transport
    public static final PipeHolder COBBLESTONE = HELPER.registerPipe("cobblestone", BCPipeTypes.DEFAULT, "Cobblestone Pipe", 0.25f, List.of(
            BuildcraftLegacy.rl("block/cobblestone_pipe")
    ), Either.right(ResourceLocation.parse("cobblestone")), Ingredient.of(Tags.Items.COBBLESTONES), List.of(BlockTags.MINEABLE_WITH_PICKAXE), 1);

    // Gold pipes - fast item transport
    public static final PipeHolder GOLD = HELPER.registerPipe("gold", BCPipeTypes.DEFAULT, "Gold Pipe", 0.5f, List.of(
            BuildcraftLegacy.rl("block/gold_pipe")
    ), Either.right(ResourceLocation.parse("gold_block")), Ingredient.of(Tags.Items.INGOTS_GOLD), List.of(BlockTags.MINEABLE_WITH_PICKAXE), 2);

    // Diamond pipes - extracting + filtering
    public static final PipeHolder DIAMOND = HELPER.registerPipe("diamond", BCPipeTypes.EXTRACTING, "Diamond Pipe", 0.5f, List.of(
            BuildcraftLegacy.rl("block/diamond_pipe"),
            BuildcraftLegacy.rl("block/diamond_pipe_extracting")
    ), Either.right(ResourceLocation.parse("diamond_block")), Ingredient.of(Tags.Items.GEMS_DIAMOND), List.of(BlockTags.MINEABLE_WITH_PICKAXE), 3);

    // Stone pipes - basic item transport
    public static final PipeHolder STONE = HELPER.registerPipe("stone", BCPipeTypes.DEFAULT, "Stone Pipe", 0.25f, List.of(
            BuildcraftLegacy.rl("block/stone_pipe")
    ), Either.right(ResourceLocation.parse("stone")), Ingredient.of(Tags.Items.STONES), List.of(BlockTags.MINEABLE_WITH_PICKAXE), 4);

    // Quartz pipes - basic item transport
    public static final PipeHolder QUARTZ = HELPER.registerPipe("quartz", BCPipeTypes.DEFAULT, "Quartz Pipe", 0.25f, List.of(
            BuildcraftLegacy.rl("block/quartz_pipe")
    ), Either.right(ResourceLocation.parse("quartz_block")), Ingredient.of(Tags.Items.GEMS_QUARTZ), List.of(BlockTags.MINEABLE_WITH_PICKAXE), 5);

    // Sandstone pipes - basic item transport
    public static final PipeHolder SANDSTONE = HELPER.registerPipe("sandstone", BCPipeTypes.DEFAULT, "Sandstone Pipe", 0.25f, List.of(
            BuildcraftLegacy.rl("block/sandstone_pipe")
    ), Either.right(ResourceLocation.parse("sandstone")), Ingredient.of(Items.SANDSTONE), List.of(BlockTags.MINEABLE_WITH_PICKAXE), 6);

    // Iron pipes - directional item transport
    public static final PipeHolder IRON = HELPER.registerPipe("iron", BCPipeTypes.DEFAULT, "Iron Pipe", 0.25f, List.of(
            BuildcraftLegacy.rl("block/iron_pipe")
    ), Either.right(ResourceLocation.parse("iron_block")), Ingredient.of(Tags.Items.INGOTS_IRON), List.of(BlockTags.MINEABLE_WITH_PICKAXE), 7);

    // Clay pipes - prioritize same-color containers
    public static final PipeHolder CLAY = HELPER.registerPipe("clay", BCPipeTypes.DEFAULT, "Clay Pipe", 0.25f, List.of(
            BuildcraftLegacy.rl("block/clay_pipe")
    ), Either.right(ResourceLocation.parse("clay")), Ingredient.of(Items.CLAY_BALL), List.of(BlockTags.MINEABLE_WITH_SHOVEL), 8);

    // Void pipes - destroy items
    public static final PipeHolder VOID = HELPER.registerPipe("void", BCPipeTypes.VOID, "Void Pipe", 0.25f, List.of(
            BuildcraftLegacy.rl("block/void_pipe")
    ), Either.right(ResourceLocation.parse("obsidian")), Ingredient.of(Items.OBSIDIAN), List.of(BlockTags.MINEABLE_WITH_PICKAXE), 9);

    public static void init() {
    }
}
