package com.androidpi.tools.aop.transform;

import com.androidpi.tools.aop.weaver.OnClickListenerAdapter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.BiConsumer;

import static org.objectweb.asm.ClassReader.SKIP_DEBUG;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ASM7;

//@AutoService(BiConsumer.class)
public class ClassConsumer implements BiConsumer<InputStream, OutputStream> {

    @Override
    public void accept(InputStream inputStream, OutputStream outputStream) {
        System.out.println("ClassConsumer");
        try (InputStream in = new BufferedInputStream(inputStream);
             OutputStream out = new BufferedOutputStream(outputStream)) {
            ClassReader reader = new ClassReader(in);
            ClassWriter writer = new ClassWriter(reader, COMPUTE_FRAMES);
            ClassVisitor adapter = new OnClickListenerAdapter(ASM7, writer);
            reader.accept(adapter, SKIP_DEBUG);
            out.write(writer.toByteArray());
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
