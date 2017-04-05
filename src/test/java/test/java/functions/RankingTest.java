package test.java.functions;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by holi on 4/3/17.
 */
public class RankingTest {

    @Test
    void singleton() throws Throwable {
        assertThat(ranking("foo"), equalTo(singletonList("1 - foo (3)")));
    }

    @Test
    void sortsByLexicographicallyWhenRankingWith2WordsThatTheirLengthAreTheSame() throws Throwable {
        assertThat(ranking("foo bar"), equalTo(asList(
        /**/"1 - bar (3)",
        /**/"1 - foo (3)"
        )));
    }

    @Test
    void sortsByLengthWhenRankingWith2WordsThatTheirLengthAreDifferent() throws Throwable {
        assertThat(ranking("fuzz bar"), equalTo(asList(
        /**/"1 - fuzz (4)",
        /**/"2 - bar (3)"
        )));
    }

    @Test
    void incrementsRankOnLastRankingItem() throws Throwable {
        assertThat(ranking("fuzz buzz foo"), equalTo(asList(
        /**/"1 - buzz (4)",
        /**/"1 - fuzz (4)",
        /**/"2 - foo (3)"
        )));
    }

    private List<String> ranking(String sentence) {
        Map.Entry<Integer, String> NONE = new AbstractMap.SimpleEntry<>(0, "");

        BiConsumer<Stack<Entry<Integer, String>>, String> rankingEvaluation = (ranking, it) -> {
            Entry<Integer, String> last = ranking.isEmpty() ? NONE : ranking.peek();
            int rank = last.getValue().length() == it.length() ? last.getKey() : last.getKey() + 1;
            ranking.add(new AbstractMap.SimpleEntry<>(rank, it));
        };

        return Arrays.stream(sentence.split(" "))
                .sorted(comparing(String::length).reversed()
                        .thenComparing(String::compareTo))
                .collect(Stack::new, rankingEvaluation, List::addAll).stream()
                .map(it -> format("%d - %s (%d)", it.getKey(), it.getValue(), it.getValue().length()))
                .collect(toList());
    }

}
