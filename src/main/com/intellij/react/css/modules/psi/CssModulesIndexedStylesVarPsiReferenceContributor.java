/*
 * Copyright (c) 2016-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.react.css.modules.psi;

import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.css.CssClass;
import com.intellij.psi.css.StylesheetFile;
import com.intellij.util.ProcessingContext;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Adds a PSI references from a indexed string literal on a styles object to its corresponding class name.
 * For example, the 'normal' in styles['normal'] will point to the '.normal {}' CSS class in a require'd stylesheet.
 */
public class CssModulesIndexedStylesVarPsiReferenceContributor extends PsiReferenceContributor {

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(CssModulesUtil.STRING_PATTERN, new PsiReferenceProvider() {
            @NotNull
            @Override
            public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                final JSVariable cssClassNamesVariable = CssModulesUtil.getCssClassNamesVariableDeclaration((JSLiteralExpression) element);
                if (cssClassNamesVariable != null) {
                    final String literalClass = "." + StringUtils.stripStart(StringUtils.stripEnd(element.getText(), "\"'"), "\"'");
                    final Ref<StylesheetFile> referencedStyleSheet = new Ref<>();
                    final CssClass cssClass = CssModulesUtil.getCssClass(cssClassNamesVariable, literalClass, referencedStyleSheet);
                    if (cssClass != null) {
                        return new PsiReference[]{new PsiReferenceBase<PsiElement>(element) {
                            @Nullable
                            @Override
                            public PsiElement resolve() {
                                return cssClass;
                            }

                            @NotNull
                            @Override
                            public Object[] getVariants() {
                                return new Object[0];
                            }
                        }};
                    } else {
                        if (referencedStyleSheet.get() != null) {
                            final TextRange rangeInElement = TextRange.from(1, element.getTextLength() - 2); // minus string quotes
                            return new PsiReference[]{new CssModulesUnknownClassPsiReference(element, rangeInElement, referencedStyleSheet.get())};
                        }
                    }

                }
                return new PsiReference[0];
            }
        });
    }

}
