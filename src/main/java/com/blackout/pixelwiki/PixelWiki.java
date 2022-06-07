package com.blackout.pixelwiki;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forgespi.language.IModInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.artifact.versioning.ArtifactVersion;

import java.util.Optional;

@Mod(PixelWiki.MODID)
public class PixelWiki {
	public static final String MODID = "pixelwiki";
	public static final String MODNAME = "PixelWiki";
	public static ArtifactVersion VERSION = null;
	public static final Logger LOGGER = LogManager.getLogger(MODID);

	public PixelWiki() {
		Optional<? extends ModContainer> opt = ModList.get().getModContainerById(MODID);
		if (opt.isPresent()) {
			IModInfo modInfo = opt.get().getModInfo();
			VERSION = modInfo.getVersion();
		} else LOGGER.warn("Cannot get version from mod info");

		LOGGER.debug(MODNAME + " Version is: " + VERSION);
		LOGGER.debug("Mod ID for " + MODNAME + " is: " + MODID);

		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		forgeBus.addListener(MiscEventHandler::onRegisterCommandEvent);
		forgeBus.register(this);
	}
}