package com.thepigcat.buildcraft.events;

import com.thepigcat.buildcraft.BuildcraftLegacy;
import com.thepigcat.buildcraft.api.capabilties.JumboItemHandler;
import com.thepigcat.buildcraft.content.blockentities.CrateBE;
import com.thepigcat.buildcraft.content.blocks.CrateBlock;
import com.thepigcat.buildcraft.networking.PipeSyncManager;
import com.thepigcat.buildcraft.util.BlockUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.items.ItemHandlerHelper;

@EventBusSubscriber(modid = BuildcraftLegacy.MODID)
public final class CommonEvents {
    /**
     * Flushes the batched pipe movement updates once per server tick, after every block entity has
     * ticked, so a tick's worth of movement for a whole chunk travels in a single packet.
     */
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            PipeSyncManager.flush(level);
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getLevel().isClientSide()) return;
        // 只在按下的一瞬间处理，否则按住不放会每个 tick 都抽一次
        if (event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START) return;

        Player player = event.getEntity();
        // 关键：只有潜行才是「快速取物」。不潜行时保持原版挖掘行为，
        // 否则玩家永远没法从正面把箱子挖掉（旧逻辑会无条件取消破坏事件）。
        if (!player.isShiftKeyDown()) return;

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState blockState = level.getBlockState(pos);
        if (!(blockState.getBlock() instanceof CrateBlock)) return;
        if (!event.getFace().equals(blockState.getValue(BlockStateProperties.HORIZONTAL_FACING))) return;

        CrateBE be = BlockUtils.getBE(CrateBE.class, level, pos);
        if (be == null) return;

        event.setCanceled(true);

        JumboItemHandler itemHandler = be.getItemHandler();
        ItemStack stack = itemHandler.getStackInSlot(0);
        if (!stack.isEmpty()) {
            int count = Math.min(stack.getMaxStackSize(), stack.getCount());
            ItemStack extracted = itemHandler.extractItem(0, count, false);
            ItemHandlerHelper.giveItemToPlayer(player, extracted);
        }
    }
}
