package org.thinkingstudio.mio_zoomer.config.screen;

import java.lang.reflect.Field;
import java.util.Map;

import ho.artisan.azusa_config.shadow.quiltconfig.api.Constraint;
import ho.artisan.azusa_config.shadow.quiltconfig.api.values.TrackedValue;
import ho.artisan.azusa_config.shadow.quiltconfig.api.values.ValueTreeNode;

import net.minecraft.util.CommonColors;
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

	private final Map<TrackedValue<Object>, Object> newValues;
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

		this.initializeOptionList();
		this.appendPresetSection();

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
		graphics.drawCenteredShadowedText(this.textRenderer, this.title, this.width / 2, 8, CommonColors.WHITE);
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
		this.newValues.clear();

		for (TrackedValue<?> trackedValue : MioZoomerConfigManager.CONFIG.values()) {
			if (trackedValue.getRealValue() != null) {
				newValues.put((TrackedValue<Object>) trackedValue, trackedValue.getRealValue());
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void initializeOptionList() {
		for (var node : MioZoomerConfigManager.CONFIG.nodes()) {
			if (node instanceof ValueTreeNode.Section section) {
				var separator = new SpruceSeparatorOption(
					String.format("config.mio_zoomer.%s", section.key()),
					true,
					Text.translatable(String.format("config.mio_zoomer.%s.tooltip", section.key())));
				this.addOptionToList(separator, WidgetSize.Size.FULL);

				for (var subNode : section) {
					var size = subNode.metadata(WidgetSize.TYPE);

					if (subNode instanceof TrackedValue<?> trackedValue) {
						var trackie = (TrackedValue<Object>) trackedValue;
						this.newValues.putIfAbsent(trackie, trackedValue.getRealValue());

						if (trackedValue.value() instanceof Boolean) {
							SpruceOption option;
							if (!trackedValue.equals(MioZoomerConfigManager.CONFIG.tweaks.unbind_conflicting_key)) {
								option = new SpruceBooleanOption(
									String.format("config.mio_zoomer.%s", trackedValue.key()),
									() -> (Boolean) this.newValues.get(trackie),
									value -> this.newValues.replace(trackie, value),
									Text.translatable(String.format("config.mio_zoomer.%s.tooltip", trackedValue.key())));
							} else {
								// TODO - ew, hardcoding; we can do better than that
								option = SpruceSimpleActionOption.of(
									"config.mio_zoomer.tweaks.unbind_conflicting_key",
									button -> ZoomUtils.unbindConflictingKey(this.client, true),
									Text.translatable("config.mio_zoomer.tweaks.unbind_conflicting_key.tooltip"));
							}
							this.addOptionToList(option, size);
						} else if (trackedValue.value() instanceof Double) {
							double minimum = Double.MIN_VALUE;
							double maximum = Double.MAX_VALUE;
							for (Constraint<?> constraint : trackedValue.constraints()) {
								if (constraint instanceof Constraint.Range<?>) {
									try {
										var minField = Constraint.Range.class.getDeclaredField("min");
										var maxField = Constraint.Range.class.getDeclaredField("max");

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
							this.addOptionToList(option, size);
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
							this.addOptionToList(option, size);
						} else if (trackedValue.value() instanceof ConfigEnums.ConfigEnum) {
							var option = new SpruceCyclingOption(
								String.format("config.mio_zoomer.%s", trackedValue.key()),
								amount -> this.newValues.replace(trackie, ((ConfigEnums.ConfigEnum) this.newValues.get(trackie)).next()),
								option2 -> getCyclingOptionText(String.format("config.mio_zoomer.%s.%s", trackedValue.key(), this.newValues.get(trackie).toString().toLowerCase()), option2.getPrefix()),
								Text.translatable(String.format("config.mio_zoomer.%s.tooltip", trackedValue.key())));
							this.addOptionToList(option, size);
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

	private void appendPresetSection() {
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

		this.list.addSingleOptionEntry(resetSeparator);
		this.list.addOptionEntry(presetOption, resetSettingsOption);
	}

	private void addOptionToList(SpruceOption option, WidgetSize.Size size) {
		if (size == WidgetSize.Size.HALF) {
			if (this.optionBuffer == null) {
				this.optionBuffer = option;
			} else {
				this.list.addOptionEntry(this.optionBuffer, option);
				this.optionBuffer = null;
			}
		} else {
			if (this.optionBuffer != null) {
				this.list.addOptionEntry(this.optionBuffer, null);
				this.optionBuffer = null;
			}
			this.list.addSingleOptionEntry(option);
		}
	}

	private void refresh() {
		var scrollAmount = this.list.getScrollAmount();
		this.init(this.client, this.width, this.height);
		this.list.setScrollAmount(scrollAmount);
	}

	@SuppressWarnings("unchecked")
	public void resetToPreset(ConfigEnums.ZoomPresets preset) {
		Map<TrackedValue<?>, Object> presets = Map.ofEntries(
			Map.entry(MioZoomerConfigManager.CONFIG.features.cinematic_camera, preset == ConfigEnums.ZoomPresets.CLASSIC ? ConfigEnums.CinematicCameraOptions.VANILLA : ConfigEnums.CinematicCameraOptions.OFF),
			Map.entry(MioZoomerConfigManager.CONFIG.features.reduce_sensitivity, preset == ConfigEnums.ZoomPresets.CLASSIC ? false : true),
			Map.entry(MioZoomerConfigManager.CONFIG.features.zoom_transition, preset == ConfigEnums.ZoomPresets.CLASSIC ? ConfigEnums.ZoomTransitionOptions.OFF : ConfigEnums.ZoomTransitionOptions.SMOOTH),
			Map.entry(MioZoomerConfigManager.CONFIG.features.zoom_mode, preset == ConfigEnums.ZoomPresets.PERSISTENT ? ConfigEnums.ZoomModes.PERSISTENT : ConfigEnums.ZoomModes.HOLD),
			Map.entry(MioZoomerConfigManager.CONFIG.features.zoom_scrolling, switch (preset) {
				case CLASSIC -> false;
				case SPYGLASS -> false;
				default -> true;
			}),
			Map.entry(MioZoomerConfigManager.CONFIG.features.extra_key_binds, preset == ConfigEnums.ZoomPresets.CLASSIC ? false : true),
			Map.entry(MioZoomerConfigManager.CONFIG.features.zoom_overlay, preset == ConfigEnums.ZoomPresets.SPYGLASS ? ConfigEnums.ZoomOverlays.SPYGLASS : ConfigEnums.ZoomOverlays.OFF),
			Map.entry(MioZoomerConfigManager.CONFIG.features.spyglass_dependency, preset == ConfigEnums.ZoomPresets.SPYGLASS ? ConfigEnums.SpyglassDependency.BOTH : ConfigEnums.SpyglassDependency.OFF),
			Map.entry(MioZoomerConfigManager.CONFIG.values.zoom_divisor, switch (preset) {
				case PERSISTENT -> 1.0D;
				case SPYGLASS -> 10.0D;
				default -> 4.0D;
			}),
			Map.entry(MioZoomerConfigManager.CONFIG.values.minimum_zoom_divisor, 1.0D),
			Map.entry(MioZoomerConfigManager.CONFIG.values.maximum_zoom_divisor, 50.0D),
			Map.entry(MioZoomerConfigManager.CONFIG.values.upper_scroll_steps, preset == ConfigEnums.ZoomPresets.SPYGLASS ? 16 : 20),
			Map.entry(MioZoomerConfigManager.CONFIG.values.lower_scroll_steps, preset == ConfigEnums.ZoomPresets.SPYGLASS ? 8 : 4),
			Map.entry(MioZoomerConfigManager.CONFIG.values.smooth_multiplier, preset == ConfigEnums.ZoomPresets.SPYGLASS ? 0.5D : 0.75D),
			Map.entry(MioZoomerConfigManager.CONFIG.values.cinematic_multiplier, 4.0D),
			Map.entry(MioZoomerConfigManager.CONFIG.values.minimum_linear_step, 0.125D),
			Map.entry(MioZoomerConfigManager.CONFIG.values.maximum_linear_step, 0.25D),
			Map.entry(MioZoomerConfigManager.CONFIG.tweaks.reset_zoom_with_mouse, preset == ConfigEnums.ZoomPresets.CLASSIC ? false : true),
			Map.entry(MioZoomerConfigManager.CONFIG.tweaks.forget_zoom_divisor, true),
			Map.entry(MioZoomerConfigManager.CONFIG.tweaks.unbind_conflicting_key, false),
			Map.entry(MioZoomerConfigManager.CONFIG.tweaks.use_spyglass_texture, preset == ConfigEnums.ZoomPresets.SPYGLASS ? true : false),
			Map.entry(MioZoomerConfigManager.CONFIG.tweaks.use_spyglass_sounds, preset == ConfigEnums.ZoomPresets.SPYGLASS ? true : false),
			Map.entry(MioZoomerConfigManager.CONFIG.tweaks.show_restriction_toasts, true),
			//Map.entry(MioZoomerConfigManager.CONFIG.tweaks.print_owo_on_start, preset == ZoomPresets.CLASSIC ? false : true)
			Map.entry(MioZoomerConfigManager.CONFIG.tweaks.print_owo_on_start, false)
		);

		this.newValues.clear();

		for (TrackedValue<?> trackedValue : MioZoomerConfigManager.CONFIG.values()) {
			this.newValues.put((TrackedValue<Object>) trackedValue, presets.get(trackedValue));
		}

		this.refresh();
	}
}
