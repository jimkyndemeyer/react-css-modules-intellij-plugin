/*
 * Copyright (c) 2016-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.react.css.modules.psi;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.css.StylesheetFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents an invalid CSS class reference in a specific style sheet file.
 */
public class CssModulesUnknownClassPsiReference extends PsiReferenceBase<PsiElement> {

    private final StylesheetFile stylesheetFile;

    public CssModulesUnknownClassPsiReference(@NotNull PsiElement element, TextRange rangeInElement, StylesheetFile stylesheetFile) {
        super(element, rangeInElement);
        this.stylesheetFile = stylesheetFile;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        // self reference to prevent JS tooling from reporting unresolved symbol
        return this.getElement();
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return new Object[0];
    }

    public StylesheetFile getStylesheetFile() {
        return stylesheetFile;
    }
}
