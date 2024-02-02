package org.thinkingstudio.mio_zoomer;

import ho.artisan.azusa_config.shadow.quiltconfig.api.annotations.ConfigFieldAnnotationProcessor;

import org.thinkingstudio.mio_zoomer.config.metadata.WidgetSize;

public class MioZoomerPreLaunchMod {
	public static void onPreLaunch() {
		ConfigFieldAnnotationProcessor.register(WidgetSize.class, new WidgetSize.Processor());
	}
}
