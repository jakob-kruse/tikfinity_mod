package de.jakobkruse.tikfinity.mixin;

import de.jakobkruse.tikfinity.TikfinityMod;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class TikfinityModMixin {
    @Inject(at = @At("HEAD"), method = "loadWorld")
    private void init(CallbackInfo info) {
        TikfinityMod.LOGGER.info("loadWorld");
    }
}
