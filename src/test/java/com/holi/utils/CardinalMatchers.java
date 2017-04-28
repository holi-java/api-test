package com.holi.utils;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.equalTo;

/**
 * Created by holi on 4/29/17.
 */
public class CardinalMatchers {
    public static Matcher<Number> once() {
        return exactly(1);
    }

    public static Matcher<Number> never() {
        return exactly(0);
    }

    public static Matcher<Number> exactly(int times) {
        return is(equalTo(times));
    }

    public static Matcher<Number> is(final Matcher<Integer> matcher) {
        return new FeatureMatcher<Number, Integer>(matcher, "call times", "") {
            @Override
            protected Integer featureValueOf(Number actual) {
                return actual.intValue();
            }
        };
    }
}
