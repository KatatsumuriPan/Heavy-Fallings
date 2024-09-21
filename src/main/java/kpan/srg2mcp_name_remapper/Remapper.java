package kpan.srg2mcp_name_remapper;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.annotation.Nullable;
import kpan.heavy_fallings.util.ReflectionUtil;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraftforge.fml.common.patcher.ClassPatchManager;
import org.apache.logging.log4j.LogManager;
import org.objectweb.asm.ClassReader;

public class Remapper {
    private static boolean inited = false;

    private static LaunchClassLoader classLoader;

    private static Map<String, String> srgMcpFieldMap = new HashMap<>();
    private static Map<String, String> srgMcpMethodMap = new HashMap<>();

    // ownerはnet/minecraft/を省略したい！（いずれ）
    private static Map<String, Map<String, String>> fieldMcpSrgMap = Maps.newHashMap(); // owner -> (mcp -> srg)
    private static Map<String, Map<String, String>> methodMcpSrgMap = Maps.newHashMap(); // owner -> (mcp+desc -> srg)
    private static Map<String, Map<String, String>> rawFieldMcpSrgMap = Maps.newHashMap(); // owner -> (mcp -> srg)
    private static Map<String, Map<String, String>> rawMethodMcpSrgMap = Maps.newHashMap(); // owner -> (mcp+desc -> srg)
    private static Set<String> loadedClassNames = Sets.newHashSet();


    public static void init() {
        if (!inited) {
            inited = true;
            try {
                classLoader = ReflectionUtil.getPrivateField(FMLDeobfuscatingRemapper.INSTANCE, "classLoader");
                loadMcpMap();
            } catch (IOException e) {
                System.out.println("An error occurred loading the srg map data");
                System.out.println(e);
            }
        }
    }
    private static void loadMcpMap() throws IOException {
        InputStream stream = Remapper.class.getResourceAsStream("/nameremapper/srg2mcp.txt2");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {// readerがcloseされれば全部closeされる(はず)
            String owner = "";
            boolean uniqueMethod = false;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals("*")) {
                    uniqueMethod = true;
                    continue;
                }
                String[] split = line.split(" ");
                if (split.length == 1) {
                    // class
                    owner = "net/minecraft/" + line;
                    uniqueMethod = false;
                } else if (split.length == 2) {
                    // field or unique method
                    if (uniqueMethod) {
                        String srg = "func_" + split[0];
                        String mcp = split[1];
                        if (!rawMethodMcpSrgMap.containsKey(owner))
                            rawMethodMcpSrgMap.put(owner, new TreeMap<>());
                        rawMethodMcpSrgMap.get(owner).put(mcp, srg);
                        srgMcpMethodMap.put(srg, mcp);
                    } else {
                        String srg = "field_" + split[0];
                        String mcp = split[1];
                        if (!rawFieldMcpSrgMap.containsKey(owner))
                            rawFieldMcpSrgMap.put(owner, new TreeMap<>());
                        rawFieldMcpSrgMap.get(owner).put(mcp, srg);
                        srgMcpFieldMap.put(srg, mcp);
                    }
                } else {
                    String srg = "func_" + split[0];
                    String mcp = split[1];
                    String methodDesc = split[2];
                    if (!rawMethodMcpSrgMap.containsKey(owner))
                        rawMethodMcpSrgMap.put(owner, new TreeMap<>());
                    rawMethodMcpSrgMap.get(owner).put(mcp + methodDesc, srg);
                    srgMcpMethodMap.put(srg, mcp);
                }
            }
        }
    }

    public static String srg2McpFieldName(String srgFieldName) {
        return srgMcpFieldMap.getOrDefault(srgFieldName, srgFieldName);
    }

    public static String srg2McpMethodName(String srgMethodName) {
        return srgMcpMethodMap.getOrDefault(srgMethodName, srgMethodName);
    }

    public static String mcp2SrgFieldName(String owner, String mcpName) {
        Map<String, String> map = getFieldMcp2SrgMap(owner);
        if (map == null)
            return mcpName;
        String srgName = map.get(mcpName);
        if (srgName != null)
            return srgName;
        return mcpName;
    }

    public static String mcp2SrgMethodName(String owner, String mcpName, @Nullable String methodDesc) {
        Map<String, String> map = getMethodMcp2SrgMap(owner);
        if (map == null)
            return mcpName;
        String srgName = map.get(mcpName);
        if (srgName != null)
            return srgName;
        if (methodDesc != null) {
            srgName = map.get(mcpName + methodDesc);
            if (srgName != null)
                return srgName;
        }
        return mcpName;
    }

    private static @Nullable Map<String, String> getMethodMcp2SrgMap(String owner) {
        if (!loadedClassNames.contains(owner)) {
            findAndMergeSuperMaps(owner);
        }
        return methodMcpSrgMap.get(owner);
    }
    private static @Nullable Map<String, String> getFieldMcp2SrgMap(String owner) {
        if (!loadedClassNames.contains(owner)) {
            findAndMergeSuperMaps(owner);
        }
        return fieldMcpSrgMap.get(owner);
    }
    private static void findAndMergeSuperMaps(String className) {
        try {
            String superName = null;
            String[] interfaces = new String[0];
            byte[] classBytes = ClassPatchManager.INSTANCE.getPatchedResource(FMLDeobfuscatingRemapper.INSTANCE.unmap(className), className, classLoader);
            if (classBytes != null) {
                ClassReader cr = new ClassReader(classBytes);
                superName = cr.getSuperName();
                interfaces = cr.getInterfaces();
            }
            mergeSuperMaps(className, superName, interfaces);
            loadedClassNames.add(className);
        } catch (IOException e) {
            FMLLog.log.error("Error getting patched resource:", e);// for java8
        }
    }
    private static void mergeSuperMaps(String className, @Nullable String superName, String[] interfaces) {
        if (Strings.isNullOrEmpty(superName))
            return;

        List<String> allParents = ImmutableList.<String>builder().add(superName).addAll(Arrays.asList(interfaces)).build();
        // generate maps for all parent objects
        for (String parentThing : allParents) {
            if (!fieldMcpSrgMap.containsKey(parentThing)) {
                findAndMergeSuperMaps(parentThing);
            }
        }
        Map<String, String> method_map = new HashMap<>();
        Map<String, String> field_map = new HashMap<>();
        for (String parentThing : allParents) {

            if (methodMcpSrgMap.containsKey(parentThing)) {
                method_map.putAll(methodMcpSrgMap.get(parentThing));
            }
            if (fieldMcpSrgMap.containsKey(parentThing)) {
                field_map.putAll(fieldMcpSrgMap.get(parentThing));
            }
        }

        if (rawMethodMcpSrgMap.containsKey(className)) {
            method_map.putAll(rawMethodMcpSrgMap.get(className));
        }
        if (rawFieldMcpSrgMap.containsKey(className)) {
            field_map.putAll(rawFieldMcpSrgMap.get(className));
        }
        methodMcpSrgMap.put(className, ImmutableMap.copyOf(method_map));
        fieldMcpSrgMap.put(className, ImmutableMap.copyOf(field_map));

        LogManager.getLogger().debug("map : " + className + "  count : " + field_map.size() + "," + method_map.size());
    }

}
