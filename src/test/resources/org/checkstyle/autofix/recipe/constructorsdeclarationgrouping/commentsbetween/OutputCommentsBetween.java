/*xml
<module name="Checker">
  <module name="TreeWalker">
    <module name="com.puppycrawl.tools.checkstyle.checks.coding.ConstructorsDeclarationGroupingCheck"/>
  </module>
</module>
*/

package org.checkstyle.autofix.recipe.constructorsdeclarationgrouping.commentsbetween;

/**
 * Class where constructors are separated only by comments.
 * Comments between constructors are allowed, so no violations should occur.
 */
public class OutputCommentsBetween {

    OutputCommentsBetween() {}

    // Default constructor above, parameterized below.
    OutputCommentsBetween(String s) {}

    /* Another comment block. */
    OutputCommentsBetween(int x) {}

    void foo() {}

}
