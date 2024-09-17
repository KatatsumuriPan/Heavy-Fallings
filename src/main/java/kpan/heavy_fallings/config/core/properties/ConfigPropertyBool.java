package kpan.heavy_fallings.config.core.properties;

import kpan.heavy_fallings.config.core.ConfigSide;
import kpan.heavy_fallings.config.core.gui.ModGuiConfig;
import kpan.heavy_fallings.config.core.gui.ModGuiConfigEntries;
import kpan.heavy_fallings.config.core.gui.ModGuiConfigEntries.BooleanEntry;
import kpan.heavy_fallings.config.core.gui.ModGuiConfigEntries.IGuiConfigEntry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ConfigPropertyBool extends AbstractConfigProperty {

	public static final String TYPE = "B";

	private final boolean defaultValue;
	private boolean value;

	public ConfigPropertyBool(String id, boolean defaultValue, String commentForFile, int order, ConfigSide side) {
		super(id, commentForFile, order, side);
		this.defaultValue = defaultValue;
		value = defaultValue;
	}

	public boolean getValue() {
		return value;
	}

	public void setValue(boolean value) {
		this.value = value;
		dirty = true;
	}

	public boolean getDefaultValue() {
		return defaultValue;
	}

	@Override
	public boolean readValue(String value) {
		if ("true".equalsIgnoreCase(value)) {
			setValue(true);
			return true;
		} else if ("false".equalsIgnoreCase(value)) {
			setValue(false);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public String getAdditionalComment() {
		return "Default: " + defaultValue;
	}

	@Override
	public String getTypeString() {
		return TYPE;
	}

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
		return "true".equalsIgnoreCase(str) || "false".equalsIgnoreCase(str);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IGuiConfigEntry toEntry(ModGuiConfig screen, ModGuiConfigEntries entryList) {
		return new BooleanEntry(screen, entryList, this);
	}
}
