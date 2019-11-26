package com.androidpi.tools.aop.plugin;

import com.android.build.gradle.AppExtension;
import com.android.build.gradle.BaseExtension;
import com.androidpi.tools.aop.transform.AopTransform;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class AopPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        applyPlugin(project, AppExtension.class);
//        applyPlugin(project, LibraryExtension.class);
    }

    public <T extends BaseExtension> void applyPlugin(Project project, Class<T> extensionClass) {
        try {
            project.getExtensions().create(AopExtension.EXTENSION_AOP, AopExtension.class);
            T extension = project.getExtensions().getByType(extensionClass);
            extension.registerTransform(new AopTransform(project));
        } catch (Exception e) {
            // ignore
            e.printStackTrace();
        }
    }
}