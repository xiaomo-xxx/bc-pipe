package com.thepigcat.buildcraft;

import com.thepigcat.buildcraft.client.blockentities.CrateBERenderer;
import com.thepigcat.buildcraft.client.blockentities.DiamondPipeBERenderer;
import com.thepigcat.buildcraft.client.blockentities.PipeBERenderer;
import com.thepigcat.buildcraft.client.blockentities.TankBERenderer;
import com.thepigcat.buildcraft.client.items.CrateItemRenderer;
import com.thepigcat.buildcraft.client.items.TankItemRenderer;
import com.thepigcat.buildcraft.client.screens.DiamondPipeScreen;
import com.thepigcat.buildcraft.registries.BCBlockEntities;
import com.thepigcat.buildcraft.registries.BCBlocks;
import com.thepigcat.buildcraft.registries.BCMenuTypes;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import org.jetbrains.annotations.NotNull;

@Mod(value = BuildcraftLegacyClient.MODID, dist = Dist.CLIENT)
public final class BuildcraftLegacyClient {
    public static final String MODID = "buildcraft";

    public BuildcraftLegacyClient(IEventBus eventBus, ModContainer modContainer) {
        eventBus.addListener(this::registerRenderers);
        eventBus.addListener(this::registerClientExtensions);
        eventBus.addListener(this::registerMenuScreens);

        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    private void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            @Override
            public @NotNull BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return TANK_ITEM_RENDERER;
            }
        }, BCBlocks.TANK.asItem());

        event.registerItem(new IClientItemExtensions() {
            @Override
            public @NotNull BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return CRATE_ITEM_RENDERER;
            }
        }, BCBlocks.CRATE.asItem());
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BCBlockEntities.ITEM_PIPE.get(), PipeBERenderer::new);
        event.registerBlockEntityRenderer(BCBlockEntities.EXTRACTING_ITEM_PIPE.get(), PipeBERenderer::new);
        // 钻石管道用子类渲染器：先画管内物品，再画六个方向色（绕开 block model）
        event.registerBlockEntityRenderer(BCBlockEntities.DIAMOND_ITEM_PIPE.get(), DiamondPipeBERenderer::new);
        event.registerBlockEntityRenderer(BCBlockEntities.CRATE.get(), CrateBERenderer::new);
        event.registerBlockEntityRenderer(BCBlockEntities.TANK.get(), TankBERenderer::new);
    }

    private static final TankItemRenderer TANK_ITEM_RENDERER = new TankItemRenderer();
    private static final CrateItemRenderer CRATE_ITEM_RENDERER = new CrateItemRenderer();

    private void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(BCMenuTypes.DIAMOND_PIPE.get(), DiamondPipeScreen::new);
    }
}
