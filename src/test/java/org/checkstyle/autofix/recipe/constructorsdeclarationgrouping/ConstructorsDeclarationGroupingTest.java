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

    @RecipeTest
    void simple(ReportParser parser) throws Exception {
        verify(parser, "Simple");
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
}
