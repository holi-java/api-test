package test.mockito;

import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.util.Optional;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static test.mockito.AnswerPipelineTest.AnswerPipeline.will;

/**
 * Created by holi on 4/27/17.
 */
class AnswerPipelineTest {

    @Test
    void transformsTheReturnedValue() throws Throwable {
        Function<String, Optional> function = mock(Function.class);

        when(function.apply(anyString())).then(will(returnsFirstArg()).as(String.class).to(Optional::of));

        assertThat(function.apply("first"), equalTo(Optional.of("first")));
    }

    interface AnswerPipeline<T> extends Answer<T> {

        static <R> AnswerPipeline<R> will(Answer<R> answer) {
            return answer::answer;
        }

        default <R> AnswerPipeline<R> as(Class<R> type) {
            return to(type::cast);
        }

        default <R> AnswerPipeline<R> to(Function<T, R> mapper) {
            return it -> mapper.apply(answer(it));
        }
    }

}
