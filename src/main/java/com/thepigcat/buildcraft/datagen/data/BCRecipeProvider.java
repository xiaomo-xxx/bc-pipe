package com.thepigcat.buildcraft.datagen.data;

import com.thepigcat.buildcraft.BuildcraftLegacy;
import com.thepigcat.buildcraft.registries.BCBlocks;
import com.thepigcat.buildcraft.registries.BCItems;
import com.thepigcat.buildcraft.tags.BCTags;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class BCRecipeProvider extends net.minecraft.data.recipes.RecipeProvider {
    public BCRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, BCItems.WRENCH)
                .pattern("I I")
                .pattern(" G ")
                .pattern(" I ")
                .define('I', Tags.Items.INGOTS_IRON)
                .define('G', BCTags.Items.STONE_GEAR)
                .unlockedBy("has_stone_gear", has(BCTags.Items.STONE_GEAR))
                .save(recipeOutput);

        gearRecipe(recipeOutput, ItemTags.PLANKS, null, BCItems.WOODEN_GEAR);
        gearRecipe(recipeOutput, Tags.Items.COBBLESTONES, BCTags.Items.WOODEN_GEAR, BCItems.STONE_GEAR);
        gearRecipe(recipeOutput, Tags.Items.INGOTS_IRON, BCTags.Items.STONE_GEAR, BCItems.IRON_GEAR);
        gearRecipe(recipeOutput, Tags.Items.INGOTS_GOLD, BCTags.Items.IRON_GEAR, BCItems.GOLD_GEAR);

        // === Pipe Recipes ===
        // Wooden pipe (extracting) - planks + glass
        dynamicPipeRecipe(recipeOutput, "wooden_pipe", Ingredient.of(ItemTags.PLANKS), 8);
        // Cobblestone pipe - cobblestone + glass
        dynamicPipeRecipe(recipeOutput, "cobblestone_pipe", Ingredient.of(Tags.Items.COBBLESTONES), 8);
        // Stone pipe - stone + glass
        dynamicPipeRecipe(recipeOutput, "stone_pipe", Ingredient.of(Items.STONE), 8);
        // Quartz pipe - quartz + glass
        dynamicPipeRecipe(recipeOutput, "quartz_pipe", Ingredient.of(Items.QUARTZ), 8);
        // Sandstone pipe - sandstone + glass
        dynamicPipeRecipe(recipeOutput, "sandstone_pipe", Ingredient.of(Items.SANDSTONE), 8);
        // Gold pipe - gold ingot + glass
        dynamicPipeRecipe(recipeOutput, "gold_pipe", Ingredient.of(Tags.Items.INGOTS_GOLD), 8);
        // Iron pipe - iron ingot + glass
        dynamicPipeRecipe(recipeOutput, "iron_pipe", Ingredient.of(Tags.Items.INGOTS_IRON), 8);
        // Clay pipe - clay ball + glass
        dynamicPipeRecipe(recipeOutput, "clay_pipe", Ingredient.of(Items.CLAY_BALL), 8);
        // Diamond pipe - diamond + glass
        dynamicPipeRecipe(recipeOutput, "diamond_pipe", Ingredient.of(Tags.Items.GEMS_DIAMOND), 8);
        // Void pipe - obsidian + glass
        dynamicPipeRecipe(recipeOutput, "void_pipe", Ingredient.of(Items.OBSIDIAN), 8);
        // Emerald pipe - emerald + glass (fast extracting)
        dynamicPipeRecipe(recipeOutput, "emerald_pipe", Ingredient.of(Tags.Items.GEMS_EMERALD), 8);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, BCBlocks.CRATE)
                .pattern("LSL")
                .pattern("L L")
                .pattern("LSL")
                .define('L', ItemTags.LOGS)
                .define('S', ItemTags.WOODEN_SLABS)
                .unlockedBy("has_log", has(ItemTags.LOGS))
                .save(recipeOutput);

        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, BCBlocks.TANK)
                .pattern("GGG")
                .pattern("G G")
                .pattern("GGG")
                .define('G', Tags.Items.GLASS_BLOCKS)
                .unlockedBy("has_glass", has(Tags.Items.GLASS_BLOCKS))
                .save(recipeOutput);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, BCBlocks.TANK)
                .requires(BCBlocks.TANK)
                .unlockedBy("has_tank", has(BCBlocks.TANK))
                .save(recipeOutput, BuildcraftLegacy.rl("tank_reset"));
    }

    /**
     * Creates a pipe recipe using the dynamically registered pipe item.
     * Pattern: M M = material, glass is implicit
     * Output: 8 pipes
     */
    private void dynamicPipeRecipe(RecipeOutput recipeOutput, String pipeId, Ingredient material, int count) {
        ResourceLocation pipeRL = BuildcraftLegacy.rl(pipeId);
        Item pipeItem = BuiltInRegistries.ITEM.get(pipeRL);
        if (pipeItem == Items.AIR) return; // Pipe not registered

        // Get first item from ingredient for unlock criterion
        ItemStack[] items = material.getItems();
        ItemLike unlockItem = items.length > 0 ? items[0].getItem() : Items.STONE;

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, pipeItem, count)
                .pattern("MGM")
                .define('M', material)
                .define('G', Tags.Items.GLASS_BLOCKS)
                .unlockedBy("has_" + pipeId, has(unlockItem))
                .save(recipeOutput, BuildcraftLegacy.rl(pipeId));
    }

    public static Criterion<InventoryChangeTrigger.TriggerInstance> has(TagKey<Item> tag) {
        return RecipeProvider.has(tag);
    }

    private void pipeRecipe(RecipeOutput recipeOutput, TagKey<Item> material, ItemLike result) {
        String path = material.location().getPath();
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result, 8)
                .pattern("MGM")
                .define('M', material)
                .define('G', Tags.Items.GLASS_BLOCKS)
                .unlockedBy("has_"+path, has(material))
                .save(recipeOutput);
    }

    private void gearRecipe(RecipeOutput recipeOutput, TagKey<Item> material, @Nullable TagKey<Item> previous, ItemLike result) {
        String path = material.location().getPath();
        if (previous != null) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result)
                    .pattern(" M ")
                    .pattern("MPM")
                    .pattern(" M ")
                    .define('M', material)
                    .define('P', previous)
                    .unlockedBy("has_" + path, has(material))
                    .save(recipeOutput);
        } else {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result)
                    .pattern(" M ")
                    .pattern("M M")
                    .pattern(" M ")
                    .define('M', material)
                    .unlockedBy("has_" + path, has(material))
                    .save(recipeOutput);
        }
    }
}
