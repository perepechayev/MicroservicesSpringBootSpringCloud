package org.psp.core.product;

public class PageAction extends BasePage {

    public PageAction pa1() {
        return this;
    }

    public PageAction pa2() {
        return this;
    }

    public static void test() {
        PageAction p = new PageAction();
        p.pa1().m().m();
    }
}
