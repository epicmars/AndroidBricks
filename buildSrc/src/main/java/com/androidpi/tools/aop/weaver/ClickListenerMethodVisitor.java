package com.androidpi.tools.aop.weaver;

import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASM6;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

/**
 * Created by jastrelax on 2017/6/9.
 */

public class ClickListenerMethodVisitor extends MethodVisitor {

    public ClickListenerMethodVisitor(int api) {
        super(api);
    }

    public ClickListenerMethodVisitor(int api, MethodVisitor mv) {
        super(api, mv);
    }

    public ClickListenerMethodVisitor(MethodVisitor mv) {
        this(ASM6, mv);
    }

    @Override
    public void visitCode() {
        super.visitCode();
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKESTATIC,
                "com.androidpi.app.bricks.logger.Logger".replace('.', '/'),
                "log", "(Landroid/view/View;)V", false);
    }

    @Override
    public void visitInsn(int opcode) {
        super.visitInsn(opcode);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(maxStack + 1, maxLocals);
    }
}
