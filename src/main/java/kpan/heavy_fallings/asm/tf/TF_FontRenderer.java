package kpan.heavy_fallings.asm.tf;

import kpan.heavy_fallings.asm.core.AsmTypes;
import kpan.heavy_fallings.asm.core.AsmUtil;
import kpan.heavy_fallings.asm.core.adapters.InjectInstructionsAdapter;
import kpan.heavy_fallings.asm.core.adapters.Instructions;
import kpan.heavy_fallings.asm.core.adapters.MixinAccessorAdapter;
import kpan.heavy_fallings.asm.core.adapters.MyClassVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class TF_FontRenderer {

    private static final String TARGET = "net.minecraft.client.gui.FontRenderer";
    private static final String HOOK = AsmTypes.HOOK + "HK_" + "FontRenderer";
    private static final String ACC = AsmTypes.ACC + "ACC_" + "FontRenderer";

    public static ClassVisitor appendVisitor(ClassVisitor cv, String className) {
        if (!TARGET.equals(className))
            return cv;
        ClassVisitor newcv = new MyClassVisitor(cv, className) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                if (name.equals("renderChar")) {
                    mv = InjectInstructionsAdapter.injectFirst(mv, name,
                            Instructions.create()
                                    .aload(0)
                                    .iload(1)
                                    .invokeStatic(HOOK, "onRenderChar", AsmUtil.toMethodDesc(AsmTypes.CHAR, TARGET, AsmTypes.CHAR))
                                    .istore(1)
                    );
                    success();
                }
                return mv;
            }
        };
        newcv = new MixinAccessorAdapter(newcv, className, ACC);
        return newcv;
    }
}
