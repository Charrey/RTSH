package com.charrey.util;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.Supplier;

public class AnyGenerator<C> implements Supplier<C> {

    private final Function<C, C> generator;
    private C current;

    public AnyGenerator(C init, Function<C, C> next) {
        generator = next;
        current = init;
    }

    @Override
    public C get() {
        C toReturn = current;
        current = generator.apply(current);
        return toReturn;
    }
}
