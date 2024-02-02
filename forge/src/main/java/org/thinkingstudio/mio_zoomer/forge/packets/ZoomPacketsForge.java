package org.thinkingstudio.mio_zoomer.forge.packets;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.event.EventNetworkChannel;
import net.minecraftforge.network.simple.SimpleChannel;
import org.thinkingstudio.mio_zoomer.MioZoomerClientMod;
import org.thinkingstudio.mio_zoomer.config.ConfigEnums;
import org.thinkingstudio.mio_zoomer.config.MioZoomerConfigManager;
import org.thinkingstudio.mio_zoomer.packets.ZoomPackets;
import org.thinkingstudio.mio_zoomer.utils.ZoomUtils;

import java.util.function.Function;

public class ZoomPacketsForge {
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

		<T extends ZoomPacket> void registerPacket(Class<T> clazz, Function<PacketByteBuf, T> decode) {
			CHANNEL.messageBuilder(clazz, packetIndex++)
				.encoder(ZoomPacket::encode)
				.decoder(decode)
				.consumerMainThread((packet, supplier) -> packet.handle(supplier.get()))
				.add();
		}
	}

	//Registers all the packets
	public static void registerPackets() {
		ZoomPacketRegister packetRegister = new ZoomPacketRegister();
		packetRegister.registerPacket(DisableZoomPacket.class, DisableZoomPacket::decode);
		packetRegister.registerPacket(DisableZoomScrollingPacket.class, DisableZoomScrollingPacket::decode);
		packetRegister.registerPacket(ForceClassicModePacket.class, ForceClassicModePacket::decode);
		packetRegister.registerPacket(ForceZoomDivisorPacket.class, ForceZoomDivisorPacket::decode);
		packetRegister.registerPacket(AcknowledgeModPacket.class, AcknowledgeModPacket::decode);
		packetRegister.registerPacket(ForceSpyglassPacket.class, ForceSpyglassPacket::decode);
		packetRegister.registerPacket(ForceSpyglassOverlayPacket.class, ForceSpyglassOverlayPacket::decode);
		packetRegister.registerPacket(ResetRestrictionsPacket.class, ResetRestrictionsPacket::decode);
	}

	/**
	 * The "Disable Zoom" packet,
	 * if this packet is received, Mio Zoomer's zoom will be disabled completely while in the server
	 * Supported since Mio Zoomer 4.0.0 (1.16)
	 * <p>
	 * Arguments: None
	 */
	public static record DisableZoomPacket(boolean disableZoom) implements ZoomPacket {
		@Override
		public void encode(PacketByteBuf buf) {
			buf.writeBoolean(disableZoom);
		}

		@Override
		public void handle(NetworkEvent.Context context) {
			ZoomUtils.LOGGER.info("[Mio Zoomer] This server has disabled zooming");
			ZoomPackets.disableZoom = disableZoom;
			ZoomPackets.checkRestrictions();
		}

		public static DisableZoomPacket decode(PacketByteBuf buf) {
			return new DisableZoomPacket(buf.readBoolean());
		}
	}

	/**
	 * The "Disable Zoom Scrolling" packet,
	 * If this packet is received, zoom scrolling will be disabled while in the server
	 * Supported since Mio Zoomer 4.0.0 (1.16)
	 * <p>
	 * Arguments: None
	 */
	public static record DisableZoomScrollingPacket(boolean disableScrolling) implements ZoomPacket {
		@Override
		public void encode(PacketByteBuf buf) {
			buf.writeBoolean(disableScrolling);
		}

		@Override
		public void handle(NetworkEvent.Context context) {
			ZoomUtils.LOGGER.info("[Mio Zoomer] This server has disabled zoom scrolling");
			ZoomPackets.applyDisableZoomScrolling();
			ZoomPackets.disableZoomScrolling = disableScrolling;
			ZoomPackets.checkRestrictions();
		}

		public static DisableZoomScrollingPacket decode(PacketByteBuf buf) {
			return new DisableZoomScrollingPacket(buf.readBoolean());
		}
	}

	/**
	 * The "Force Classic Mode" packet,
	 * If this packet is received, the Classic Mode will be activated while connected to the server,
	 * under the Classic mode, the Classic preset will be forced on all non-cosmetic options
	 * Supported since Mio Zoomer 5.0.0-beta.1 (1.17)
	 * <p>
	 * Arguments: None
	 */
	public static record ForceClassicModePacket(boolean forceClassicMode) implements ZoomPacket {
		@Override
		public void encode(PacketByteBuf buf) {
			buf.writeBoolean(forceClassicMode);
		}

		@Override
		public void handle(NetworkEvent.Context context) {
			ZoomUtils.LOGGER.info("[Mio Zoomer] This server has imposed classic mode");
			ZoomPackets.disableZoomScrolling = forceClassicMode;
			ZoomPackets.forceClassicMode = forceClassicMode;
			ZoomPackets.applyDisableZoomScrolling();
			ZoomPackets.applyClassicMode();
			MioZoomerConfigManager.configureZoomInstance();
			ZoomPackets.checkRestrictions();
		}

		public static ForceClassicModePacket decode(PacketByteBuf buf) {
			return new ForceClassicModePacket(buf.readBoolean());
		}
	}

	/**
	 * The "Force Zoom Divisor" packet,
	 * If this packet is received, the minimum and maximum zoom divisor values will be overriden
	 * with the provided arguments
	 * Supported since Mio Zoomer 5.0.0-beta.2 (1.17)
	 * <p>
	 * Arguments: One double (max & min) or two doubles (first is max, second is min)
	 */
	public static record ForceZoomDivisorPacket(double minDouble, double maxDouble) implements ZoomPacket {
		@Override
		public void encode(PacketByteBuf buf) {
			buf.writeDouble(minDouble());
			buf.writeDouble(maxDouble());
		}

		@Override
		public void handle(NetworkEvent.Context context) {
			if ((minDouble <= 0.0 || maxDouble <= 0.0) || minDouble > maxDouble) {
				ZoomUtils.LOGGER.info(String.format("[Mio Zoomer] This server has attempted to set invalid divisor values! (min %s, max %s)", minDouble, maxDouble));
			} else {
				ZoomUtils.LOGGER.info(String.format("[Mio Zoomer] This server has set the zoom divisors to minimum %s and maximum %s", minDouble, maxDouble));
				ZoomPackets.maximumZoomDivisor = maxDouble;
				ZoomPackets.minimumZoomDivisor = minDouble;
				ZoomPackets.forceZoomDivisors = true;
				MioZoomerConfigManager.configureZoomInstance();
				ZoomPackets.checkRestrictions();
			}
		}

		public static ForceZoomDivisorPacket decode(PacketByteBuf buf) {
			return new ForceZoomDivisorPacket(buf.readDouble(), buf.readDouble());
		}
	}

	/**
	 * The "Acknowledge Mod" packet,
	 * If received, a toast will appear, the toast will either state that
	 * wthe server won't restrict the mod or say that the server controls will be activated
	 * Supported since Mio Zoomer 5.0.0-beta.2 (1.17)
	 * <p>
	 * Arguments: one boolean, false for restricting, true for restrictionless
	 */
	public static record AcknowledgeModPacket(ZoomPackets.Acknowledgement restrictions) implements ZoomPacket {
		@Override
		public void encode(PacketByteBuf buf) {
			buf.writeEnumConstant(restrictions);
		}

		@Override
		public void handle(NetworkEvent.Context context) {
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
				if (restrictions == ZoomPackets.Acknowledgement.HAS_RESTRICTIONS) {
					if (ZoomPackets.getAcknowledgement().equals(ZoomPackets.Acknowledgement.HAS_RESTRICTIONS)) {
						ZoomUtils.LOGGER.info("[Mio Zoomer] This server acknowledges the mod and has established some restrictions");
						this.doSendToast(Text.translatable("toast.mio_zoomer.acknowledge_mod_restrictions"));
					}
				} else {
					if (ZoomPackets.getAcknowledgement().equals(ZoomPackets.Acknowledgement.HAS_NO_RESTRICTIONS)) {
						ZoomUtils.LOGGER.info("[Mio Zoomer] This server acknowledges the mod and establishes no restrictions");
						this.doSendToast(Text.translatable("toast.mio_zoomer.acknowledge_mod"));
					}
				}
			});
		}

		public static AcknowledgeModPacket decode(PacketByteBuf buf) {
			return new AcknowledgeModPacket(buf.readEnumConstant(ZoomPackets.Acknowledgement.class));
		}

		private void doSendToast(Text text) {
			ZoomPackets.sendToast(MinecraftClient.getInstance(), text);
		}
	}

	/**
	 * The "Force Spyglass" packet,
	 * This packet lets the server to impose a spyglass restriction
	 * wthe server won't restrict the mod or say that the server controls will be activated
	 * Supported since Mio Zoomer 5.0.0-beta.4 (1.18.2)
	 */
	public static record ForceSpyglassPacket(ConfigEnums.SpyglassDependency dependency) implements ZoomPacket {
		@Override
		public void encode(PacketByteBuf buf) {
			buf.writeEnumConstant(dependency);
		}

		@Override
		public void handle(NetworkEvent.Context context) {
			ZoomUtils.LOGGER.info("[Mio Zoomer] This server has imposed classic mode");
			ZoomPackets.disableZoomScrolling = true;
			ZoomPackets.forceClassicMode = true;
			ZoomPackets.applyDisableZoomScrolling();
			ZoomPackets.applyClassicMode();
			MioZoomerConfigManager.configureZoomInstance();
			ZoomPackets.checkRestrictions();
		}

		public static ForceSpyglassPacket decode(PacketByteBuf buf) {
			return new ForceSpyglassPacket(buf.readEnumConstant(ConfigEnums.SpyglassDependency.class));
		}
	}

	/**
	 * The "Force Spyglass Overlay" packet,
	 * This packet will let the server restrict the mod to spyglass-only usage
	 * Not supported yet!
	 * <p>
	 * Arguments: probably some, we'll see
	 */
	public static record ForceSpyglassOverlayPacket(ConfigEnums.ZoomOverlays overlay) implements ZoomPacket {
		@Override
		public void encode(PacketByteBuf buf) {
			buf.writeEnumConstant(overlay());
		}

		@Override
		public void handle(NetworkEvent.Context context) {
			ZoomUtils.LOGGER.info(String.format("[Mio Zoomer] This server has imposed a spyglass overlay on the zoom"));
			MioZoomerConfigManager.ZOOM_OVERLAY.setOverride(ConfigEnums.ZoomOverlays.SPYGLASS);
			ZoomPackets.spyglassOverlay = true;
			ZoomPackets.checkRestrictions();
		}

		public static ForceSpyglassOverlayPacket decode(PacketByteBuf buf) {
			return new ForceSpyglassOverlayPacket(buf.readEnumConstant(ConfigEnums.ZoomOverlays.class));
		}
	}

	public static record ResetRestrictionsPacket() implements ZoomPacket {
		@Override
		public void encode(PacketByteBuf buf) {

		}

		@Override
		public void handle(NetworkEvent.Context context) {
			if (ZoomPackets.hasRestrictions) {
				ZoomPackets.resetPacketSignals();
			}
		}

		public static ResetRestrictionsPacket decode(PacketByteBuf buf) {
			return new ResetRestrictionsPacket();
		}
	}

	public static interface ZoomPacket {
		void encode(PacketByteBuf buf);

		void handle(NetworkEvent.Context context);
	}
}
