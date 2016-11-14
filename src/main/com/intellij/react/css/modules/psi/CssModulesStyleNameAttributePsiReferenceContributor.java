/*
 * Copyright (c) 2016-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.react.css.modules.psi;

import com.google.common.collect.Lists;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.css.CssClass;
import com.intellij.psi.css.StylesheetFile;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Adds a PSI references from class names used a styleName React attribute to their corresponding imported CSS classes.
 */
public class CssModulesStyleNameAttributePsiReferenceContributor extends PsiReferenceContributor {


    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(CssModulesUtil.STYLE_NAME_PATTERN, new PsiReferenceProvider() {
            @NotNull
            @Override
            public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
                final StylesheetFile styleSheetFile = CssModulesUtil.getImportedStyleSheetFile(element);
                if (styleSheetFile != null) {
                    final XmlAttributeValue xmlAttributeValue = (XmlAttributeValue) element;
                    if (xmlAttributeValue.getValue().startsWith("{")) {
                        // attribute value is a jsx expression and not a literal class name
                        return PsiReference.EMPTY_ARRAY;
                    }
                    final String[] cssClassNames = xmlAttributeValue.getValue().split(" ");
                    final List<PsiReference> referenceList = Lists.newArrayListWithExpectedSize(1);
                    int offset = xmlAttributeValue.getValueTextRange().getStartOffset() - xmlAttributeValue.getTextRange().getStartOffset();
                    for (String cssClassName : cssClassNames) {
                        final Ref<CssClass> cssClassRef = new Ref<>(null);
                        cssClassRef.set(CssModulesUtil.getCssClass(styleSheetFile, "." + cssClassName));
                        final TextRange rangeInElement = TextRange.from(offset, cssClassName.length());
                        if (cssClassRef.get() != null) {
                            referenceList.add(new PsiReferenceBase<PsiElement>(element, rangeInElement) {
                                @Nullable
                                @Override
                                public PsiElement resolve() {
                                    return cssClassRef.get();
                                }

                                @NotNull
                                @Override
                                public Object[] getVariants() {
                                    return new Object[0];
                                }
                            });
                        } else {
                            referenceList.add(new CssModulesUnknownClassPsiReference(element, rangeInElement, styleSheetFile));
                        }
                        offset += cssClassName.length() + 1;
                    }
                    return referenceList.toArray(new PsiReference[referenceList.size()]);
                }

                return PsiReference.EMPTY_ARRAY;
            }
        });
    }

}
