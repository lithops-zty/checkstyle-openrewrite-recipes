///////////////////////////////////////////////////////////////////////////////////////////////
// checkstyle-openrewrite-recipes: Automatically fix Checkstyle violations with OpenRewrite.
// Copyright (C) 2025 The Checkstyle OpenRewrite Recipes Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
///////////////////////////////////////////////////////////////////////////////////////////////

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
import org.openrewrite.java.tree.Space;
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

            final List<Statement> statements = new ArrayList<>(cd.getBody().getStatements());

            // Identify violating constructors and the initial constructor group boundary
            final List<J.MethodDeclaration> violatingConstructors = new ArrayList<>();
            int firstConstructorIndex = -1;

            for (int i = 0; i < statements.size(); i++) {
                final Statement statement = statements.get(i);
                if (statement instanceof J.MethodDeclaration) {
                    final J.MethodDeclaration method = (J.MethodDeclaration) statement;
                    if (method.isConstructor()) {
                        if (firstConstructorIndex == -1) {
                            firstConstructorIndex = i;
                        }
                        if (isAtViolationLocation(method)) {
                            violatingConstructors.add(method);
                        }
                    }
                }
            }

            if (violatingConstructors.isEmpty() || firstConstructorIndex == -1) {
                return cd;
            }

            // Find the index just past the initial contiguous constructor group.
            // This is where violating constructors will be inserted.
            int insertIndex = firstConstructorIndex;
            while (insertIndex < statements.size()
                    && statements.get(insertIndex) instanceof J.MethodDeclaration
                    && ((J.MethodDeclaration) statements.get(insertIndex)).isConstructor()
                    && !violatingConstructors.contains(statements.get(insertIndex))) {
                insertIndex++;
            }

            // The prefix (leading whitespace / newlines) used by members at the insert position.
            // We reuse this prefix for each moved constructor so indentation stays consistent.
            final Space memberPrefix = statements.get(insertIndex).getPrefix();

            // Remove violating constructors from their original positions.
            // Violating constructors are always after the initial group, so removing them
            // does not affect insertIndex.
            for (final J.MethodDeclaration violatingConstructor : violatingConstructors) {
                statements.remove(violatingConstructor);
            }

            // Insert violating constructors right after the initial constructor group,
            // assigning the correct prefix to preserve indentation without reformatting.
            for (int i = 0; i < violatingConstructors.size(); i++) {
                final J.MethodDeclaration ctor = violatingConstructors.get(i)
                        .withPrefix(memberPrefix);
                statements.add(insertIndex + i, ctor);
            }

            return cd.withBody(cd.getBody().withStatements(statements));
        }

        private boolean isAtViolationLocation(J.MethodDeclaration methodDeclaration) {
            final J.CompilationUnit cursor =
                    getCursor().firstEnclosing(J.CompilationUnit.class);

            final int line = PositionHelper.computeLinePosition(
                    cursor, methodDeclaration, getCursor());

            boolean matches = false;

            for (final CheckstyleViolation violation : violations) {
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
