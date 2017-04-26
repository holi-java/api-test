package test.mockito;

import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static test.mockito.AdditionalAnswersTest.AnswerChain.will;

/**
 * Created by holi on 4/27/17.
 */
class AdditionalAnswersTest {

    @Test
    void returnMappedValueFromTheFirstArgument() throws Throwable {
        Function<String, String> function = mock(Function.class);

        when(function.apply(anyString())).then(
            /**/ will(returnsFirstArg())
            /**/.as(String.class)
            /**/.to(String::toUpperCase)
        );

        assertThat(function.apply("first"), equalTo("FIRST"));
    }

    interface AnswerChain<T> extends Answer<T> {

        static <T> AnswerChain<T> will(Answer<T> answer) {
            return answer::answer;
        }

        default <R> AnswerChain<R> as(Class<R> type) {
            return (it) -> type.cast(this.answer(it));
        }

        default <R> AnswerChain<R> to(Function<T, R> mapper) {
            return (it) -> mapper.apply(this.answer(it));
        }
    }

}
