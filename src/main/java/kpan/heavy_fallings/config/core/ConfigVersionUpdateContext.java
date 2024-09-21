package kpan.heavy_fallings.config.core;

public class ConfigVersionUpdateContext {
    public final ModConfigurationFile modConfigurationFile;
    public final String loadedConfigVersion;
    public String categoryPath;
    public String type;
    public String name;
    public String value;
    public boolean cancelled = false;
    public ConfigVersionUpdateContext(ModConfigurationFile modConfigurationFile, String categoryPath, String type, String name, String value) {
        this.modConfigurationFile = modConfigurationFile;
        loadedConfigVersion = modConfigurationFile.getLoadedConfigVersion();
        this.categoryPath = categoryPath;
        this.type = type;
        this.name = name;
        this.value = value;
    }
}
