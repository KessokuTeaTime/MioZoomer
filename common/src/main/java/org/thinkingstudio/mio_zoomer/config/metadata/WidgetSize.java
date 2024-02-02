package org.thinkingstudio.mio_zoomer.config.metadata;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;

import ho.artisan.azusa_config.shadow.quiltconfig.api.annotations.ConfigFieldAnnotationProcessor;
import ho.artisan.azusa_config.shadow.quiltconfig.api.metadata.MetadataContainerBuilder;
import ho.artisan.azusa_config.shadow.quiltconfig.api.metadata.MetadataType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface WidgetSize {
	MetadataType<Size, Builder> TYPE = MetadataType.create(() -> Optional.of(Size.FULL), Builder::new);

	WidgetSize.Size value();

	final class Processor implements ConfigFieldAnnotationProcessor<WidgetSize> {
		@Override
		public void process(WidgetSize annotation, MetadataContainerBuilder<?> builder) {
			builder.metadata(TYPE, size -> size.set(annotation.value()));
		}
	}

	final class Builder implements MetadataType.Builder<Size> {
		private Size size = Size.FULL;

		public void set(Size size) {
			this.size = size;
		}

		@Override
		public Size build() {
			return this.size;
		}
	}

	enum Size {
		HALF,
		FULL
	}
}
