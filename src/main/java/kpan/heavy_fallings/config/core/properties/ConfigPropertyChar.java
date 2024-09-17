package kpan.heavy_fallings.config.core.properties;

import kpan.heavy_fallings.config.core.ConfigSide;
import kpan.heavy_fallings.config.core.gui.ModGuiConfig;
import kpan.heavy_fallings.config.core.gui.ModGuiConfigEntries;
import kpan.heavy_fallings.config.core.gui.ModGuiConfigEntries.CharEntry;
import kpan.heavy_fallings.config.core.gui.ModGuiConfigEntries.IGuiConfigEntry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ConfigPropertyChar extends AbstractConfigProperty {

	public static final String TYPE = "C";

	private final char defaultValue;
	private char value;
	public ConfigPropertyChar(String id, char defaultValue, String commentForFile, int order, ConfigSide side) {
		super(id, commentForFile, order, side);
		this.defaultValue = defaultValue;
		value = defaultValue;
	}

	public char getValue() {
		return value;
	}
	public void setValue(char value) {
		this.value = value;
		dirty = true;
	}

	@Override
	public boolean readValue(String value) {
		if (value.length() != 1)
			return false;
		this.value = value.charAt(0);
		dirty = true;
		return true;
	}

	@Override
	public String getAdditionalComment() {
		return "Default: " + defaultValue;
	}

	@Override
	public String getTypeString() { return TYPE; }
	@Override
	public String getValueString() {
		return value + "";
	}
	@Override
	public String getDefaultValueString() {
		return defaultValue + "";
	}
	@Override
	public boolean isDefault() {
		return value == defaultValue;
	}
	@Override
	public void setToDefault() {
		value = defaultValue;
	}
	@Override
	public boolean isValidValue(String str) {
		return str.length() == 1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IGuiConfigEntry toEntry(ModGuiConfig screen, ModGuiConfigEntries entryList) {
		return new CharEntry(screen, entryList, this);
	}
}
