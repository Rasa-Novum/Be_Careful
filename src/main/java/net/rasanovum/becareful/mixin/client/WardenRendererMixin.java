package net.rasanovum.becareful.mixin.client;

import net.minecraft.client.model.WardenModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.WardenRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.monster.warden.Warden;
import net.rasanovum.becareful.client.WardenKeyLayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class WardenRendererMixin implements RenderLayerParent<Warden, WardenModel<Warden>> {
    @Shadow
    protected abstract boolean addLayer(RenderLayer<Warden, WardenModel<Warden>> layer);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void beCareful$addLostKeyLayer(EntityRendererProvider.Context context, EntityModel<?> model,
                                           float shadowRadius, CallbackInfo ci) {
        if ((Object) this instanceof WardenRenderer) {
            @SuppressWarnings("unchecked")
            RenderLayerParent<Warden, WardenModel<Warden>> parent =
                    (RenderLayerParent<Warden, WardenModel<Warden>>) (Object) this;
            addLayer(new WardenKeyLayer(parent));
        }
    }
}
