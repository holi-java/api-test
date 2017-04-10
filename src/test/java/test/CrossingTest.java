package test;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by holi on 4/10/17.
 */


public class CrossingTest {

    @Test
    void sortWithSpecificComparator() throws Throwable {
        List<Integer> list = asList(0, 1, 2);
        List<Integer> reversedList = asList(2, 1, 0);
        Comparator<Integer> originalOrder = (a, b) -> a < b ? -1 : 1;

        assertThat(update(list, with(originalOrder)), equalTo(list));

        assertThat(update(list, with(originalOrder.reversed())), equalTo(reversedList));
    }

    private List<Integer> update(List<Integer> input, Comparator comparator) {
        List<Integer> list = new ArrayList<>(input);
        SUT it = new SUT(comparator);

        it.update(list);
        return list;
    }

    private Comparator with(Comparator comparator) {
        return comparator;
    }

    class SUT {
        private Comparator comparator;

        public SUT(Comparator comparator) {
            this.comparator = comparator;
        }

        public void update(List toUpdate) {
            Collections.sort(toUpdate, comparator);
        }
    }
}


