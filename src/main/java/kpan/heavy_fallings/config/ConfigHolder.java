package kpan.heavy_fallings.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import kpan.heavy_fallings.config.core.ConfigAnnotations.ConfigOrder;
import kpan.heavy_fallings.config.core.ConfigAnnotations.FileComment;
import kpan.heavy_fallings.config.core.ConfigAnnotations.Id;
import kpan.heavy_fallings.config.core.ConfigAnnotations.Side;
import kpan.heavy_fallings.config.core.ConfigSide;
import kpan.heavy_fallings.config.core.ConfigVersionUpdateContext;
import kpan.heavy_fallings.util.IBlockPredicate;

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

        @Id("breakable")
        @FileComment("Blocks that will be broken by a falling block.")
        @ConfigOrder(1)
        public List<IBlockPredicate> breakable = new ArrayList<>();

        @Id("nonBreakable")
        @FileComment("Blocks that won't be broken by a falling block.")
        @ConfigOrder(2)
        public List<IBlockPredicate> nonBreakable = new ArrayList<>(Arrays.asList(IBlockPredicate.parse("bed")));

        @Id("dropsWhenBroken")
        @FileComment("Blocks that drop the items when broken.")
        @ConfigOrder(3)
        public List<IBlockPredicate> dropsWhenBroken = new ArrayList<>();

        @Id("noDropsWhenBroken")
        @FileComment("Blocks that drop no items when broken.")
        @ConfigOrder(4)
        public List<IBlockPredicate> noDropsWhenBroken = new ArrayList<>();

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
