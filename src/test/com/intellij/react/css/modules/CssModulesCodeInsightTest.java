/*
 * Copyright (c) 2016-present, Jim Kynde Meyer
 * All rights reserved.
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.intellij.react.css.modules;

import com.google.common.collect.Lists;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.testFramework.LightProjectDescriptor;
import com.intellij.testFramework.fixtures.LightCodeInsightFixtureTestCase;
import com.intellij.usageView.UsageInfo;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.List;


public class CssModulesCodeInsightTest extends LightCodeInsightFixtureTestCase {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        myFixture.copyFileToProject("Component.css");
        myFixture.copyFileToProject("ComponentFindUsages.jsx");
        myFixture.copyFileToProject("react.d.ts");
        myFixture.copyFileToProject("tsconfig.json");
    }

    @Override
    protected String getTestDataPath() {
        return "test-resources/testData";
    }

    @NotNull
    @Override
    protected LightProjectDescriptor getProjectDescriptor() {
        return super.getProjectDescriptor();
    }


    // ---- completion ----

    @Test
    public void testCompletionComponent() {
        doTestCompletion("Component.jsx", Lists.newArrayList("error", "normal", "north"));
    }

    @Test
    public void testCompletionComponentEs6Import() {
        doTestCompletion("ComponentEs6Import.jsx", Lists.newArrayList("error", "normal", "north"));
    }

    @Test
    public void testCompletionComponentEs6ImportStyleName() {
        doTestCompletion("ComponentEs6ImportStyleName.jsx", Lists.newArrayList("error", "normal", "north"));
    }

    @Test
    public void testCompletionComponentNor() {
        doTestCompletion("ComponentNor.jsx", Lists.newArrayList("normal", "north"));
    }

    @Test
    public void testCompletionComponentNormalErr() {
        doTestCompletion("ComponentNormalErr.jsx", null); // single match completion shows as null
    }

    @Test
    public void testCompletionComponentStringLiteral() {
        doTestCompletion("ComponentStringLiteral.jsx", Lists.newArrayList("error", "normal", "north"));
    }

    @Test
    public void testCompletionComponentStringLiteralNor() {
        doTestCompletion("ComponentStringLiteralNor.jsx", Lists.newArrayList("normal", "north"));
    }

    @Test
    public void testCompletionComponentTypeScript() {
        doTestCompletion("ComponentTypeScript.tsx", Lists.newArrayList("error", "normal", "north"));
    }

    @Test
    public void testCompletionComponentTypeScriptStringLiteral() {
        doTestCompletion("ComponentTypeScriptStringLiteral.tsx", Lists.newArrayList("normal", "north"));
    }

    @Test
    public void testCompletionComposesClassName() {
        doTestCompletion("CompletionComposesClassName.css", Lists.newArrayList("normal", "north"));
    }

    @Test
    public void testCompletionComposesClassNameMultiple() {
        doTestCompletion("CompletionComposesClassNameMultiple.css", Lists.newArrayList("north"));
    }

    @Test
    public void testCompletionComposesClassNameSemiColon() {
        doTestCompletion("CompletionComposesClassNameSemiColon.css", Lists.newArrayList("normal", "north"));
    }

    @Test
    public void testCompletionComposesProperty() {
        doTestCompletion("CompletionComposesProperty.css", Lists.newArrayList("composes", "mask-composite", "-webkit-background-composite", "-webkit-mask-composite"));
    }

    private void doTestCompletion(String sourceFile, List<String> expectedCompletions) {
        myFixture.configureByFiles(sourceFile);
        myFixture.complete(CompletionType.BASIC, 1);
        final List<String> completions = myFixture.getLookupElementStrings();
        assertEquals("Wrong completions", expectedCompletions, completions);
    }


    // ---- error highlighting -----

    @Test
    public void testCssAnnotations() {
        myFixture.configureByFiles("Annotations.css");
        myFixture.checkHighlighting(true, false, true);
    }

    @Test
    public void testComponentAnnotations() {
        myFixture.configureByFiles("ComponentAnnotations.jsx");
        myFixture.checkHighlighting(false, false, false);
    }

    @Test
    public void testComponentTypeScriptAnnotations() {
        myFixture.configureByFiles("ComponentTypeScriptAnnotations.tsx");
        myFixture.checkHighlighting(false, false, false);
    }


    // --- PSI references (find usages etc.) ---

    @Test
    public void testComponentFindUsages() {
        final List<UsageInfo> usageInfos = Lists.newArrayList(myFixture.testFindUsages("ComponentFindUsages.css"));
        assertEquals(3, usageInfos.size()); // 2 from this plugin, one is self reference
    }

}
