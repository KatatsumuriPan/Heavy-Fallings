package kpan.heavy_fallings.config.core;

import java.io.BufferedWriter;
import java.io.IOException;
import kpan.heavy_fallings.config.core.gui.ModGuiConfig;
import kpan.heavy_fallings.config.core.gui.ModGuiConfigEntries;
import kpan.heavy_fallings.config.core.gui.ModGuiConfigEntries.IGuiConfigEntry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IConfigElement {
	int getOrder();
	ConfigSide getSide();

	void write(BufferedWriter out, int indent, String path) throws IOException;
	boolean showInGui();

	@SideOnly(Side.CLIENT)
	IGuiConfigEntry toEntry(ModGuiConfig screen, ModGuiConfigEntries entryList);
}
