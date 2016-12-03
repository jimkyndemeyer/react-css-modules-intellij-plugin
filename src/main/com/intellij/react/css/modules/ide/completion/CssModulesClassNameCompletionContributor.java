/*
 * Copyright (c) 2016-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.react.css.modules.ide.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.CssClass;
import com.intellij.psi.css.StylesheetFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.react.css.modules.psi.CssModulesUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;


/**
 * Completion on available class names from a require'd CSS file.
 */
public class CssModulesClassNameCompletionContributor extends CompletionContributor {

    public CssModulesClassNameCompletionContributor() {

        CompletionProvider<CompletionParameters> provider = new CompletionProvider<CompletionParameters>() {
            @Override
            protected void addCompletions(@NotNull final CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {

                final PsiElement completionElement = Optional.ofNullable(parameters.getOriginalPosition()).orElse(parameters.getPosition());

                if (completionElement.getParent() instanceof XmlAttributeValue) {
                    // Completion for React styleName attribute
                    if (CssModulesUtil.STYLE_NAME_FILTER.isAcceptable(completionElement.getParent(), completionElement)) {
                        final StylesheetFile importedStyleSheetFile = CssModulesUtil.getImportedStyleSheetFile(completionElement);
                        if (importedStyleSheetFile != null) {
                            addCompletions(result, importedStyleSheetFile);
                        }
                    }
                } else if (completionElement.getParent() instanceof JSLiteralExpression) {
                    // Completion for styles['my-class-name']
                    final JSLiteralExpression literalExpression = (JSLiteralExpression) completionElement.getParent();
                    final PsiElement cssClassNamesImportOrRequire = CssModulesUtil.getCssClassNamesImportOrRequireDeclaration(literalExpression);
                    if (cssClassNamesImportOrRequire != null) {
                        final StylesheetFile stylesheetFile = CssModulesUtil.resolveStyleSheetFile(cssClassNamesImportOrRequire);
                        if (stylesheetFile != null) {
                            addCompletions(result, stylesheetFile);
                        }
                    }
                }

            }

            private void addCompletions(@NotNull CompletionResultSet result, StylesheetFile stylesheetFile) {
                for (CssClass cssClass : PsiTreeUtil.findChildrenOfType(stylesheetFile, CssClass.class)) {
                    if(!CssModulesUtil.isCssModuleClass(cssClass)) {
                        continue;
                    }
                    LookupElementBuilder element = LookupElementBuilder.createWithIcon(cssClass);
                    if (cssClass.getPresentation() != null) {
                        final String location = cssClass.getPresentation().getLocationString();
                        element = element.withTypeText(location, true);
                    }
                    result.addElement(element);
                }
            }
        };

        extend(CompletionType.BASIC, PlatformPatterns.psiElement(), provider);

    }


}