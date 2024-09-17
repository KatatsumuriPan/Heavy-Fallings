package kpan.heavy_fallings.config;

import kpan.heavy_fallings.config.core.ConfigAnnotations.ConfigOrder;
import kpan.heavy_fallings.config.core.ConfigAnnotations.FileComment;
import kpan.heavy_fallings.config.core.ConfigAnnotations.Id;
import kpan.heavy_fallings.config.core.ConfigAnnotations.Side;
import kpan.heavy_fallings.config.core.ConfigSide;
import kpan.heavy_fallings.config.core.ConfigVersionUpdateContext;

public class ConfigHolder {

	@Id("Client")
	@FileComment("Client Settings(Rendering, resources, etc.)")
	@ConfigOrder(1)
	@Side(ConfigSide.CLIENT)
	public static Client client = new Client();

	public static class Client {

		@Id("LineBreakAlgorithm")
		@FileComment("The algorithm used for line breaks")
		@ConfigOrder(1)
		public Algorithm lineBreakAlgorithm = Algorithm.NON_ASCII;

		public enum Algorithm {
			VANILLA,
			NON_ASCII,
			PHRASE,
		}
	}

	//	@Id("Common")
	//	@FileComment("Common settings(Blocks, items, etc.)")
	//	@ConfigOrder(2)
	//	@Side(ConfigSide.COMMON)
	//	public static Common common = new Common();

	public static class Common {

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
