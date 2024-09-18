package kpan.heavy_fallings.config;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import kpan.heavy_fallings.config.core.ConfigAnnotations.ConfigOrder;
import kpan.heavy_fallings.config.core.ConfigAnnotations.FileComment;
import kpan.heavy_fallings.config.core.ConfigAnnotations.Id;
import kpan.heavy_fallings.config.core.ConfigAnnotations.Side;
import kpan.heavy_fallings.config.core.ConfigSide;
import kpan.heavy_fallings.config.core.ConfigVersionUpdateContext;
import net.minecraft.block.state.IBlockState;

public class ConfigHolder {

    @Id("Client")
    @FileComment("Client Settings(Rendering, resources, etc.)")
    @ConfigOrder(1)
    @Side(ConfigSide.CLIENT)
    public static Client client = new Client();

    public static class Client {

    }

    @Id("Common")
    @FileComment("Common settings(Blocks, items, etc.)")
    @ConfigOrder(2)
    @Side(ConfigSide.COMMON)
    public static Common common = new Common();

    public static class Common {

        public List<Predicate<IBlockState>> breaks = new ArrayList<>();
        public List<Predicate<IBlockState>> nonBreaks = new ArrayList<>();

        public List<Predicate<IBlockState>> dropOnBroken = new ArrayList<>();
        public List<Predicate<IBlockState>> notDropOnBroken = new ArrayList<>();

    }

    //	@Id("Server")
    //	@FileComment("Server settings(Behaviors, physics, etc.)")
    //	@ConfigOrder(3)
    //	@Side(ConfigSide.SERVER)
    //	public static Server server = new Server();

    public static class Server {

    }

    public static void updateVersion(ConfigVersionUpdateContext context) {
        switch (context.loadedConfigVersion) {
            case "1":
                break;
            default:
                throw new RuntimeException("Unknown config version:" + context.loadedConfigVersion);
        }
    }

    public static String getVersion() { return "1"; }
}
