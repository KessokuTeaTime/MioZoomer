package org.thinkingstudio.mio_zoomer.commands;

import org.thinkingstudio.mio_zoomer.config.screen.MioZoomerConfigScreen;
import org.thinkingstudio.mio_zoomer.config.screen.widgets.SpruceLabelOption;
import org.thinkingstudio.mio_zoomer.packets.ZoomPackets;
import org.thinkingstudio.obsidianui.Position;
import org.thinkingstudio.obsidianui.SpruceTexts;
import org.thinkingstudio.obsidianui.background.SimpleColorBackground;
import org.thinkingstudio.obsidianui.option.SpruceSeparatorOption;
import org.thinkingstudio.obsidianui.option.SpruceSimpleActionOption;
import org.thinkingstudio.obsidianui.screen.SpruceScreen;
import org.thinkingstudio.obsidianui.widget.SpruceButtonWidget;
import org.thinkingstudio.obsidianui.widget.container.SpruceOptionListWidget;
import org.thinkingstudio.mio_zoomer.config.MioZoomerConfigManager;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.text.Text;

public class MioZoomerCommandScreen extends SpruceScreen {
	private SpruceOptionListWidget list;
	private final SimpleColorBackground darkenedBackground = new SimpleColorBackground(0, 0, 0, 128);

	public MioZoomerCommandScreen() {
		super(Text.translatable("command.mio_zoomer.title"));
	}

	@Override
	protected void init() {
		super.init();
		this.list = new SpruceOptionListWidget(Position.of(0, 22), this.width, this.height - 36 - 22);

		var configButton = SpruceSimpleActionOption.of(
			"command.mio_zoomer.config",
			button -> this.client.setScreen(new MioZoomerConfigScreen(this)),
			null);

		var restrictionsSeparator = new SpruceSeparatorOption(
			"command.mio_zoomer.restrictions",
			true,
			Text.translatable("command.mio_zoomer.restrictions.tooltip"));

		this.list.addSingleOptionEntry(configButton);
		this.list.addSingleOptionEntry(restrictionsSeparator);


		if (ZoomPackets.getHasRestrictions()) {
			var textLabel = new SpruceLabelOption("command.mio_zoomer.restrictions.acknowledgement", true);
			this.list.addSingleOptionEntry(textLabel);
		}

		if (ZoomPackets.getDisableZoom()) {
			var textLabel = new SpruceLabelOption("command.mio_zoomer.restrictions.disable_zoom", true);
			this.list.addSingleOptionEntry(textLabel);
		}

		if (ZoomPackets.getDisableZoomScrolling()) {
			var textLabel = new SpruceLabelOption("command.mio_zoomer.restrictions.disable_zoom_scrolling", true);
			this.list.addSingleOptionEntry(textLabel);
		}

		if (ZoomPackets.getForceClassicMode()) {
			var textLabel = new SpruceLabelOption("command.mio_zoomer.restrictions.force_classic_mode", true);
			this.list.addSingleOptionEntry(textLabel);
		}

		if (ZoomPackets.getForceZoomDivisors()) {
			double minimumZoomDivisor = ZoomPackets.getMinimumZoomDivisor();
			double maximumZoomDivisor = ZoomPackets.getMaximumZoomDivisor();
			var textLabel = new SpruceLabelOption(
				"command.mio_zoomer.restrictions.force_zoom_divisors",
				minimumZoomDivisor != maximumZoomDivisor
					? Text.translatable("command.mio_zoomer.restrictions.force_zoom_divisors", minimumZoomDivisor, maximumZoomDivisor)
					: Text.translatable("command.mio_zoomer.restrictions.force_zoom_divisor", minimumZoomDivisor),
				true);
			this.list.addSingleOptionEntry(textLabel);
		}

		if (ZoomPackets.getSpyglassDependency()) {
			String key = switch (MioZoomerConfigManager.SPYGLASS_DEPENDENCY.value()) {
				case REQUIRE_ITEM -> "command.mio_zoomer.restrictions.force_spyglass.require_item";
				case REPLACE_ZOOM -> "command.mio_zoomer.restrictions.force_spyglass.replace_zoom";
				case BOTH -> "command.mio_zoomer.restrictions.force_spyglass.both";
				default -> "";
			};
			var textLabel = new SpruceLabelOption(key, true);
			this.list.addSingleOptionEntry(textLabel);
		}

		if (ZoomPackets.getSpyglassOverlay()) {
			var textLabel = new SpruceLabelOption("command.mio_zoomer.restrictions.force_spyglass_overlay", true);
			this.list.addSingleOptionEntry(textLabel);
		}

		if (!ZoomPackets.getHasRestrictions()) {
			boolean acknowledged = ZoomPackets.getAcknowledgement().equals(ZoomPackets.Acknowledgement.HAS_NO_RESTRICTIONS);
			if (acknowledged) {
				var textLabel = new SpruceLabelOption("command.mio_zoomer.restrictions.no_restrictions.acknowledged", true);
				this.list.addSingleOptionEntry(textLabel);
			} else {
				var textLabel = new SpruceLabelOption("command.mio_zoomer.restrictions.no_restrictions", true);
				this.list.addSingleOptionEntry(textLabel);
			}
		}

		this.list.setBackground(darkenedBackground);

		this.addDrawableChild(this.list);
		this.addDrawableChild(new SpruceButtonWidget(Position.of(this, this.width / 2 - 100, this.height - 28), 200, 20, SpruceTexts.GUI_DONE,
			btn -> this.client.setScreen(null)).asVanilla());
	}

	@Override
	public void renderTitle(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		graphics.drawCenteredShadowedText(this.textRenderer, this.title, this.width / 2, 8, 0xFFFFFF);
	}
}
