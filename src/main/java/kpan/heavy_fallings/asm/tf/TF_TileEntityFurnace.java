package kpan.heavy_fallings.asm.tf;

import kpan.heavy_fallings.asm.core.AsmTypes;
import kpan.heavy_fallings.asm.core.AsmUtil;
import kpan.heavy_fallings.asm.core.adapters.Instructions;
import kpan.heavy_fallings.asm.core.adapters.Instructions.OpcodeMethod;
import kpan.heavy_fallings.asm.core.adapters.MixinAccessorAdapter;
import kpan.heavy_fallings.asm.core.adapters.MyClassVisitor;
import kpan.heavy_fallings.asm.core.adapters.ReplaceInstructionsAdapter;
import kpan.heavy_fallings.asm.core.adapters.ReplaceRefMethodAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

public class TF_TileEntityFurnace {

    private static final String TARGET = "net.minecraft.tileentity.TileEntityFurnace";
    private static final String HOOK = AsmTypes.HOOK + "HK_" + "TileEntityFurnace";
    private static final String ACC = AsmTypes.ACC + "ACC_" + "TileEntityFurnace";

    public static ClassVisitor appendVisitor(ClassVisitor cv, String className) {
        if (!TARGET.equals(className))
            return cv;
        ClassVisitor newcv = new MyClassVisitor(cv, className) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                if (name.equals("readFromNBT") || name.equals("update")) {
                    mv = new ReplaceInstructionsAdapter(mv, name,
                            Instructions.create()
                                    .methodRep(OpcodeMethod.STATIC, TARGET, "getItemBurnTime"),
                            Instructions.create()
                                    .invokeStatic(HOOK, "getItemBurnTime", AsmUtil.toMethodDesc(AsmTypes.INT, AsmTypes.ITEMSTACK)));
                    success();
                }
                return mv;
            }
        }.setSuccessExpected(2);
        newcv = new ReplaceRefMethodAdapter(newcv, HOOK, TARGET, "getName", AsmUtil.toMethodDesc(AsmTypes.STRING));
        newcv = new MixinAccessorAdapter(newcv, className, ACC);
        return newcv;
    }
}
