package dev.clientbot.mixin;

import dev.clientbot.core.ClientBotMod;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "renderLevel", at = @At("HEAD"), cancellable = true)
    private void clientbot$skipWorld(DeltaTracker deltaTracker, CallbackInfo ci) {
        if (ClientBotMod.instance() != null && ClientBotMod.instance().rendering().isSuppressed()) ci.cancel();
    }

    @Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
    private void clientbot$skipHand(CameraRenderState cameraRenderState, float partialTick, Matrix4fc matrix, CallbackInfo ci) {
        if (ClientBotMod.instance() != null && ClientBotMod.instance().rendering().isHandSuppressed()) ci.cancel();
    }
}
