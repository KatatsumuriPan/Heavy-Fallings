package kpan.srg2mcp_name_remapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import kpan.asmupdate.asm.core.AsmUtil;

public class Remapper {
    private static boolean inited = false;

    private static Map<String, String> srgMcpFieldMap = new HashMap<>();
    private static Map<String, String> srgMcpMethodMap = new HashMap<>();

    public static void init() {
        if (!inited) {
            inited = true;
            loadSrg2McpFieldMap();
            loadSrg2McpMethodMap();
            AsmUtil.LOGGER.debug("Srg Rename Mapping Loaded Completely");
        }
    }

    private static void loadSrg2McpFieldMap() {
        InputStream stream = Remapper.class.getResourceAsStream("/nameremapper/srg2mcp/fields.csv");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {//readerがcloseされれば全部closeされる(はず)
            String line;
            while ((line = reader.readLine()) != null) {
                String[] split = line.split(",");
                srgMcpFieldMap.put(split[0], split[1]);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadSrg2McpMethodMap() {
        InputStream stream = Remapper.class.getResourceAsStream("/nameremapper/srg2mcp/methods.csv");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {//readerがcloseされれば全部closeされる(はず)
            String line;
            while ((line = reader.readLine()) != null) {
                String[] split = line.split(",");
                srgMcpMethodMap.put(split[0], split[1]);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String srg2McpFieldName(String srgFieldName) {
        return srgMcpFieldMap.getOrDefault(srgFieldName, srgFieldName);
    }

    public static String srg2McpMethodName(String srgMethodName) {
        return srgMcpMethodMap.getOrDefault(srgMethodName, srgMethodName);
    }

}
