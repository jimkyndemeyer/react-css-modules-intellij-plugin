/*
 * Copyright (c) 2016-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.react.css.modules.ide.intentions;

import com.intellij.codeInsight.intention.HighPriorityAction;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.ide.impl.DataManagerImpl;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.css.CssElementFactory;
import com.intellij.psi.css.StylesheetFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlToken;
import com.intellij.react.css.modules.psi.CssModulesUnknownClassPsiReference;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * Intention for creating a missing CSS Modules class
 */
public class CssModulesCreateClassIntention extends PsiElementBaseIntentionAction implements HighPriorityAction {

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        final PsiElement intentionElement = getIntentionElement(element);
        for (PsiReference psiReference : intentionElement.getReferences()) {
            if (psiReference instanceof CssModulesUnknownClassPsiReference) {
                final String className = psiReference.getRangeInElement().substring(intentionElement.getText());
                final StylesheetFile stylesheetFile = ((CssModulesUnknownClassPsiReference) psiReference).getStylesheetFile();
                stylesheetFile.navigate(true);
                PsiElement ruleset = CssElementFactory.getInstance(project).createRuleset("." + className + " {\n\n}", stylesheetFile.getLanguage());
                ruleset = stylesheetFile.add(ruleset);
                final int newCaretOffset = ruleset.getTextOffset() + ruleset.getText().indexOf("{") + 2; // after '{\n'
                final FileEditor[] editors = FileEditorManager.getInstance(project).getEditors(stylesheetFile.getVirtualFile());
                for (FileEditor fileEditor : editors) {
                    if (fileEditor instanceof TextEditor) {
                        final Editor cssEditor = ((TextEditor) fileEditor).getEditor();
                        cssEditor.getCaretModel().moveToOffset(newCaretOffset);
                        AnAction editorLineEnd = ActionManager.getInstance().getAction("EditorLineEnd");
                        if (editorLineEnd != null) {
                            final AnActionEvent actionEvent = AnActionEvent.createFromDataContext(
                                    ActionPlaces.UNKNOWN,
                                    null,
                                    new DataManagerImpl.MyDataContext(cssEditor.getComponent())
                            );
                            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(cssEditor.getDocument());
                            editorLineEnd.actionPerformed(actionEvent);
                        }
                    }
                }
                return;
            }
        }
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        final PsiElement intentionElement = getIntentionElement(element);
        if (intentionElement != null) {
            for (PsiReference psiReference : intentionElement.getReferences()) {
                if (psiReference instanceof CssModulesUnknownClassPsiReference) {
                    return true;
                }
            }
        }
        return false;
    }

    @NotNull
    @Override
    public String getText() {
        return "Create CSS Modules class";
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return getText();
    }


    private PsiElement getIntentionElement(@NotNull PsiElement element) {
        PsiElement intentionElement;
        if (element instanceof XmlToken) {
            intentionElement = PsiTreeUtil.getParentOfType(element, XmlAttributeValue.class);
        } else {
            intentionElement = PsiTreeUtil.getParentOfType(element, JSLiteralExpression.class);
            if (intentionElement == null) {
                intentionElement = PsiTreeUtil.getPrevSiblingOfType(element, JSLiteralExpression.class);
            }
        }
        return intentionElement;
    }

}
