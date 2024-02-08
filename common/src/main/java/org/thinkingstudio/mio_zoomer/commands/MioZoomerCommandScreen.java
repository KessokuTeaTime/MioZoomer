package org.thinkingstudio.mio_zoomer.commands;

import org.thinkingstudio.mio_zoomer.config.MioZoomerConfigManager;
import org.thinkingstudio.mio_zoomer.config.screen.MioZoomerConfigScreen;
import org.thinkingstudio.mio_zoomer.config.screen.widgets.SpruceLabelOption;
import org.thinkingstudio.mio_zoomer.network.ZoomNetwork;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.text.Text;
import org.thinkingstudio.obsidianui.Position;
import org.thinkingstudio.obsidianui.SpruceTexts;
import org.thinkingstudio.obsidianui.background.SimpleColorBackground;
import org.thinkingstudio.obsidianui.option.SpruceSeparatorOption;
import org.thinkingstudio.obsidianui.option.SpruceSimpleActionOption;
import org.thinkingstudio.obsidianui.screen.SpruceScreen;
import org.thinkingstudio.obsidianui.widget.SpruceButtonWidget;
import org.thinkingstudio.obsidianui.widget.container.SpruceOptionListWidget;

public class MioZoomerCommandScreen extends SpruceScreen {
	private static final SimpleColorBackground DARKENED_BACKGROUND = new SimpleColorBackground(0, 0, 0, 128);

	public MioZoomerCommandScreen() {
		super(Text.translatable("command.mio_zoomer.title"));
	}

	@Override
	protected void init() {
		super.init();
		var list = new SpruceOptionListWidget(Position.of(0, 22), this.width, this.height - 36 - 22);
		list.setBackground(DARKENED_BACKGROUND);

		var configButton = SpruceSimpleActionOption.of(
			"command.mio_zoomer.config",
			button -> this.client.setScreen(new MioZoomerConfigScreen(this)),
			null);

		var restrictionsSeparator = new SpruceSeparatorOption(
			"command.mio_zoomer.restrictions",
			true,
			Text.translatable("command.mio_zoomer.restrictions.tooltip"));

		list.addSingleOptionEntry(configButton);
		list.addSingleOptionEntry(restrictionsSeparator);


		if (ZoomNetwork.getHasRestrictions()) {
			list.addSingleOptionEntry(new SpruceLabelOption("command.mio_zoomer.restrictions.acknowledgement", true));
		}

		if (ZoomNetwork.getDisableZoom()) {
			list.addSingleOptionEntry(new SpruceLabelOption("command.mio_zoomer.restrictions.disable_zoom", true));
		}

		if (ZoomNetwork.getDisableZoomScrolling()) {
			list.addSingleOptionEntry(new SpruceLabelOption("command.mio_zoomer.restrictions.disable_zoom_scrolling", true));
		}

		if (ZoomNetwork.getForceClassicMode()) {
			list.addSingleOptionEntry(new SpruceLabelOption("command.mio_zoomer.restrictions.force_classic_mode", true));
		}

		if (ZoomNetwork.getForceZoomDivisors()) {
			double minimumZoomDivisor = ZoomNetwork.getMinimumZoomDivisor();
			double maximumZoomDivisor = ZoomNetwork.getMaximumZoomDivisor();
			list.addSingleOptionEntry(new SpruceLabelOption(
				"command.mio_zoomer.restrictions.force_zoom_divisors",
				minimumZoomDivisor != maximumZoomDivisor
					? Text.translatable("command.mio_zoomer.restrictions.force_zoom_divisors", minimumZoomDivisor, maximumZoomDivisor)
					: Text.translatable("command.mio_zoomer.restrictions.force_zoom_divisor", minimumZoomDivisor),
				true)
			);
		}

		if (ZoomNetwork.getSpyglassDependency()) {
			var key = switch (MioZoomerConfigManager.CONFIG.features.spyglass_dependency.value()) {
				case REQUIRE_ITEM -> "command.mio_zoomer.restrictions.force_spyglass.require_item";
				case REPLACE_ZOOM -> "command.mio_zoomer.restrictions.force_spyglass.replace_zoom";
				case BOTH -> "command.mio_zoomer.restrictions.force_spyglass.both";
				default -> "";
			};
			list.addSingleOptionEntry(new SpruceLabelOption(key, true));
		}

		if (ZoomNetwork.getSpyglassOverlay()) {
			list.addSingleOptionEntry(new SpruceLabelOption("command.mio_zoomer.restrictions.force_spyglass_overlay", true));
		}

		if (!ZoomNetwork.getHasRestrictions()) {
			boolean acknowledged = ZoomNetwork.getAcknowledgement().equals(ZoomNetwork.Acknowledgement.HAS_NO_RESTRICTIONS);
			list.addSingleOptionEntry(new SpruceLabelOption(acknowledged
				? "command.mio_zoomer.restrictions.no_restrictions.acknowledged"
				: "command.mio_zoomer.restrictions.no_restrictions",
				true)
			);
		}

		this.addDrawableChild(list);
		this.addDrawableChild(new SpruceButtonWidget(Position.of(this, this.width / 2 - 100, this.height - 28), 200, 20, SpruceTexts.GUI_DONE,
			btn -> this.client.setScreen(null)).asVanilla());
	}

	@Override
	public void renderTitle(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		graphics.drawCenteredShadowedText(this.textRenderer, this.title, this.width / 2, 8, 0xFFFFFF);
	}
}
