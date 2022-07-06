package net.mehvahdjukaar.supplementaries.mixins.forge;

import net.mehvahdjukaar.supplementaries.client.renderers.items.CageItemRenderer;
import net.mehvahdjukaar.supplementaries.client.renderers.items.JarItemRenderer;
import net.mehvahdjukaar.supplementaries.common.items.CageItem;
import net.mehvahdjukaar.supplementaries.forge.SupplementariesForgeClient;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.IItemRenderProperties;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Consumer;

@Mixin(CageItem.class)
public  abstract class SelfCageItemMixin extends Item {

    public SelfCageItemMixin(Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IItemRenderProperties> consumer) {
        SupplementariesForgeClient.registerISTER(consumer, CageItemRenderer::new);
    }
}