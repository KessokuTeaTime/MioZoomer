package org.thinkingstudio.mio_zoomer;

import org.quiltmc.config.api.annotations.ConfigFieldAnnotationProcessor;

import org.thinkingstudio.mio_zoomer.config.metadata.WidgetSize;

public class MioZoomerPreLaunchMod {

	public static void onPreLaunch() {
		ConfigFieldAnnotationProcessor.register(WidgetSize.class, new WidgetSize.Processor());
	}
}
