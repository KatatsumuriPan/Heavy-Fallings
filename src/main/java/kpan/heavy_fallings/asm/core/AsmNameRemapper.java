package kpan.heavy_fallings.asm.core;

import kpan.heavy_fallings.Remapper;

public class AsmNameRemapper {

    private static boolean inited = false;

    public static void init() {
        if (!inited) {
            inited = true;
            Remapper.init();
        }
    }

    public static String srg2McpFieldName(String srgFieldName) {
        return Remapper.srg2McpFieldName(srgFieldName);
    }

    public static String srg2McpMethodName(String srgMethodName) {
        return Remapper.srg2McpMethodName(srgMethodName);
    }

}
