package com.ootruffle.bedwarsupgradesdisplay.mixins;

import com.ootruffle.bedwarsupgradesdisplay.ChatListener;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Chat message hook for the Ornithe (1.8.9) target.
 *
 * OneConfig's own cross-platform ChatEvent was tried first, but
 * github.com/TomekoYT/HyBedWars — a real, published Bedwars companion mod on
 * this exact platform — instead hooks the raw packet handler directly via a
 * mixin, which is the proven-working approach for this specific platform.
 * This mirrors HyBedWars's NetHandlerPlayClientMixin, simplified since we
 * only need to read the message (never cancel or rewrite it).
 */
@Mixin(NetHandlerPlayClient.class)
public abstract class NetHandlerPlayClientMixin {

    @Inject(method = "handleChat", at = @At("HEAD"))
    private void bwupgradesdisplay$onHandleChat(S02PacketChat packetIn, CallbackInfo ci) {
        IChatComponent message = packetIn.getChatComponent();
        if (message == null) {
            return;
        }
        ChatListener.onChatReceived(message.getUnformattedText());
    }
}
