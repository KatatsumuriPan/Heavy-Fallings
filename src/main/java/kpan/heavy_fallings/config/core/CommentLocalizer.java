package kpan.heavy_fallings.config.core;

import kpan.heavy_fallings.ModMain;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CommentLocalizer {

	public static String tryLocalize(String localizationKey, String defaultValue) {
		if (!ModMain.proxy.hasClientSide())
			return defaultValue;

		String localized = format(localizationKey);
		if (localizationKey.equals(localized))
			return defaultValue;
		else
			return localized;
	}

	@SideOnly(Side.CLIENT)
	private static String format(String localizationKey) {
		return I18n.format(localizationKey);
	}
}
