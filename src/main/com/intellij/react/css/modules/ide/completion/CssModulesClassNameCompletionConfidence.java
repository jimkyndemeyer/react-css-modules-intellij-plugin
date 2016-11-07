/*
 * Copyright (c) 2016-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.react.css.modules.ide.completion;


import com.intellij.codeInsight.completion.CompletionConfidence;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.css.StylesheetFile;
import com.intellij.react.css.modules.psi.CssModulesUtil;
import com.intellij.util.ThreeState;
import org.jetbrains.annotations.NotNull;

/**
 * Enables automatic completion inside styles['completion here'] string literals (which is disabled by default in JS)
 */
public class CssModulesClassNameCompletionConfidence extends CompletionConfidence {

    @NotNull
    @Override
    public ThreeState shouldSkipAutopopup(@NotNull PsiElement contextElement, @NotNull PsiFile psiFile, int offset) {
        if (contextElement.getParent() instanceof JSLiteralExpression) {
            final JSVariable cssClassNamesVariable = CssModulesUtil.getCssClassNamesVariableDeclaration((JSLiteralExpression) contextElement.getParent());
            if (cssClassNamesVariable != null) {
                final StylesheetFile stylesheetFile = CssModulesUtil.resolveStyleSheetFile(cssClassNamesVariable);
                if (stylesheetFile != null) {
                    return ThreeState.NO;
                }
            }
        }
        return ThreeState.UNSURE;
    }
}
