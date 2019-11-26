package com.androidpi.tools.aop.transform;

import com.android.build.api.transform.Format;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.androidpi.tools.aop.plugin.AopExtension;
import com.androidpi.tools.aop.weaver.AopLogWeaver;

import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by jastrelax on 2017/5/24.
 */
public class AopTransform extends Transform {

    private static final String CONTENT_LOCATION_NAME = "aop";

    private AopExtension aopExtension;

    public AopTransform(Project project) {
        aopExtension = project.getExtensions().getByType(AopExtension.class);
    }

    @Override
    public String getName() {
        return "aop";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return AopExtension.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return aopExtension.getScopes();
    }

//    @Override
//    public Set<? super QualifiedContent.Scope> getReferencedScopes() {
//        return aopExtension.getReferencedScopes();
//    }

    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation);

        System.out.println("--------------------------------------");
        System.out.println("---------- transform  start-----------");
        System.out.println("--------------------------------------");

        transformInvocation.getOutputProvider().deleteAll();

        File outputRoot = transformInvocation
                .getOutputProvider()
                .getContentLocation(CONTENT_LOCATION_NAME, AopExtension.CONTENT_CLASS, aopExtension.getScopes(), Format.DIRECTORY);

        if (null == transformInvocation.getInputs()) {
            return;
        }
        for (TransformInput input : transformInvocation.getInputs()) {
            if (input.getJarInputs() != null) {
                input.getJarInputs().forEach(jarInput -> {
                    System.out.println("jarInput: " + jarInput.getFile().getPath());
                    System.out.println("jarOutput: " + transformInvocation
                            .getOutputProvider()
                            .getContentLocation(CONTENT_LOCATION_NAME, AopExtension.CONTENT_CLASS, aopExtension.getScopes(), Format.JAR).getPath());

                    try (JarFile jarFile = new JarFile(jarInput.getFile())) {
                        Enumeration<JarEntry> entries = jarFile.entries();
                        while (entries.hasMoreElements()) {
                            System.out.println("entry: " + entries.nextElement().getName());
                        }
                    } catch (IOException e) {

                    }
                });
            }

            if (input.getDirectoryInputs() != null) {
                input.getDirectoryInputs().forEach(directoryInput -> {
                    File inputRoot = directoryInput.getFile();
                    System.out.println("directory: " + inputRoot.getPath());
                    System.out.println("out: " + outputRoot.getPath());
                    AopLogWeaver.traverseDir(inputRoot, inputRoot, outputRoot);
                });
            }
        }

        System.out.println("--------------------------------------");
        System.out.println("---------- transform end--------------");
        System.out.println("--------------------------------------");
    }
}