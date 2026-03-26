/*xml
<module name="Checker">
  <module name="TreeWalker">
    <module name="com.puppycrawl.tools.checkstyle.checks.coding.ConstructorsDeclarationGroupingCheck"/>
  </module>
</module>
*/

package org.checkstyle.autofix.recipe.constructorsdeclarationgrouping.simple;

public class InputSimple {

    int x;

    InputSimple() {}

    InputSimple(String s) {}

    void foo() {}

    InputSimple(int x) {} // violation 'Constructors should be grouped together'

    InputSimple(String s, int x) {} // violation 'Constructors should be grouped together'
}
