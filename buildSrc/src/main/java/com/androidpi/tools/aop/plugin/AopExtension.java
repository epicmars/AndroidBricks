package com.androidpi.tools.aop.plugin;

import com.android.build.api.transform.QualifiedContent;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.Set;

import static com.android.build.api.transform.QualifiedContent.DefaultContentType.CLASSES;
import static com.android.build.api.transform.QualifiedContent.Scope.EXTERNAL_LIBRARIES;
import static com.android.build.api.transform.QualifiedContent.Scope.PROJECT;
import static com.android.build.api.transform.QualifiedContent.Scope.SUB_PROJECTS;

/** Created by jastrelax on 2018/7/31. */
public class AopExtension {

    public static final String EXTENSION_AOP = "aop";
    private final String SCOPE_FULL = "full";
    private final String SCOPE_PROJECT = "project";

    public static final Set<QualifiedContent.ContentType> CONTENT_CLASS = ImmutableSet.of(CLASSES);

    public static final Set<QualifiedContent.Scope> SCOPES_PROJECT = ImmutableSet.of(PROJECT);

    public static final Set<QualifiedContent.Scope> SCOPES_FULL =
            Sets.immutableEnumSet(PROJECT, SUB_PROJECTS, EXTERNAL_LIBRARIES);

    public static final Set<QualifiedContent.Scope> REFERENCED_SCOPES =
            ImmutableSet.of(SUB_PROJECTS, EXTERNAL_LIBRARIES);

    private String scope = SCOPE_PROJECT;
    private Set<QualifiedContent.Scope> scopes = SCOPES_FULL;
    private Set<QualifiedContent.Scope> referencedScopes = REFERENCED_SCOPES;

    public void setScope(String scope) {
        this.scope = scope;
        switch (scope) {
            case SCOPE_FULL:
                scopes = SCOPES_FULL;
                break;
            case SCOPE_PROJECT:
            default:
                scopes = SCOPES_PROJECT;
                break;
        }
    }

    public Set<? super QualifiedContent.Scope> getScopes() {
        return scopes;
    }

    public void setScopes(Set<QualifiedContent.Scope> scopes) {
        this.scopes = scopes;
    }

    public Set<? super QualifiedContent.Scope> getReferencedScopes() {
        return referencedScopes;
    }

    public void setReferencedScopes(Set<QualifiedContent.Scope> referencedScopes) {
        this.referencedScopes = referencedScopes;
    }
}
