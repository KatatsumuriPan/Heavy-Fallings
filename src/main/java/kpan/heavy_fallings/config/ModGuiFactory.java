package kpan.heavy_fallings.config;

import java.util.Set;
import kpan.heavy_fallings.ModMain;
import kpan.heavy_fallings.ModTagsGenerated;
import kpan.heavy_fallings.config.core.gui.ModGuiConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

public class ModGuiFactory implements IModGuiFactory {

	@Override
	public void initialize(Minecraft minecraftInstance) {

	}

	@Override
	public boolean hasConfigGui() {
		return true;
	}

	@Override
	public GuiScreen createConfigGui(GuiScreen parentScreen) {
		return new ModGuiConfig(parentScreen, ModMain.defaultConfig.getRootCategory().getOrderedElements(), null, false, false, ModTagsGenerated.MODNAME, "");
	}

	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return null;
	}

}
