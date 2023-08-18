package net.mehvahdjukaar.supplementaries.integration.forge;

import com.mojang.datafixers.util.Either;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.supplementaries.api.IQuiverEntity;
import net.mehvahdjukaar.supplementaries.common.block.tiles.SafeBlockTile;
import net.mehvahdjukaar.supplementaries.common.block.tiles.TrappedPresentBlockTile;
import net.mehvahdjukaar.supplementaries.common.items.QuiverItem;
import net.mehvahdjukaar.supplementaries.common.items.SackItem;
import net.mehvahdjukaar.supplementaries.common.items.SafeItem;
import net.mehvahdjukaar.supplementaries.common.items.tooltip_components.InventoryTooltip;
import net.mehvahdjukaar.supplementaries.integration.forge.quark.TaterInAJarTileRenderer;
import net.mehvahdjukaar.supplementaries.reg.ModRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Lazy;
import vazkii.arl.util.ItemNBTHelper;
import vazkii.quark.api.event.UsageTickerEvent;
import vazkii.quark.base.handler.GeneralConfig;
import vazkii.quark.base.module.ModuleLoader;
import vazkii.quark.content.client.module.ImprovedTooltipsModule;
import vazkii.quark.content.management.module.ExpandedItemInteractionsModule;

import java.util.ArrayList;
import java.util.List;

public class QuarkClientCompatImpl {

    public static void initClient() {
        ClientHelper.addBlockEntityRenderersRegistration(QuarkClientCompatImpl::registerEntityRenderers);
        MinecraftForge.EVENT_BUS.addListener(QuarkClientCompatImpl::onItemTooltipEvent);
        MinecraftForge.EVENT_BUS.addListener(QuarkClientCompatImpl::quiverUsageTicker);
    }

    private static void registerEntityRenderers(ClientHelper.BlockEntityRendererEvent event) {
        event.register(QuarkCompatImpl.TATER_IN_A_JAR_TILE.get(), TaterInAJarTileRenderer::new);
    }

    public static void setupClient() {
        ClientHelper.registerRenderType(QuarkCompatImpl.TATER_IN_A_JAR.get(), RenderType.cutout());
    }

    public static boolean shouldHaveButtonOnRight() {
        return !(GeneralConfig.qButtonOnRight && GeneralConfig.enableQButton);
    }

    public static boolean canRenderBlackboardTooltip() {
        return canRenderQuarkTooltip();
    }

    public static boolean canRenderQuarkTooltip() {
        return ModuleLoader.INSTANCE.isModuleEnabled(ImprovedTooltipsModule.class)
                && ImprovedTooltipsModule.shulkerTooltips &&
                (!ImprovedTooltipsModule.shulkerBoxRequireShift || Screen.hasShiftDown());
    }

    public static void registerTooltipComponent(ClientHelper.TooltipComponentEvent event) {
        event.register(InventoryTooltip.class, QuarkInventoryTooltipComponent::new);
    }


    private static final Lazy<SafeBlockTile> DUMMY_SAFE_TILE = Lazy.of(() -> new SafeBlockTile(BlockPos.ZERO, ModRegistry.SAFE.get().defaultBlockState()));

    public static void onItemTooltipEvent(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        if (canRenderQuarkTooltip()) {
            Item item = stack.getItem();
            if (item instanceof SafeItem || item instanceof SackItem) {
                CompoundTag cmp = ItemNBTHelper.getCompound(stack, "BlockEntityTag", false);
                if (cmp.contains("LootTable")) return;

                if (item instanceof SafeItem) {
                    DUMMY_SAFE_TILE.get().load(cmp);
                    Player player = Minecraft.getInstance().player;
                    if (!(player == null || DUMMY_SAFE_TILE.get().canPlayerOpen(Minecraft.getInstance().player, false))) {
                        return;
                    }
                }
                List<Either<FormattedText, TooltipComponent>> tooltip = event.getTooltipElements();
                List<Either<FormattedText, TooltipComponent>> tooltipCopy = new ArrayList<>(tooltip);

                for (int i = 1; i < tooltipCopy.size(); i++) {
                    Either<FormattedText, TooltipComponent> either = tooltipCopy.get(i);
                    if (either.left().isPresent()) {
                        String s = either.left().get().getString();
                        if (!s.startsWith("§") || s.startsWith("§o"))
                            tooltip.remove(either);
                    }
                }
                if (ImprovedTooltipsModule.shulkerBoxRequireShift && !Screen.hasShiftDown())
                    tooltip.add(1, Either.left(Component.translatable("quark.misc.shulker_box_shift")));
            }
        }
    }



    public static void quiverUsageTicker(UsageTickerEvent.GetCount event) {
        if (event.currentRealStack.getItem() instanceof ProjectileWeaponItem && event.currentStack != event.currentRealStack) {
            //adds missing ones from quiver

            if (event.player instanceof IQuiverEntity qe) {
                var q = qe.getQuiver();
                if (!q.isEmpty()) {
                    QuiverItem.Data data = QuiverItem.getQuiverData(q);
                    if (data != null) {
                        //sanity check
                        ItemStack selected = data.getSelected();

                        if (event.currentStack.is(selected.getItem())) {
                            //just recomputes everything
                            int count = data.getSelectedArrowCount();
                            Inventory inventory = event.player.getInventory();

                            for (int i = 0; i < inventory.getContainerSize(); ++i) {
                                ItemStack stackAt = inventory.getItem(i);
                                if (selected.is(stackAt.getItem())) {
                                    count += stackAt.getCount();
                                }
                            }
                            event.setResultCount(count);
                        }
                    }
                }
            }
        }
    }




}
