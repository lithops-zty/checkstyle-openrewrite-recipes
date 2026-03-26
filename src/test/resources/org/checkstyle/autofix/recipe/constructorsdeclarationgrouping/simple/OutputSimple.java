package org.checkstyle.autofix.recipe.constructorsdeclarationgrouping.simple;

public class OutputSimple {

    int x;

    OutputSimple() {}

    OutputSimple(String s) {}

    OutputSimple(int x) {} // violation 'Constructors should be grouped together'

    OutputSimple(String s, int x) {} // violation 'Constructors should be grouped together'

    void foo() {}
}
