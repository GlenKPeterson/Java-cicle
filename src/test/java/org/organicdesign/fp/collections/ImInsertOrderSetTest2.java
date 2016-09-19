package org.organicdesign.fp.collections;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.organicdesign.fp.FunctionUtils.ordinal;

/**
 Created by gpeterso on 9/16/16.
 */
public class ImInsertOrderSetTest2 {
    @Test public void testBasics() {
        List<String> control1 = new ArrayList<>();
        Set<String> control2 = new HashSet<>();
        ImSet<String> test = ImInsertOrderSet2.empty();

        assertEquals(control1.size(), test.size());
        assertEquals(control2.hashCode(), test.hashCode());
        assertEquals(control2, test);
        assertEquals(test, control2);
        assertEquals("ImInsertOrderSet2()", test.toString());

        final int SIZE = 100;

        for (int i = SIZE; i > 0; i--) {
            control1.add(ordinal(i));
            control2.add(ordinal(i));
            test = test.put(ordinal(i));
            assertEquals(control2.contains(ordinal(i)), test.contains(ordinal(i)));
            assertEquals(control1.size(), test.size());
            assertEquals(control2.hashCode(), test.hashCode());
            assertEquals(control2, test);
            assertEquals(test, control2);
        }

        control1.add(null);
        control2.add(null);
        test = test.put(null);

        assertEquals(control2.contains(null), test.contains(null));
        assertEquals(control1.size(), test.size());
        assertEquals(control2.hashCode(), test.hashCode());
        assertEquals(control2, test);
        assertEquals(test, control2);

        Random rand = new Random();
        for (int i = SIZE; i > 0; i--) {
            int idx = rand.nextInt(i);
            control1.remove(ordinal(idx));
            control2.remove(ordinal(idx));
            test = test.without(ordinal(idx));

            assertEquals(control2.contains(ordinal(idx)), test.contains(ordinal(idx)));
            assertEquals(control1.size(), test.size());
            assertEquals(control2.hashCode(), test.hashCode());
            assertEquals(control2, test);
            assertEquals(test, control2);
        }
    }

    @Test public void coverageJunky() {
        assertEquals("ImInsertOrderSet2(\"hello\")",
                     ImInsertOrderSet2.empty().put("hello").toString());
    }
}