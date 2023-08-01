package de.jakobkruse.tikfinity.mixin;

import de.jakobkruse.tikfinity.CommandExecutor;
import de.jakobkruse.tikfinity.CommandServer;
import de.jakobkruse.tikfinity.TikfinityMod;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class TikfinityModMixin {

    @Unique
    private CommandServer commandServer;

    @Inject(at = @At("TAIL"), method = "loadWorld")
    private void init(CallbackInfo info) {
        TikfinityMod.LOGGER.info("Starting Web Server on Client");
        commandServer = new CommandServer(new CommandExecutor());

        commandServer.startServer();
    }

    @Inject(at = @At("TAIL"), method = "shutdown")
    private void shutdown(CallbackInfo info) {
        TikfinityMod.LOGGER.info("Stopping Web Server");
        if(commandServer != null) {
            commandServer.stopServer();
        }

    }

}
