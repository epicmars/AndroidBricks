package com.androidpi.tools.aop.weaver;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ASM7;
import static org.objectweb.asm.Opcodes.V1_8;

/** Created by on 2019-10-22. */
public class OnClickListenerAdapter extends ClassVisitor {

    public static final String NAME_ONCLICKLISTENER = "android/view/View$OnClickListener";

    private String owner;
    private String descripter;

    public OnClickListenerAdapter(int api) {
        super(api);
    }

    public OnClickListenerAdapter(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    public OnClickListenerAdapter(ClassVisitor classVisitor) {
        this(ASM7, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(V1_8, access, name, signature, superName, interfaces);
        this.owner = name;
        this.descripter = "L" + owner + ";";
    }

    @Override
    public MethodVisitor visitMethod(
            int access, String name, String desc, String signature, String[] exceptions) {
        System.out.printf("visitMethod: %d| %s| %s| %s| %s\n", access, name, desc, signature, exceptions);
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        return new SetOnClickListenerVisitor(mv, name, owner, descripter);
    }
}

