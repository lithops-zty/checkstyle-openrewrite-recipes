package org.checkstyle.autofix.recipe.constructorsdeclarationgrouping;

import static com.google.common.truth.Truth.assertWithMessage;

import java.util.List;

import org.checkstyle.autofix.parser.ReportParser;
import org.checkstyle.autofix.recipe.AbstractRecipeTestSupport;
import org.checkstyle.autofix.recipe.ConstructorsDeclarationGrouping;
import org.checkstyle.autofix.recipe.RecipeTest;
import org.junit.jupiter.api.Test;

public class ConstructorsDeclarationGroupingTest extends AbstractRecipeTestSupport {

    @Override
    protected String getSubpackage() {
        return "constructorsdeclarationgrouping";
    }

    @Test
    public void checkDescription() {
        final ConstructorsDeclarationGrouping recipe =
                new ConstructorsDeclarationGrouping(List.of());

        final String expectedDescription =
                "Groups all constructors together in a class.";

        assertWithMessage("Invalid description")
                .that(recipe.getDescription())
                .isEqualTo(expectedDescription);
    }

    @Test
    public void checkDisplayName() {
        final ConstructorsDeclarationGrouping recipe =
                new ConstructorsDeclarationGrouping(List.of());

        final String expectedDisplayName =
                "ConstructorsDeclarationGrouping recipe";

        assertWithMessage("Invalid display name")
                .that(recipe.getDisplayName())
                .isEqualTo(expectedDisplayName);
    }

    /**
     * Baseline test: two constructors separated by a method and a field.
     * Verifies that all violating constructors are moved to follow the initial group.
     */
    @RecipeTest
    void simple(ReportParser parser) throws Exception {
        verify(parser, "Simple");
    }

    /**
     * A single constructor is separated from the first constructor by a field declaration.
     * Verifies the minimal field-separation scenario.
     */
    @RecipeTest
    void separatedByField(ReportParser parser) throws Exception {
        verify(parser, "SeparatedByField");
    }

    /**
     * A single constructor is separated from the initial group by a method.
     * Verifies the minimal method-separation scenario.
     */
    @RecipeTest
    void separatedByMethod(ReportParser parser) throws Exception {
        verify(parser, "SeparatedByMethod");
    }

    /**
     * A single constructor is separated from the initial group by an inner class declaration.
     * Inner class declarations are non-constructor members and therefore trigger a violation.
     * Verifies the inner-class-separation scenario.
     */
    @RecipeTest
    void separatedByInnerClass(ReportParser parser) throws Exception {
        verify(parser, "SeparatedByInnerClass");
    }

    /**
     * Three constructors are each separated by a different member (field, field, method).
     * Verifies that all violating constructors are collected and inserted in their
     * original relative order after the initial constructor group.
     */
    @RecipeTest
    void multipleViolations(ReportParser parser) throws Exception {
        verify(parser, "MultipleViolations");
    }

    /**
     * Constructors are separated only by comments (single-line and block).
     * According to the rule, comments between constructors are allowed.
     * Verifies that no modification is made when there are no actual violations.
     */
    @RecipeTest
    void commentsBetween(ReportParser parser) throws Exception {
        verify(parser, "CommentsBetween");
    }

    /**
     * An inner static class has a constructor separated from its group by a field.
     * Verifies that the fix is correctly scoped to the inner class body without
     * affecting the outer class.
     */
    @RecipeTest
    void innerClass(ReportParser parser) throws Exception {
        verify(parser, "InnerClass");
    }

    /**
     * A private enum nested in a class has a constructor separated by a field.
     * Verifies that the fix works correctly inside enum bodies.
     */
    @RecipeTest
    void innerEnum(ReportParser parser) throws Exception {
        verify(parser, "InnerEnum");
    }

    /**
     * Exactly one constructor is separated from the sole initial constructor.
     * Verifies the simplest possible violation: a single isolated constructor.
     */
    @RecipeTest
    void onlyOneViolation(ReportParser parser) throws Exception {
        verify(parser, "OnlyOneViolation");
    }

    /**
     * All constructors are already grouped together with no violations.
     * Verifies that the Recipe makes no changes when the code is already compliant.
     */
    @RecipeTest
    void noViolation(ReportParser parser) throws Exception {
        verify(parser, "NoViolation");
    }

    /**
     * Constructors with different access modifiers (public, protected, private) are
     * separated by a field. Verifies that modifiers are preserved correctly after moving.
     */
    @RecipeTest
    void modifiers(ReportParser parser) throws Exception {
        verify(parser, "Modifiers");
    }

    /**
     * Constructors carrying annotations (@SuppressWarnings, @Deprecated) are separated
     * by a method. Verifies that annotations are preserved correctly after moving.
     */
    @RecipeTest
    void annotations(ReportParser parser) throws Exception {
        verify(parser, "Annotations");
    }

    /**
     * The first constructor is preceded by fields and methods; a second constructor
     * appears after another method. Verifies that the fix works when the constructor
     * group does not start at the top of the class body.
     */
    @RecipeTest
    void outerClassSeparated(ReportParser parser) throws Exception {
        verify(parser, "OuterClassSeparated");
    }

    /**
     * Both the outer class and a nested static inner class independently have
     * ungrouped constructors. Verifies that the fix is applied correctly and
     * independently in each class scope.
     */
    @RecipeTest
    void multipleGroups(ReportParser parser) throws Exception {
        verify(parser, "MultipleGroups");
    }

}
