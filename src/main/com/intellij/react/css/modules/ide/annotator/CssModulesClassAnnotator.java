/*
 * Copyright (c) 2016-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.react.css.modules.ide.annotator;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.react.css.modules.psi.CssModulesUnknownClassPsiReference;
import com.intellij.react.css.modules.psi.CssModulesUtil;
import org.jetbrains.annotations.NotNull;


/**
 * Adds error markers to unknown class names.
 *
 * @see CssModulesUnknownClassPsiReference
 */
public class CssModulesClassAnnotator implements Annotator {

    @Override
    public void annotate(@NotNull PsiElement psiElement, @NotNull AnnotationHolder annotationHolder) {
        PsiElement elementToAnnotate = null;
        if (psiElement instanceof XmlAttributeValue) {
            if (CssModulesUtil.STYLE_NAME_FILTER.isAcceptable(psiElement, psiElement)) {
                elementToAnnotate = psiElement;
            }
        } else if (psiElement instanceof JSLiteralExpression) {
            elementToAnnotate = psiElement;
        }
        if (elementToAnnotate != null) {
            for (PsiReference psiReference : psiElement.getReferences()) {
                if (psiReference instanceof CssModulesUnknownClassPsiReference) {
                    final TextRange rangeInElement = psiReference.getRangeInElement();
                    if (rangeInElement.isEmpty()) {
                        continue;
                    }
                    int start = psiElement.getTextRange().getStartOffset() + rangeInElement.getStartOffset();
                    int length = rangeInElement.getLength();
                    final TextRange textRange = TextRange.from(start, length);
                    if (!textRange.isEmpty()) {
                        final String message = "Unknown class name \"" + rangeInElement.substring(psiElement.getText()) + "\"";
                        annotationHolder.createErrorAnnotation(textRange, message);
                    }
                }
            }
        }
    }
}
