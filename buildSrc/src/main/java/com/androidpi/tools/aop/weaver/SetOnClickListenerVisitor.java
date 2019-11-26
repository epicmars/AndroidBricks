package com.androidpi.tools.aop.weaver;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/** Created by jastrelax on 2017/6/9. */
public class SetOnClickListenerVisitor extends MethodVisitor implements Opcodes {

    private String name;
    private String owner;
    private String descripter;
    private int extraStack, extraLocals;

    public SetOnClickListenerVisitor(int api) {
        super(api);
    }

    public SetOnClickListenerVisitor(int api, MethodVisitor methodVisitor) {
        super(api, methodVisitor);
    }

    public SetOnClickListenerVisitor(
            MethodVisitor mv, String name, String owner, String descripter) {
        this(ASM7, mv);
        this.name = name;
        this.owner = owner;
        this.descripter = descripter;
    }

    @Override
    public void visitCode() {
        if ("onClick".equals(name)) {
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKESTATIC,
                    "com.androidpi.app.bricks.logger.Logger".replace('.', '/'),
                    "log", "(Landroid/view/View;)V", false);
        }
        super.visitCode();
    }

    @Override
    public void visitMethodInsn(
            int opcode, String owner, String name, String descriptor, boolean isInterface) {
        if (name.equals("setOnClickListener") && descriptor.equals("(Landroid/view/View$OnClickListener;)V")) {
            mv.visitMethodInsn(
                    INVOKESTATIC,
                    "com.androidpi.app.bricks.utils.AntiDoubleClickHelper".replace('.', '/'),
                    "antiDoubleClickListener",
                    "(Landroid/view/View$OnClickListener;)Lcom/androidpi/app/bricks/utils/AntiDoubleClickListener;",
                    false);
        }
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }
}
