package kpan.heavy_fallings.asm.tf;

import kpan.heavy_fallings.asm.core.AsmNameRemapper;
import kpan.heavy_fallings.asm.core.AsmTypes;
import kpan.heavy_fallings.asm.core.adapters.MyClassVisitor;
import kpan.heavy_fallings.asm.core.adapters.RedirectInvokeAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class TF_EntityFallingBlock {

    private static final String TARGET = "net.minecraft.entity.item.EntityFallingBlock";
    private static final String HOOK = AsmTypes.HOOK + "HK_" + "EntityFallingBlock";

    public static ClassVisitor appendVisitor(ClassVisitor cv, String className) {
        if (!TARGET.equals(className))
            return cv;
        ClassVisitor newcv = new MyClassVisitor(cv, className) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                String mcpName = AsmNameRemapper.runtime2McpMethodName(name);
                if (mcpName.equals("onUpdate")) {
                    mv = RedirectInvokeAdapter.virtual(mv, mcpName, HOOK, AsmTypes.WORLD, "mayPlace");
                    success();
                }
                return mv;
            }
        };
        return newcv;
    }
}
