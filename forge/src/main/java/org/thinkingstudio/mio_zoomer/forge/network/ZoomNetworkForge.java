package org.thinkingstudio.mio_zoomer.forge.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.event.EventNetworkChannel;
import net.minecraftforge.network.simple.SimpleChannel;
import org.thinkingstudio.mio_zoomer.MioZoomerClientMod;
import org.thinkingstudio.mio_zoomer.forge.network.packets.ZoomPackets;

import java.util.function.Function;

public class ZoomNetworkForge {
	public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder.named(new Identifier(MioZoomerClientMod.MODID, "network"))
		.clientAcceptedVersions(e -> true)
		.serverAcceptedVersions(e -> true)
		.networkProtocolVersion(() -> "hmm :)")
		.simpleChannel();
	public static final EventNetworkChannel EXISTENCE_CHANNEL = NetworkRegistry.ChannelBuilder.named(new Identifier(MioZoomerClientMod.MODID, "exists"))
		.clientAcceptedVersions(e -> true)
		.serverAcceptedVersions(e -> true)
		.networkProtocolVersion(() -> "Why'd I exist?!")
		.eventNetworkChannel();


	public static class ZoomPacketRegister {
		int packetIndex = 0;

		<T extends ZoomPackets.ZoomPacket> void registerPacket(Class<T> clazz, Function<PacketByteBuf, T> decode) {
			CHANNEL.messageBuilder(clazz, packetIndex++)
				.encoder(ZoomPackets.ZoomPacket::encode)
				.decoder(decode)
				.consumerMainThread((packet, supplier) -> packet.handle(supplier.get()))
				.add();
		}
	}

	//Registers all the network
	public static void registerPackets() {
		ZoomPacketRegister packetRegister = new ZoomPacketRegister();
		packetRegister.registerPacket(ZoomPackets.DisableZoomPacket.class, ZoomPackets.DisableZoomPacket::decode);
		packetRegister.registerPacket(ZoomPackets.DisableZoomScrollingPacket.class, ZoomPackets.DisableZoomScrollingPacket::decode);
		packetRegister.registerPacket(ZoomPackets.ForceClassicModePacket.class, ZoomPackets.ForceClassicModePacket::decode);
		packetRegister.registerPacket(ZoomPackets.ForceZoomDivisorPacket.class, ZoomPackets.ForceZoomDivisorPacket::decode);
		packetRegister.registerPacket(ZoomPackets.AcknowledgeModPacket.class, ZoomPackets.AcknowledgeModPacket::decode);
		packetRegister.registerPacket(ZoomPackets.ForceSpyglassPacket.class, ZoomPackets.ForceSpyglassPacket::decode);
		packetRegister.registerPacket(ZoomPackets.ForceSpyglassOverlayPacket.class, ZoomPackets.ForceSpyglassOverlayPacket::decode);
		packetRegister.registerPacket(ZoomPackets.ResetRestrictionsPacket.class, ZoomPackets.ResetRestrictionsPacket::decode);
	}
}
