package org.thinkingstudio.mio_zoomer.config.screen.widgets;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.thinkingstudio.obsidianui.Position;
import org.thinkingstudio.obsidianui.option.SpruceOption;
import org.thinkingstudio.obsidianui.widget.SpruceWidget;
import org.thinkingstudio.obsidianui.widget.text.SpruceNamedTextFieldWidget;
import org.thinkingstudio.obsidianui.widget.text.SpruceTextFieldWidget;

public class SpruceBoundedIntegerInputOption extends SpruceOption {
	private final Supplier<Integer> getter;
	private final Consumer<Integer> setter;
	private final Text tooltip;
	private final Integer minimum;
	private final Integer maximum;

	public SpruceBoundedIntegerInputOption(String key, Integer minimum, Integer maximum, Supplier<Integer> getter, Consumer<Integer> setter, @Nullable Text tooltip) {
		super(key);
		this.minimum = minimum;
		this.maximum = maximum;
		this.getter = getter;
		this.setter = setter;
		this.tooltip = tooltip;
	}

	@Override
	public SpruceWidget createWidget(Position position, int width) {
		var textField = new SpruceTextFieldWidget(position, width, 20, this.getPrefix());
		textField.setText(String.valueOf(this.get()));
		textField.setTextPredicate(SpruceTextFieldWidget.INTEGER_INPUT_PREDICATE);
		textField.setRenderTextProvider((displayedText, offset) -> {
			textField.setTooltip(Text.empty());
			var tooltipText = Text.empty().append(this.tooltip);
			Style tooltipStyle = Style.EMPTY;

			try {
				int value = Integer.parseInt(textField.getText());
				var bound = boundCheck(value);

				if (bound.isPresent()) {
					tooltipStyle = tooltipStyle.withColor(Formatting.RED);
					if (!bound.get()) {
						tooltipText.append("\n");
						tooltipText.append(minimum == Integer.MIN_VALUE
							? Text.translatable("config.mio_zoomer.widget.bounded_integer.below_legal").setStyle(tooltipStyle)
							: Text.translatable("config.mio_zoomer.widget.bounded_integer.below_range", minimum.toString()).setStyle(tooltipStyle)
						);
					} else {
						tooltipText.append("\n");
						tooltipText.append(maximum == Integer.MAX_VALUE
							? Text.translatable("config.mio_zoomer.widget.bounded_integer.above_legal").setStyle(tooltipStyle)
							: Text.translatable("config.mio_zoomer.widget.bounded_integer.above_range", maximum.toString()).setStyle(tooltipStyle)
						);
					}
				}
				textField.setTooltip(tooltipText);
				return OrderedText.forward(displayedText, tooltipStyle);
			} catch (NumberFormatException e) {
				return OrderedText.forward(displayedText, Style.EMPTY.withColor(Formatting.RED));
			}
		});
		textField.setChangedListener(input -> {
			try {
				this.set(Integer.parseInt(input));
			} catch (NumberFormatException e) {
				this.set(null);
			}
		});
		this.setTooltip(this.tooltip);
		return new SpruceNamedTextFieldWidget(textField);
	}

	public void set(Integer value) {
		this.setter.accept(value);
	}

	public Integer get() {
		return this.getter.get();
	}

	private Optional<Boolean> boundCheck(int value) {
		if (value < minimum) {
			return Optional.of(false);
		} else if (value > maximum) {
			return Optional.of(true);
		}

		return Optional.empty();
	}
}
