package org.organicdesign.fp;

import org.junit.Test;
import org.organicdesign.fp.collections.ImList;
import org.organicdesign.fp.collections.ImMap;
import org.organicdesign.fp.collections.RangeOfInt;
import org.organicdesign.fp.collections.UnmodMap;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.Assert.*;
import static org.organicdesign.fp.StaticImports.*;
import static org.organicdesign.fp.TradJavaStreamComparisonTest.ColorVal.*;

public class TradJavaStreamComparisonTest {

    // Declare a simple enum with a character mapping (eg. for storing in a database or passing
    // over a network).  We'll calculate and store the reverse-mapping three different ways,
    // Once with UncleJim (3 lines), once using "Traditional" Java (6 lines plus closing braces),
    // and once using the new Java8 streams (9 lines, plus closing braces).
    public enum ColorVal {
        RED('R'),
        GREEN('G'),
        BLUE('B');
        private final Character ch;
        ColorVal(Character c) { ch = c; }
        public Character ch() { return ch; }

        // UncleJim's way (3 lines of code):
        static final ImMap<Character,ColorVal> charToColorMapU =
                vec(values())
                .toImMap(v -> tup(v.ch(), v));

        // Same thing in "traditional" Java (6 lines, plus closing braces):
        static final Map<Character,ColorVal> charToColorMapT;
        static {
            Map<Character,ColorVal> tempMap = new HashMap<>();
            for (ColorVal v : values()) {
                tempMap.put(v.ch(), v);
            }
            charToColorMapT = Collections.unmodifiableMap(tempMap);
        }

        // Same thing with Java 8's streams (9 lines, plus closing braces).
        static final Map<Character,ColorVal> charToColorMap8 = Collections.unmodifiableMap(
                Arrays.stream(values()).reduce(
                        new HashMap<>(),
                        (accum, v) -> {
                            accum.put(v.ch(), v);
                            return accum;
                        },
                        (accum1, accum2) -> {
                            accum1.putAll(accum2);
                            return accum1;
                        }));

        // If you were using a mutable map, you'd want to protect it with a method.
        // It's still a good idea to do that on public classes to defend against having to change
        // your public interface over time.
        public static ColorVal fromCharU(Character c) { return charToColorMapU.get(c); }
        public static ColorVal fromCharT(Character c) { return charToColorMapT.get(c); }
        public static ColorVal fromChar8(Character c) { return charToColorMap8.get(c); }
    }

    // Prove that all three reverse-mappings work.
    @Test public void testReverseMapping() {
        assertEquals(RED, ColorVal.fromCharU('R'));
        assertEquals(RED, ColorVal.fromCharT('R'));
        assertEquals(RED, ColorVal.fromChar8('R'));

        assertEquals(GREEN, ColorVal.fromCharU('G'));
        assertEquals(GREEN, ColorVal.fromCharT('G'));
        assertEquals(GREEN, ColorVal.fromChar8('G'));

        assertEquals(BLUE, ColorVal.fromCharU('B'));
        assertEquals(BLUE, ColorVal.fromCharT('B'));
        assertEquals(BLUE, ColorVal.fromChar8('B'));

        assertNull(ColorVal.fromCharU('x'));
        assertNull(ColorVal.fromCharT('x'));
        assertNull(ColorVal.fromChar8('x'));
    }

    // Prove that all three reverse-mappings cannot be changed accidentally

    // Notice that UncleJim is the only one to give us a deprecation warning on this mutator
    // method because it always throws an exception.  It's not deprecated in the sense that the
    // method is ever going away.  But the warning indicates the coding error of ever trying to
    // call this method.
    @SuppressWarnings("deprecation")
    @Test(expected = UnsupportedOperationException.class)
    public void revMapExJ() { charToColorMapU.put('Z', RED); }

    // Traditional Java doesn't indicate that the put method always throws an exception.
    @Test(expected = UnsupportedOperationException.class)
    public void revMapExT() { charToColorMapT.put('Z', RED); }

    // Java8 doesn't indicate that the put method always throws an exception.
    @Test(expected = UnsupportedOperationException.class)
    public void revMapEx8() { charToColorMap8.put('Z', RED); }

    // Maybe your code has to inter-operate with a legacy system that instead of letters, uses
    // numbers to indicate the three primary colors:
    @Test public void testExtension() {

        // UncleJim has methods that return new lightweight copies of the original map,
        // with the new value added.  This makes it a snap to extend existing immutable data
        // structures: 3 loc.
        final ImMap<Character,ColorVal> extendedMapU = charToColorMapU.assoc('1', RED)
                                                                      .assoc('2', GREEN)
                                                                      .assoc('3', BLUE);

        // To make the construction private, we declare a block in traditional Java.  7 loc.
        final Map<Character,ColorVal> extendedMapT;
        {
            Map<Character,ColorVal> tempMap = new HashMap<>();
            tempMap.putAll(charToColorMapT);
            tempMap.put('1', RED);
            tempMap.put('2', GREEN);
            tempMap.put('3', BLUE);
            extendedMapT = Collections.unmodifiableMap(tempMap);
        }

        // In Java8 we can use a lambda to make construction private.  8 loc, including a cast.
        // I admit I had to look up the name of this functional class in the API docs because I
        // thought it was Producer.
        final Map<Character,ColorVal> extendedMap8 = ((Supplier<Map<Character,ColorVal>>) () -> {
            Map<Character,ColorVal> tempMap = new HashMap<>();
            tempMap.putAll(charToColorMap8);
            tempMap.put('1', RED);
            tempMap.put('2', GREEN);
            tempMap.put('3', BLUE);
            return Collections.unmodifiableMap(tempMap);
        }).get();

        // All three methods produce the same correct results.  The question is which code would
        // you rather write to achieve these results?
        assertEquals(RED, extendedMapU.get('R'));
        assertEquals(RED, extendedMapU.get('1'));
        assertEquals(RED, extendedMapT.get('R'));
        assertEquals(RED, extendedMapT.get('1'));
        assertEquals(RED, extendedMap8.get('R'));
        assertEquals(RED, extendedMap8.get('1'));

        assertEquals(GREEN, extendedMapU.get('G'));
        assertEquals(GREEN, extendedMapU.get('2'));
        assertEquals(GREEN, extendedMapT.get('G'));
        assertEquals(GREEN, extendedMapT.get('2'));
        assertEquals(GREEN, extendedMap8.get('G'));
        assertEquals(GREEN, extendedMap8.get('2'));

        assertEquals(BLUE, extendedMapU.get('B'));
        assertEquals(BLUE, extendedMapU.get('3'));
        assertEquals(BLUE, extendedMapT.get('B'));
        assertEquals(BLUE, extendedMapT.get('3'));
        assertEquals(BLUE, extendedMap8.get('B'));
        assertEquals(BLUE, extendedMap8.get('3'));

        assertNull(extendedMapU.get('x'));
        assertNull(extendedMapU.get('4'));
        assertNull(extendedMapT.get('x'));
        assertNull(extendedMapT.get('4'));
        assertNull(extendedMap8.get('x'));
        assertNull(extendedMap8.get('4'));
    }

    // ====================================== Second Example =====================================
    // TODO: This is maybe not the best example - it needs work!
    // It would be better to show a lambda with a checked exception.

    // UncleJim's way: 3 loc, 3 loc, 3 loc
    @Test
    public void colorSquareU() {
        ImList<Color> imgData = RangeOfInt.of(0, 256)
                .flatMap(i -> RangeOfInt.of(0, 256).map(j -> new Color(i, (i + j) / 2, 255)))
                .toImList();

        assertTrue(imgData.toString()
                          .startsWith("PersistentVector(" +
                                      "java.awt.Color[r=0,g=0,b=255]," +
                                      "java.awt.Color[r=0,g=0,b=255]," +
                                      "java.awt.Color[r=0,g=1,b=255]," +
                                      "java.awt.Color[r=0,g=1,b=255]," +
                                      "java.awt.Color[r=0,g=2,b=255],"));

        ImMap<Color,Integer> counts = imgData
                .foldLeft(map(),
                          (accum, color) -> accum.assoc(color, accum.getOrElse(color, 0) + 1));

        assertEquals(32896, counts.size());
        assertEquals(Integer.valueOf(2), counts.get(new Color(0, 0, 255)));
        assertEquals(Integer.valueOf(2), counts.get(new Color(16, 128, 255)));
        assertEquals(Integer.valueOf(2), counts.get(new Color(32, 128, 255)));
        assertEquals(Integer.valueOf(2), counts.get(new Color(255, 254, 255)));

        UnmodMap.UnEntry<Color,Integer> mostPopularColor =
                counts.foldLeft((UnmodMap.UnEntry<Color,Integer>) tup((Color) null, 0),
                                (max, entry) -> (entry.getValue() > max.getValue()) ? entry : max);

//        System.out.println("mostPopularColor: " + mostPopularColor);
        assertEquals(Integer.valueOf(2), mostPopularColor.getValue());
    }

    // Same thing in "Traditional" Java: 4 loc + 2 brackets, 8 loc + 4 brackets, 4 loc + 2 brackets
    @Test public void colorSquareT() {
        java.util.List<Color> imgData = new ArrayList<>();
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                imgData.add(new Color(i, (i + j) / 2, 255));
            }
        }

        assertTrue(imgData.toString()
                          .startsWith("[" +
                                      "java.awt.Color[r=0,g=0,b=255], " +
                                      "java.awt.Color[r=0,g=0,b=255], " +
                                      "java.awt.Color[r=0,g=1,b=255], " +
                                      "java.awt.Color[r=0,g=1,b=255], " +
                                      "java.awt.Color[r=0,g=2,b=255], "));

        Map<Color,Integer> counts = new HashMap<>();
        for (Color c : imgData) {
            counts.put(c, counts.getOrDefault(c, 0) + 1);
        }

        assertEquals(32896, counts.size());
        assertEquals(Integer.valueOf(2), counts.get(new Color(0, 0, 255)));
        assertEquals(Integer.valueOf(2), counts.get(new Color(16, 128, 255)));
        assertEquals(Integer.valueOf(2), counts.get(new Color(32, 128, 255)));
        assertEquals(Integer.valueOf(2), counts.get(new Color(255, 254, 255)));

        Map.Entry<Color,Integer> max = new Map.Entry<Color,Integer>() {
            @Override public Color getKey() { return null; }
            @Override public Integer getValue() { return 0; }
            @Override public Integer setValue(Integer value) {
                throw new UnsupportedOperationException();
            }
        };
        for (Map.Entry<Color,Integer> entry : counts.entrySet()) {
            if (entry.getValue() > max.getValue()) {
                max = entry;
            }
        }

        assertEquals(Integer.valueOf(2), max.getValue());
    }

    // Same thing with Java 8's Streams - I just used traditional Java to populate the list.
    // 4 loc + 2 brackets, 10 loc + 3 bracket-lines, 2 loc
    @Test public void colorSquare8() {
        java.util.List<Color> imgData = new ArrayList<>();
        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                imgData.add(new Color(i, (i + j) / 2, 255));
            }
        }
        assertTrue(imgData.toString()
                          .startsWith("[" +
                                      "java.awt.Color[r=0,g=0,b=255], " +
                                      "java.awt.Color[r=0,g=0,b=255], " +
                                      "java.awt.Color[r=0,g=1,b=255], " +
                                      "java.awt.Color[r=0,g=1,b=255], " +
                                      "java.awt.Color[r=0,g=2,b=255], "));

        Map<Color,Integer> counts = imgData.stream()
                .reduce(new HashMap<>(),
                        (HashMap<Color,Integer> accum, Color c) -> {
                            accum.put(c, accum.getOrDefault(c, 0) + 1);
                            return accum;
                        },
                        (HashMap<Color,Integer> accum1, HashMap<Color,Integer> accum2) -> {
                            for (Map.Entry<Color,Integer> e : accum2.entrySet()) {
                                Color key = e.getKey();
                                accum1.put(key, accum1.getOrDefault(key, 0) + e.getValue());
                            }
                            return accum1;
                        });

        assertEquals(32896, counts.size());
        assertEquals(Integer.valueOf(2), counts.get(new Color(0, 0, 255)));
        assertEquals(Integer.valueOf(2), counts.get(new Color(16, 128, 255)));
        assertEquals(Integer.valueOf(2), counts.get(new Color(32, 128, 255)));
        assertEquals(Integer.valueOf(2), counts.get(new Color(255, 254, 255)));

        Optional<Map.Entry<Color,Integer>> mostPopularColor =
                counts.entrySet().stream().max((e1, e2) -> e1.getValue() - e2.getValue());

        assertEquals(Integer.valueOf(2), mostPopularColor.get().getValue());
    }

}