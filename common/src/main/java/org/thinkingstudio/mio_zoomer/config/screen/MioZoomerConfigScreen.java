package org.thinkingstudio.mio_zoomer.config.screen;

import java.lang.reflect.Field;
import java.util.Map;

import ho.artisan.azusa_config.shadow.quiltconfig.api.Constraint;
import ho.artisan.azusa_config.shadow.quiltconfig.api.values.TrackedValue;
import ho.artisan.azusa_config.shadow.quiltconfig.api.values.ValueTreeNode;

import org.thinkingstudio.mio_zoomer.config.ConfigEnums;
import org.thinkingstudio.mio_zoomer.config.metadata.WidgetSize;
import org.thinkingstudio.mio_zoomer.config.screen.widgets.CustomTextureBackground;
import org.thinkingstudio.mio_zoomer.config.screen.widgets.SpruceBoundedDoubleInputOption;
import org.thinkingstudio.mio_zoomer.config.screen.widgets.SpruceBoundedIntegerInputOption;
import org.thinkingstudio.mio_zoomer.utils.ZoomUtils;
import org.thinkingstudio.obsidianui.Position;
import org.thinkingstudio.obsidianui.SpruceTexts;
import org.thinkingstudio.obsidianui.option.SpruceBooleanOption;
import org.thinkingstudio.obsidianui.option.SpruceCyclingOption;
import org.thinkingstudio.obsidianui.option.SpruceOption;
import org.thinkingstudio.obsidianui.option.SpruceSeparatorOption;
import org.thinkingstudio.obsidianui.option.SpruceSimpleActionOption;
import org.thinkingstudio.obsidianui.screen.SpruceScreen;
import org.thinkingstudio.obsidianui.widget.SpruceButtonWidget;
import org.thinkingstudio.obsidianui.widget.container.SpruceOptionListWidget;
import org.thinkingstudio.mio_zoomer.config.MioZoomerConfigManager;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

// TODO - Use a completely different approach that allows for a more user-friendly config screen and that yet is easy to make/edit
public class MioZoomerConfigScreen extends SpruceScreen {
	private static final CustomTextureBackground NORMAL_BACKGROUND = new CustomTextureBackground(new Identifier("minecraft:textures/block/yellow_terracotta.png"), 0.25F, 0.25F, 0.25F, 1.0F);
	private static final CustomTextureBackground DARKENED_BACKGROUND = new CustomTextureBackground(new Identifier("minecraft:textures/block/yellow_terracotta.png"), 0.125F, 0.125F, 0.125F, 1.0F);

	private SpruceOptionListWidget list;
	private final Screen parent;
	private ConfigEnums.ZoomPresets preset;

	private Map<TrackedValue<Object>, Object> newValues;
	private SpruceOption optionBuffer;

	public MioZoomerConfigScreen(Screen parent) {
		super(Text.translatable("config.mio_zoomer.title"));
		this.parent = parent;
		this.preset = ConfigEnums.ZoomPresets.DEFAULT;

		this.newValues = new Reference2ObjectArrayMap<>();
		this.optionBuffer = null;
	}

	// Unlike other options, the cycling option doesn't attach the prefix on the text;
	// So we do it ourselves automatically!
	private static Text getCyclingOptionText(String text, Text prefix) {
		return Text.translatable(
			"spruceui.options.generic",
			prefix,
			text != null ? Text.translatable(text) : Text.literal("Error"));
	}

	@Override
	protected void init() {
		super.init();
		this.list = new SpruceOptionListWidget(Position.of(0, 22), this.width, this.height - 36 - 22);
		this.list.setBackground(DARKENED_BACKGROUND);

		this.initializeOptionList(this.list);
		this.appendPresetSection(this.list);

		this.addDrawableChild(this.list);
		this.addDrawableChild(new SpruceButtonWidget(Position.of(this, this.width / 2 - 154, this.height - 28), 150, 20, Text.translatable("config.mio_zoomer.discard_changes"),
			btn -> {
				this.resetNewValues();
				this.refresh();
			}).asVanilla());
		this.addDrawableChild(new SpruceButtonWidget(Position.of(this, this.width / 2 + 4, this.height - 28), 150, 20, SpruceTexts.GUI_DONE,
			btn -> {
				this.newValues.forEach((trackedValue, newValue) -> {
					if (trackedValue.value() != null) {
						trackedValue.setValue(newValue, false);
					}
				});
				MioZoomerConfigManager.CONFIG.save();
				this.client.setScreen(this.parent);
			}).asVanilla());
	}

	@Override
	public void renderTitle(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		graphics.drawCenteredShadowedText(this.textRenderer, this.title, this.width / 2, 8, 0xFFFFFF);
	}

	@Override
	public void renderBackgroundTexture(GuiGraphics graphics) {
		NORMAL_BACKGROUND.render(graphics, this);
	}

	@Override
	public void removed() {
		this.newValues.forEach((trackedValue, newValue) -> trackedValue.setValue(newValue, false));
		MioZoomerConfigManager.CONFIG.save();
	}

	@Override
	public void closeScreen() {
		this.client.setScreen(this.parent);
	}

	@SuppressWarnings("unchecked")
	private void resetNewValues() {
		this.newValues = new Reference2ObjectArrayMap<>();

		for (TrackedValue<?> trackedValue : MioZoomerConfigManager.CONFIG.values()) {
			if (trackedValue.getRealValue() != null) {
				newValues.put((TrackedValue<Object>) trackedValue, trackedValue.getRealValue());
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void initializeOptionList(SpruceOptionListWidget options) {
		for (ValueTreeNode node : MioZoomerConfigManager.CONFIG.nodes()) {
			if (node instanceof ValueTreeNode.Section section) {
				var separator = new SpruceSeparatorOption(
					String.format("config.mio_zoomer.%s", section.key()),
					true,
					Text.translatable(String.format("config.mio_zoomer.%s.tooltip", section.key())));
				this.addOptionToList(options, separator, WidgetSize.Size.FULL);

				for (ValueTreeNode subNode : section) {
					WidgetSize.Size size = subNode.metadata(WidgetSize.TYPE);

					if (subNode instanceof TrackedValue<?> trackedValue) {
						var trackie = (TrackedValue<Object>) trackedValue;
						this.newValues.putIfAbsent(trackie, trackedValue.getRealValue());

						if (trackedValue.value() instanceof Boolean) {
							SpruceOption option;
							if (!trackedValue.equals(MioZoomerConfigManager.UNBIND_CONFLICTING_KEY)) {
								option = new SpruceBooleanOption(
									String.format("config.mio_zoomer.%s", trackedValue.key()),
									() -> (Boolean) this.newValues.get(trackie),
									value -> this.newValues.replace(trackie, value),
									Text.translatable(String.format("config.mio_zoomer.%s.tooltip", trackedValue.key())));
							} else {
								// TODO - ew, hardcoding; we can do better than that
								option = SpruceSimpleActionOption.of(
									"config.mio_zoomer.tweaks.unbind_conflicting_key",
									button -> ZoomUtils.unbindConflictingKey(client, true),
									Text.translatable("config.mio_zoomer.tweaks.unbind_conflicting_key.tooltip"));
							}
							this.addOptionToList(options, option, size);
						} else if (trackedValue.value() instanceof Double) {
							double minimum = Double.MIN_VALUE;
							double maximum = Double.MAX_VALUE;
							for (Constraint<?> constraint : trackedValue.constraints()) {
								if (constraint instanceof Constraint.Range<?>) {
									try {
										Field minField = Constraint.Range.class.getDeclaredField("min");
										Field maxField = Constraint.Range.class.getDeclaredField("max");

										minField.setAccessible(true);
										maxField.setAccessible(true);

										minimum = Math.max((Double) minField.get(constraint), minimum);
										maximum = Math.min((Double) maxField.get(constraint), maximum);
									} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
										e.printStackTrace();
									}
								}
							}

							var option = new SpruceBoundedDoubleInputOption(
								String.format("config.mio_zoomer.%s", trackedValue.key()),
								minimum, maximum,
								() -> (Double) this.newValues.get(trackie),
								value -> this.newValues.replace(trackie, value),
								Text.translatable(String.format("config.mio_zoomer.%s.tooltip", trackedValue.key())));
							this.addOptionToList(options, option, size);
						} else if (trackedValue.value() instanceof Integer) {
							int minimum = Integer.MIN_VALUE;
							int maximum = Integer.MAX_VALUE;
							for (Constraint<?> constraint : trackedValue.constraints()) {
								if (constraint instanceof Constraint.Range<?>) {
									try {
										Field minField = Constraint.Range.class.getDeclaredField("min");
										Field maxField = Constraint.Range.class.getDeclaredField("max");

										minField.setAccessible(true);
										maxField.setAccessible(true);

										minimum = Math.max((Integer) minField.get(constraint), minimum);
										maximum = Math.min((Integer) maxField.get(constraint), maximum);
									} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
										e.printStackTrace();
									}
								}
							}

							var option = new SpruceBoundedIntegerInputOption(
								String.format("config.mio_zoomer.%s", trackedValue.key()),
								minimum, maximum,
								() -> (Integer) this.newValues.get(trackie),
								value -> this.newValues.replace(trackie, value),
								Text.translatable(String.format("config.mio_zoomer.%s.tooltip", trackedValue.key())));
							this.addOptionToList(options, option, size);
						} else if (trackedValue.value() instanceof ConfigEnums.ConfigEnum) {
							var option = new SpruceCyclingOption(
								String.format("config.mio_zoomer.%s", trackedValue.key()),
								amount -> this.newValues.replace(trackie, ((ConfigEnums.ConfigEnum) this.newValues.get(trackie)).next()),
								option2 -> getCyclingOptionText(String.format("config.mio_zoomer.%s.%s", trackedValue.key(), this.newValues.get(trackie).toString().toLowerCase()), option2.getPrefix()),
								Text.translatable(String.format("config.mio_zoomer.%s.tooltip", trackedValue.key())));
							this.addOptionToList(options, option, size);
						}
					}
				}
			}
		}

		if (this.optionBuffer != null) {
			this.list.addOptionEntry(optionBuffer, null);
			this.optionBuffer = null;
		}
	}

	private void appendPresetSection(SpruceOptionListWidget options) {
		// "Reset" category separator
		var resetSeparator = new SpruceSeparatorOption(
			"config.mio_zoomer.reset",
			true,
			Text.translatable("config.mio_zoomer.reset.tooltip"));

		// Preset
		var presetOption = new SpruceCyclingOption(
			"config.mio_zoomer.reset.preset",
			amount -> this.preset = (ConfigEnums.ZoomPresets) this.preset.next(),
			option -> getCyclingOptionText(String.format("config.mio_zoomer.reset.preset.%s", this.preset.toString().toLowerCase()), option.getPrefix()),
			Text.translatable("config.mio_zoomer.reset.preset.tooltip"));

		// Reset Settings
		var resetSettingsOption = SpruceSimpleActionOption.of(
			"config.mio_zoomer.reset.reset_settings",
			button -> this.resetToPreset(this.preset),
			Text.translatable("config.mio_zoomer.reset.reset_settings.tooltip"));

		options.addSingleOptionEntry(resetSeparator);
		options.addOptionEntry(presetOption, resetSettingsOption);
	}

	private void addOptionToList(SpruceOptionListWidget options, SpruceOption option, WidgetSize.Size size) {
		if (size == WidgetSize.Size.HALF) {
			if (optionBuffer == null) {
				optionBuffer = option;
			} else {
				this.list.addOptionEntry(optionBuffer, option);
				optionBuffer = null;
			}
		} else {
			if (optionBuffer != null) {
				this.list.addOptionEntry(optionBuffer, null);
				optionBuffer = null;
			}
			this.list.addSingleOptionEntry(option);
		}
	}

	private void refresh() {
		var scrollAmount = this.list.getScrollAmount();
		this.init(client, width, height);
		this.list.setScrollAmount(scrollAmount);
	}

	@SuppressWarnings("unchecked")
	public void resetToPreset(ConfigEnums.ZoomPresets preset) {
		Map<TrackedValue<?>, Object> presets = Map.ofEntries(
			Map.entry(MioZoomerConfigManager.CINEMATIC_CAMERA, preset == ConfigEnums.ZoomPresets.CLASSIC ? ConfigEnums.CinematicCameraOptions.VANILLA : ConfigEnums.CinematicCameraOptions.OFF),
			Map.entry(MioZoomerConfigManager.REDUCE_SENSITIVITY, preset == ConfigEnums.ZoomPresets.CLASSIC ? false : true),
			Map.entry(MioZoomerConfigManager.ZOOM_TRANSITION, preset == ConfigEnums.ZoomPresets.CLASSIC ? ConfigEnums.ZoomTransitionOptions.OFF : ConfigEnums.ZoomTransitionOptions.SMOOTH),
			Map.entry(MioZoomerConfigManager.ZOOM_MODE, preset == ConfigEnums.ZoomPresets.PERSISTENT ? ConfigEnums.ZoomModes.PERSISTENT : ConfigEnums.ZoomModes.HOLD),
			Map.entry(MioZoomerConfigManager.ZOOM_SCROLLING, switch (preset) {
				case CLASSIC -> false;
				case SPYGLASS -> false;
				default -> true;
			}),
			Map.entry(MioZoomerConfigManager.EXTRA_KEY_BINDS, preset == ConfigEnums.ZoomPresets.CLASSIC ? false : true),
			Map.entry(MioZoomerConfigManager.ZOOM_OVERLAY, preset == ConfigEnums.ZoomPresets.SPYGLASS ? ConfigEnums.ZoomOverlays.SPYGLASS : ConfigEnums.ZoomOverlays.OFF),
			Map.entry(MioZoomerConfigManager.SPYGLASS_DEPENDENCY, preset == ConfigEnums.ZoomPresets.SPYGLASS ? ConfigEnums.SpyglassDependency.BOTH : ConfigEnums.SpyglassDependency.OFF),
			Map.entry(MioZoomerConfigManager.ZOOM_DIVISOR, switch (preset) {
				case PERSISTENT -> 1.0D;
				case SPYGLASS -> 10.0D;
				default -> 4.0D;
			}),
			Map.entry(MioZoomerConfigManager.MINIMUM_ZOOM_DIVISOR, 1.0D),
			Map.entry(MioZoomerConfigManager.MAXIMUM_ZOOM_DIVISOR, 50.0D),
			Map.entry(MioZoomerConfigManager.UPPER_SCROLL_STEPS, preset == ConfigEnums.ZoomPresets.SPYGLASS ? 16 : 20),
			Map.entry(MioZoomerConfigManager.LOWER_SCROLL_STEPS, preset == ConfigEnums.ZoomPresets.SPYGLASS ? 8 : 4),
			Map.entry(MioZoomerConfigManager.SMOOTH_MULTIPLIER, preset == ConfigEnums.ZoomPresets.SPYGLASS ? 0.5D : 0.75D),
			Map.entry(MioZoomerConfigManager.CINEMATIC_MULTIPLIER, 4.0D),
			Map.entry(MioZoomerConfigManager.MINIMUM_LINEAR_STEP, 0.125D),
			Map.entry(MioZoomerConfigManager.MAXIMUM_LINEAR_STEP, 0.25D),
			Map.entry(MioZoomerConfigManager.RESET_ZOOM_WITH_MOUSE, preset == ConfigEnums.ZoomPresets.CLASSIC ? false : true),
			Map.entry(MioZoomerConfigManager.FORGET_ZOOM_DIVISOR, true),
			Map.entry(MioZoomerConfigManager.UNBIND_CONFLICTING_KEY, false),
			Map.entry(MioZoomerConfigManager.USE_SPYGLASS_TEXTURE, preset == ConfigEnums.ZoomPresets.SPYGLASS ? true : false),
			Map.entry(MioZoomerConfigManager.USE_SPYGLASS_SOUNDS, preset == ConfigEnums.ZoomPresets.SPYGLASS ? true : false),
			Map.entry(MioZoomerConfigManager.SHOW_RESTRICTION_TOASTS, true),
			//Map.entry(MioZoomerConfigManager.PRINT_OWO_ON_START, preset == ConfigEnums.ZoomPresets.CLASSIC ? false : true),
			Map.entry(MioZoomerConfigManager.PRINT_OWO_ON_START, false)
		);

		this.newValues = new Reference2ObjectArrayMap<>();

		for (TrackedValue<?> trackedValue : MioZoomerConfigManager.CONFIG.values()) {
			this.newValues.put((TrackedValue<Object>) trackedValue, presets.get(trackedValue));
		}

		this.refresh();
	}
}
