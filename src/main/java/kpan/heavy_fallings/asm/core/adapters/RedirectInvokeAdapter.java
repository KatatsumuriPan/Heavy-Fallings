package kpan.heavy_fallings.asm.core.adapters;

import kpan.heavy_fallings.asm.core.AsmNameRemapper;
import kpan.heavy_fallings.asm.core.AsmUtil;
import kpan.heavy_fallings.asm.core.adapters.Instructions.OpcodeMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.MethodVisitor;

@SuppressWarnings("unused")
public class RedirectInvokeAdapter extends MyMethodVisitor {
    private final OpcodeMethod opcode;
    private final String redirectClass;
    private final String invokedMethodOwner;
    private final String invokedMethodMcpName;
    private final String invokedMethodRuntimeName;
    private final @Nullable String invokedMethodDesc;
    public RedirectInvokeAdapter(@NotNull MethodVisitor mv, String nameForDebug, OpcodeMethod opcode, String redirectClass, String invokedMethodOwner, String invokedMethodMcpName, @Nullable String invokedMethodDesc) {
        super(mv, nameForDebug);
        this.opcode = opcode;
        this.redirectClass = redirectClass;
        this.invokedMethodOwner = invokedMethodOwner.replace('.', '/');
        this.invokedMethodMcpName = invokedMethodMcpName;
        invokedMethodRuntimeName = AsmNameRemapper.mcp2RuntimeMethodName(invokedMethodOwner, invokedMethodMcpName, invokedMethodDesc);
        this.invokedMethodDesc = invokedMethodDesc;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (opcode == this.opcode.opcode
                && owner.equals(invokedMethodOwner)
                && name.equals(invokedMethodRuntimeName)
                && (invokedMethodDesc == null || desc.equals(invokedMethodDesc))
        ) {
            switch (this.opcode) {
                case VIRTUAL, INTERFACE, SPECIAL -> {
                    super.visitMethodInsn(OpcodeMethod.STATIC.opcode, redirectClass, invokedMethodMcpName, AsmUtil.insertToMethodDesc(desc, 0, invokedMethodOwner), false);
                }
                case STATIC -> {
                    super.visitMethodInsn(OpcodeMethod.STATIC.opcode, redirectClass, invokedMethodMcpName, desc, false);
                }
            }
            success();
            return;
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }


    public static RedirectInvokeAdapter virtual(@NotNull MethodVisitor mv, String nameForDebug, String redirectClass, String invokedMethodOwner, String invokedMethodMcpName) {
        return virtual(mv, nameForDebug, redirectClass, invokedMethodOwner, invokedMethodMcpName, null);
    }
    public static RedirectInvokeAdapter virtual(@NotNull MethodVisitor mv, String nameForDebug, String redirectClass, String invokedMethodOwner, String invokedMethodMcpName, @Nullable String invokedMethodDesc) {
        return new RedirectInvokeAdapter(mv, nameForDebug, OpcodeMethod.VIRTUAL, redirectClass, invokedMethodOwner, invokedMethodMcpName, invokedMethodDesc);
    }
    public static RedirectInvokeAdapter static_(@NotNull MethodVisitor mv, String nameForDebug, String redirectClass, String invokedMethodOwner, String invokedMethodMcpName) {
        return static_(mv, nameForDebug, redirectClass, invokedMethodOwner, invokedMethodMcpName, null);
    }
    public static RedirectInvokeAdapter static_(@NotNull MethodVisitor mv, String nameForDebug, String redirectClass, String invokedMethodOwner, String invokedMethodMcpName, @Nullable String invokedMethodDesc) {
        return new RedirectInvokeAdapter(mv, nameForDebug, OpcodeMethod.STATIC, redirectClass, invokedMethodOwner, invokedMethodMcpName, invokedMethodDesc);
    }
    public static RedirectInvokeAdapter interface_(@NotNull MethodVisitor mv, String nameForDebug, String redirectClass, String invokedMethodOwner, String invokedMethodMcpName) {
        return interface_(mv, nameForDebug, redirectClass, invokedMethodOwner, invokedMethodMcpName, null);
    }
    public static RedirectInvokeAdapter interface_(@NotNull MethodVisitor mv, String nameForDebug, String redirectClass, String invokedMethodOwner, String invokedMethodMcpName, @Nullable String invokedMethodDesc) {
        return new RedirectInvokeAdapter(mv, nameForDebug, OpcodeMethod.INTERFACE, redirectClass, invokedMethodOwner, invokedMethodMcpName, invokedMethodDesc);
    }
}
