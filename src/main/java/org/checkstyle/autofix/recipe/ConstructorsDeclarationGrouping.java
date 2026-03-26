package org.checkstyle.autofix.recipe;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.checkstyle.autofix.PositionHelper;
import org.checkstyle.autofix.parser.CheckstyleViolation;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.J;
import org.openrewrite.java.tree.Statement;

/**
 * Fixes Checkstyle ConstructorsDeclarationGrouping violations by moving
 * out-of-order constructors to group them together with other constructors.
 */
public class ConstructorsDeclarationGrouping extends Recipe {

    private final List<CheckstyleViolation> violations;

    public ConstructorsDeclarationGrouping(List<CheckstyleViolation> violations) {
        this.violations = violations;
    }

    @Override
    public String getDisplayName() {
        return "ConstructorsDeclarationGrouping recipe";
    }

    @Override
    public String getDescription() {
        return "Groups all constructors together in a class.";
    }

    @Override
    public TreeVisitor<?, ExecutionContext> getVisitor() {
        return new ConstructorsDeclarationGroupingVisitor();
    }

    private final class ConstructorsDeclarationGroupingVisitor
            extends JavaIsoVisitor<ExecutionContext> {

        private Path sourcePath;

        @Override
        public J.CompilationUnit visitCompilationUnit(
                J.CompilationUnit cu, ExecutionContext executionContext) {
            this.sourcePath = cu.getSourcePath();
            return super.visitCompilationUnit(cu, executionContext);
        }

        @Override
        public J.ClassDeclaration visitClassDeclaration(
                J.ClassDeclaration classDecl, ExecutionContext executionContext) {

            J.ClassDeclaration cd = super.visitClassDeclaration(classDecl, executionContext);

            // Find all methods that are constructors
            List<Statement> statements = new ArrayList<>(cd.getBody().getStatements());
            List<J.MethodDeclaration> constructors = new ArrayList<>();
            List<J.MethodDeclaration> violatingConstructors = new ArrayList<>();
            int firstConstructorIndex = -1;
            int lastConstructorIndex = -1;

            for (int i = 0; i < statements.size(); i++) {
                Statement statement = statements.get(i);
                if (statement instanceof J.MethodDeclaration) {
                    J.MethodDeclaration method = (J.MethodDeclaration) statement;
                    if (method.isConstructor()) {
                        constructors.add(method);
                        if (firstConstructorIndex == -1) {
                            firstConstructorIndex = i;
                        }
                        lastConstructorIndex = i;
                        if (isAtViolationLocation(method)) {
                            violatingConstructors.add(method);
                        }
                    }
                }
            }

            if (!violatingConstructors.isEmpty() && firstConstructorIndex != -1) {
                // Determine where to group them. We group them right after the last non-violating constructor.
                // A simpler approach: remove all violating constructors and insert them after the last valid constructor block.
                
                // Let's collect valid constructors that are part of the initial group
                int insertIndex = firstConstructorIndex;
                while (insertIndex < statements.size() && 
                       statements.get(insertIndex) instanceof J.MethodDeclaration && 
                       ((J.MethodDeclaration) statements.get(insertIndex)).isConstructor() &&
                       !violatingConstructors.contains(statements.get(insertIndex))) {
                    insertIndex++;
                }

                // Remove violating constructors from their original positions
                for (J.MethodDeclaration violatingConstructor : violatingConstructors) {
                    statements.remove(violatingConstructor);
                }

                // Insert violating constructors at the correct position
                // Since we removed elements, the insertIndex might need adjustment if we removed elements before it,
                // but violating constructors are always after the initial group.
                for (int i = 0; i < violatingConstructors.size(); i++) {
                    statements.add(insertIndex + i, violatingConstructors.get(i));
                }

                cd = cd.withBody(cd.getBody().withStatements(statements));
                // We should trigger maybeAutoFormat if we changed the tree, but let's rely on basic spacing first
                cd = maybeAutoFormat(classDecl, cd, executionContext);
            }

            return cd;
        }

        private boolean isAtViolationLocation(J.MethodDeclaration methodDeclaration) {
            final J.CompilationUnit cursor =
                    getCursor().firstEnclosing(J.CompilationUnit.class);

            final int line = PositionHelper.computeLinePosition(
                    cursor, methodDeclaration, getCursor());

            boolean matches = false;

            for (CheckstyleViolation violation : violations) {
                if (violation.getLine() == line
                        && violation.getFilePath().endsWith(sourcePath)) {
                    matches = true;
                    break;
                }
            }

            return matches;
        }
    }
}
