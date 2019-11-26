package com.androidpi.tools.aop.weaver;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.TypePath;

import static org.objectweb.asm.Opcodes.V1_8;

/**
 * Created by jastrelax on 2017/6/9.
 */

public class AopLoggerWriterAdapter extends ClassVisitor {

    public AopLoggerWriterAdapter(int api) {
        super(api);
    }

    public AopLoggerWriterAdapter(int api, ClassVisitor cv) {
        super(api, cv);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {

        System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        System.out.printf("|             %s\n", name);
        System.out.println("----------------------------------------------------------------------");
        super.visit(V1_8, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitSource(String source, String debug) {
        super.visitSource(source, debug);
    }

    @Override
    public void visitOuterClass(String owner, String name, String desc) {
        super.visitOuterClass(owner, name, desc);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
        return super.visitTypeAnnotation(typeRef, typePath, desc, visible);
    }

    @Override
    public void visitAttribute(Attribute attr) {
        super.visitAttribute(attr);
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        super.visitInnerClass(name, outerName, innerName, access);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        System.out.printf("visitMethod: %d| %s| %s| %s| %s\n", access, name, desc, signature, exceptions);
        MethodVisitor mv =  super.visitMethod(access, name, desc, signature, exceptions);
        if ("onClick".equals(name)) {
            return new ClickListenerMethodVisitor(mv);
        }
        return mv;
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
    }
}
