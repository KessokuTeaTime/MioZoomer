package org.thinkingstudio.mio_zoomer;

import ho.artisan.azusa_config.shadow.quiltconfig.api.annotations.ConfigFieldAnnotationProcessors;

import org.thinkingstudio.mio_zoomer.config.metadata.WidgetSize;

public class MioZoomerPreLaunchMod {
	public static void onPreLaunch() {
		ConfigFieldAnnotationProcessors.register(WidgetSize.class, new WidgetSize.Processor());
	}
}
