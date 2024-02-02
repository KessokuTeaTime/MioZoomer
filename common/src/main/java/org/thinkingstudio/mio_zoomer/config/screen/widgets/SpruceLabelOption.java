package org.thinkingstudio.mio_zoomer.config.screen.widgets;


import org.jetbrains.annotations.Nullable;

import net.minecraft.text.Text;
import org.thinkingstudio.obsidianui.Position;
import org.thinkingstudio.obsidianui.option.SpruceOption;
import org.thinkingstudio.obsidianui.widget.SpruceLabelWidget;
import org.thinkingstudio.obsidianui.widget.SpruceWidget;

public class SpruceLabelOption extends SpruceOption {
	private final Text text;
	private final boolean centered;

	public SpruceLabelOption(String key, boolean centered) {
		this(key, Text.translatable(key), centered);
	}

	public SpruceLabelOption(String key, Text text, boolean centered, @Nullable Text tooltip) {
		this(key, text, centered);
		this.setTooltip(tooltip);
	}

	public SpruceLabelOption(String key, Text text, boolean centered) {
		super(key);
		this.text = text;
		this.centered = centered;
	}

	@Override
	public SpruceWidget createWidget(Position position, int width) {
		var label = new SpruceLabelWidget(position, this.text, width, this.centered);
		this.getOptionTooltip().ifPresent(label::setTooltip);
		return label;
	}
}
