package kpan.heavy_fallings.config.core;

import java.io.File;
import java.lang.reflect.Field;
import java.util.function.Consumer;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ConfigHandler {
    public final Class<?> holderClass;
    public final String name;
    public final String version;
    public final Consumer<ConfigVersionUpdateContext> updater;
    public ModConfigurationFile config;

    public ConfigHandler(Class<?> holderClass, String name, String version, Consumer<ConfigVersionUpdateContext> updater) {
        this.holderClass = holderClass;
        this.name = name;
        this.version = version;
        this.updater = updater;
    }

    public void preInit(FMLPreInitializationEvent event) {
        config = new ModConfigurationFile(new File(event.getModConfigurationDirectory() + "/" + name + ".cfg"), version);
        createProperties();
        config.load(updater);
        storeToFieldAndSave();
    }

    public ModConfigCategory getRootCategory() {
        return config.getRootCategory();
    }

    public void storeToFieldAndSave() {
        try {
            config.storeToField();
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        save();
    }

    public void loadFromFieldAndSave() {
        try {
            config.loadFromField();
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        save();
    }

    private void save() {
        config.save();
    }

    private void createProperties() {
        try {
            for (Field field : holderClass.getFields()) {
                config.create(field, null);
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
