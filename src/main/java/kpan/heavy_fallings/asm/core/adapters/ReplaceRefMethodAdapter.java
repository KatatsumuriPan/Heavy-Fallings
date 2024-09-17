package kpan.heavy_fallings.asm.core.adapters;

import kpan.heavy_fallings.asm.core.AsmTypes.MethodDesc;
import kpan.heavy_fallings.asm.core.AsmUtil;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ReplaceRefMethodAdapter extends ReplaceMethodAdapter {

    private final String classForRefMethodParam;
    private final String mcpMethodName;
    private final String refClass;
    private final String returnType;
    private final String[] params;

    public ReplaceRefMethodAdapter(ClassVisitor cv, String refClass, String classForRefMethodParam, String mcpMethodName, String methodDesc) {
        super(cv, mcpMethodName, methodDesc);
        this.mcpMethodName = mcpMethodName;
        this.classForRefMethodParam = classForRefMethodParam;
        this.refClass = refClass;
        MethodDesc md = MethodDesc.fromMethodDesc(methodDesc);
        returnType = md.returnDesc;
        params = md.paramsDesc;
    }

    @Override
    protected void methodBody(MethodVisitor mv) {
        boolean is_static = (access & Opcodes.ACC_STATIC) != 0;
        int offset = 0;

        //this
        if (!is_static) {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            offset = 1;
        }

        //params
        AsmUtil.loadLocals(mv, params, offset);

        //invoke
        if (is_static)
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, refClass, mcpMethodName, methodDesc, false);
        else
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, refClass, mcpMethodName, AsmUtil.toMethodDesc(returnType, classForRefMethodParam, params), false);

        //return
        mv.visitInsn(AsmUtil.toReturnOpcode(returnType));
    }

}
