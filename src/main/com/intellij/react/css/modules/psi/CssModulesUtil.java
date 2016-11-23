/*
 * Copyright (c) 2016-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.react.css.modules.psi;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.JSTokenTypes;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSIndexedPropertyAccessExpression;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.openapi.util.Ref;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.css.CssClass;
import com.intellij.psi.css.CssFunction;
import com.intellij.psi.css.StylesheetFile;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.filters.position.FilterPattern;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import org.jetbrains.annotations.Nullable;

/**
 * Utility methods for navigating PSI trees with regards to CSS Modules.
 */
public class CssModulesUtil {

    /**
     * Filters to "styleName" React attributes.
     */
    public static final ElementFilter STYLE_NAME_FILTER = new ElementFilter() {
        @Override
        public boolean isAcceptable(Object element, @Nullable PsiElement context) {
            if (element instanceof XmlAttributeValue && context != null && context.getContainingFile() instanceof JSFile) {
                final XmlAttributeValue value = (XmlAttributeValue) element;
                final XmlAttribute xmlAttribute = PsiTreeUtil.getParentOfType(value, XmlAttribute.class);
                if (xmlAttribute != null) {
                    return xmlAttribute.getName().equals("styleName");
                }
            }
            return false;
        }

        @Override
        public boolean isClassAcceptable(Class hintClass) {
            return XmlAttributeValue.class.isAssignableFrom(hintClass);
        }
    };

    /**
     * PSI Pattern for matching "styleName" React attributes.
     */
    public static final PsiElementPattern.Capture<XmlAttributeValue> STYLE_NAME_PATTERN = PlatformPatterns
            .psiElement(XmlAttributeValue.class)
            .and(new FilterPattern(STYLE_NAME_FILTER));


    /**
     * PSI Pattern for matching string literals, e.g. the 'normal' in styles['normal']
     */
    public static final PsiElementPattern.Capture<JSLiteralExpression> STRING_PATTERN = PlatformPatterns.
            psiElement(JSLiteralExpression.class)
            .and(new FilterPattern(new ElementFilter() {
                @Override
                public boolean isAcceptable(Object element, @Nullable PsiElement context) {
                    if (element instanceof JSLiteralExpression && context != null && context.getContainingFile() instanceof JSFile) {
                        final ASTNode value = ((JSLiteralExpression) element).getNode().getFirstChildNode();
                        return value != null && value.getElementType() == JSTokenTypes.STRING_LITERAL;
                    }
                    return false;
                }

                @Override
                public boolean isClassAcceptable(Class hintClass) {
                    return JSLiteralExpression.class.isAssignableFrom(hintClass);
                }
            }));


    /**
     * Visits the containing file of the specified element to find a require to a style sheet file
     *
     * @param cssReferencingElement starting point for finding an imported style sheet file
     * @return the PSI file for the first imported style sheet file
     */
    public static StylesheetFile getImportedStyleSheetFile(PsiElement cssReferencingElement) {
        final Ref<StylesheetFile> file = new Ref<>();
        cssReferencingElement.getContainingFile().accept(new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                if (file.get() != null) {
                    return;
                }
                if (element instanceof JSLiteralExpression) {
                    if (resolveStyleSheetFile(element, file)) return;
                }
                super.visitElement(element);
            }
        });
        return file.get();
    }

    /**
     * Gets the CssClass PSI element whose name matches the specified cssClassName
     *
     * @param stylesheetFile the PSI style sheet file to visit
     * @param cssClass       the class to find, including the leading ".", e.g. ".my-class-name"
     * @return the matching class or <code>null</code> if no matches are found
     */
    public static CssClass getCssClass(StylesheetFile stylesheetFile, String cssClass) {
        final Ref<CssClass> cssClassRef = new Ref<>();
        stylesheetFile.accept(new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                if (cssClassRef.get() != null) {
                    return;
                }
                if (element instanceof CssClass) {
                    if (cssClass.equals(element.getText()) && isCssModuleClass((CssClass) element)) {
                        cssClassRef.set((CssClass) element);
                        return;
                    }
                }
                super.visitElement(element);
            }
        });
        return cssClassRef.get();
    }

    /**
     * Gets whether the specified CSS class is a CSS Modules class.
     * Classes nested in :global are considered false.
     */
    public static boolean isCssModuleClass(CssClass cssClass) {
        final CssFunction parentFunction = PsiTreeUtil.getParentOfType(cssClass, CssFunction.class);
        if(parentFunction != null) {
            if("global".equals(parentFunction.getName())) {
                // not a generated CSS Modules class
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the variable declaration that a string literal belongs to, e.g 'normal' -> 'const styles = require("./foo.css")' based on <code>styles['normal']</code>
     *
     * @param classNameLiteral a string literal that is potentially a CSS class name
     * @return the JS variable that is a potential require of a style sheet file, or <code>null</code> if the PSI structure doesn't match
     */
    public static JSVariable getCssClassNamesVariableDeclaration(JSLiteralExpression classNameLiteral) {
        final JSIndexedPropertyAccessExpression expression = PsiTreeUtil.getParentOfType(classNameLiteral, JSIndexedPropertyAccessExpression.class);
        if (expression != null) {
            // string literal is part of "var['string literal']", e.g. "styles['normal']"
            if (expression.getQualifier() != null) {
                final PsiReference psiReference = expression.getQualifier().getReference();
                if (psiReference != null) {
                    final PsiElement varReference = psiReference.resolve();
                    if (varReference instanceof JSVariable) {
                        return (JSVariable) varReference;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Resolves the style sheet PSI file that backs a require("./stylesheet.css").
     *
     * @param cssFileNameLiteralParent parent element to a file name string literal that points to a style sheet file
     * @return the matching style sheet PSI file, or <code>null</code> if the file can't be resolved
     */
    public static StylesheetFile resolveStyleSheetFile(PsiElement cssFileNameLiteralParent) {
        final Ref<StylesheetFile> stylesheetFileRef = new Ref<>();
        cssFileNameLiteralParent.accept(new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                if (stylesheetFileRef.get() != null) {
                    return;
                }
                if (element instanceof JSLiteralExpression) {
                    if (resolveStyleSheetFile(element, stylesheetFileRef)) return;
                }
                super.visitElement(element);
            }
        });
        return stylesheetFileRef.get();
    }

    /**
     * Resolves a CssClass PSI element given a CSS filename and class name
     *
     * @param cssFileNameLiteralParent element which contains a require'd style sheet file
     * @param cssClass                 the CSS class to get including the "."
     * @param referencedStyleSheet     ref to set to the style sheet that any matching CSS class is declared in
     * @return the matching CSS class, or <code>null</code> in case the class is unknown
     */
    public static CssClass getCssClass(PsiElement cssFileNameLiteralParent, String cssClass, Ref<StylesheetFile> referencedStyleSheet) {
        StylesheetFile stylesheetFile = resolveStyleSheetFile(cssFileNameLiteralParent);
        if (stylesheetFile != null) {
            referencedStyleSheet.set(stylesheetFile);
            return getCssClass(stylesheetFile, cssClass);
        } else {
            referencedStyleSheet.set(null);
            return null;
        }
    }

    /**
     * Gets the style sheet, if any, that the specified element resolves to
     *
     * @param element           element used to resolve
     * @param stylesheetFileRef the ref to set the resolved sheet on
     * @return true if the element resolves to a style sheet file, false otherwise
     */
    private static boolean resolveStyleSheetFile(PsiElement element, Ref<StylesheetFile> stylesheetFileRef) {
        for (PsiReference reference : element.getReferences()) {
            final PsiElement fileReference = reference.resolve();
            if (fileReference instanceof StylesheetFile) {
                stylesheetFileRef.set((StylesheetFile) fileReference);
                return true;
            }
        }
        return false;
    }

}
