package kpan.heavy_fallings.asm.core;

import java.util.Map;
import javax.annotation.Nullable;
import kpan.heavy_fallings.ModTagsGenerated;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.MCVersion;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name;

@IFMLLoadingPlugin.TransformerExclusions({ModTagsGenerated.MODGROUP + ".asm.core.", ModTagsGenerated.MODGROUP + ".asm.tf.", ModTagsGenerated.MODGROUP + ".util.ReflectionUtil"})
@Name("AsmPlugin")
@MCVersion("1.12.2")
public class AsmPlugin implements IFMLLoadingPlugin {

    public AsmPlugin() {
        AsmUtil.LOGGER.debug("This is " + (AsmUtil.isDeobfEnvironment() ? "deobf" : "obf") + " environment");
    }

    @Override
    public String[] getASMTransformerClass() { return new String[]{AsmTransformer.class.getName()}; }

    @Override
    public String getModContainerClass() { return null; }

    @Nullable
    @Override
    public String getSetupClass() { return null; }

    @Override
    public void injectData(Map<String, Object> data) { }

    @Override
    public String getAccessTransformerClass() { return AccessTransformerForMixin.class.getName(); }

}
