package com.androidpi.tools.aop.weaver;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import static org.objectweb.asm.Opcodes.ASM6;

/**
 * Created by jastrelax on 2017/6/9.
 */

public class AopLogWeaver {

    public static void traverseDir(File inputRoot, File dir, File outputRoot) {
        File[] files = dir.listFiles();
        if (null == files || files.length == 0)
            return;
        for (File f : files) {
            if (f.isDirectory()) {
                traverseDir(inputRoot, f, outputRoot);
            } else if (f.getName().endsWith(".class")) {
                visitClass(inputRoot, f, outputRoot);
            }
        }
    }

    public static void visitClass(File inputRoot, File classFile, File outputRoot) {
            String relativePath = classFile.getPath().replaceFirst(inputRoot.getPath(), "");
            transform(inputRoot, relativePath, outputRoot);
    }

    public static void transform(File inputDirRoot, String relativePath, File outputDirRoot) {
        try {
            File inputFile = new File(inputDirRoot, relativePath);
            File outputFile = new File(outputDirRoot, relativePath);
            System.out.println("------------------------------------------------------");
            System.out.printf("input: %s\n", inputFile.getPath());
            System.out.printf("output: %s\n", outputFile.getPath());

            InputStream classInput = new BufferedInputStream(new FileInputStream(inputFile));

            ClassReader reader = new ClassReader(classInput);

            ClassWriter writer = new ClassWriter(reader, 0);

            ClassVisitor adapter = new OnClickListenerAdapter(ASM6, writer);

            reader.accept(adapter, 0);


            File outputDir = outputFile.getParentFile();
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));
            out.write(writer.toByteArray());
            out.flush();
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }


    public static void traverseJar(File file) {
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(file);
            Enumeration<JarEntry> entries = jarFile.entries();
            //
            FileOutputStream jarFileOut = new FileOutputStream(new File("buildSrc/build/weaver/test.jar"));
            JarOutputStream jarOutputStream = new JarOutputStream(new BufferedOutputStream(jarFileOut));

            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                System.out.println(jarEntry.getName());
                jarOutputStream.putNextEntry(jarEntry);

                if (jarEntry.getName().endsWith(".class")) {
                    visitClassInJar(jarFile);

                    // 重新打文件
                    File generated = new File("buildSrc/build/classes/generated");
                    File generatedClass = new File(generated, jarEntry.getName());
                    InputStream in = new BufferedInputStream(new FileInputStream(generatedClass));
                    byte[] buf = new byte[1024];
                    int count;
                    while ((count = in.read(buf)) > 0) {
                        jarOutputStream.write(buf, 0, count);
                    }
                }
            }

            // 关闭流
            jarOutputStream.flush();
            jarOutputStream.close();
        } catch (IOException e) {
            try {
                if (null != jarFile) {
                    jarFile.close();
                }
            } catch (IOException e1) {

            }
        }
    }

    private static void visitClassInJar(JarFile entry) {

    }
}
