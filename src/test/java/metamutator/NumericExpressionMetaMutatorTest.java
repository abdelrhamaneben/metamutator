package metamutator;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;


import org.junit.Test;

import spoon.Launcher;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.visitor.filter.NameFilter;
import spoon.reflect.visitor.filter.TypeFilter;
import bsh.Interpreter;
import static org.apache.commons.lang.reflect.MethodUtils.*;

public class NumericExpressionMetaMutatorTest {

    @Test
    public void testBinaryOperatorMetaMutator() throws Exception {
        // build the model and apply the transformation
        Launcher l = new Launcher();
        l.addInputResource("src/test/java/resources/Foo.java");
        l.addProcessor(new NumericExpressionMetaMutator());
        l.run();

        // now we get the code of Foo
        CtClass c = (CtClass) l.getFactory().Package().getRootPackage().getElements(new NameFilter("Foo")).get(0);
        
        // printing the metaprogram
        System.out.println("// Metaprogram: ");
        System.out.println(c.toString());

        // we prepare an interpreter for the transformed code
        Interpreter bsh = new Interpreter();
        // creating a new instance of the class
        Object o = ((Class)bsh.eval(c.toString())).newInstance();
        
        // test with the second mutation hotspot
        Selector sel1=Selector.getSelectorByName( NumericExpressionMetaMutator.PREFIX + "4");
        sel1.choose(0);// INIT B
        assertEquals(-1, invokeExactMethod(o, "add", new Object[] {3, -4}));   
        sel1.choose(1);// ABS B
        assertEquals(7, invokeExactMethod(o, "add", new Object[] {3, -4}));  
        sel1.choose(3);// MINUS B
        assertEquals(7, invokeExactMethod(o, "add", new Object[] {3, -4})); 
        sel1.choose(4);// INC B
        assertEquals(0, invokeExactMethod(o, "add", new Object[] {3, -4}));  
        sel1.choose(5);// DEC B
        assertEquals(-2, invokeExactMethod(o, "add", new Object[] {3, -4}));
        
        metamutator.NumericExpressionMetaMutator.UNARY[] abc =new metamutator.NumericExpressionMetaMutator.UNARY[]{};
    }
}