package test.springframework.batch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.StepScopeTestExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Created by holi on 4/4/17.
 */
@ContextConfiguration
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, StepScopeTestExecutionListener.class})
@ExtendWith(SpringExtension.class)
public class StepScopeTestExecutionListenerTest {
    @Autowired
    private ItemReader<String> foo;

    public StepExecution initializingStepExecutionWithSpecialContext() {
        ExecutionContext context = new ExecutionContext();
        context.put("foo", "bar");
        return MetaDataInstanceFactory.createStepExecution(context);
    }

    @Test
    void read() throws Throwable {
        assertThat(foo.read(), equalTo("bar"));
    }

    @Configuration
    static class Config {

        @Bean
        StepScope stepScope() {
            return new StepScope();
        }

        @Bean
        @Scope("step")
        public ItemReader<String> foo(@Value("#{stepExecutionContext}") Map<String, String> context) {
            return () -> context.get("foo");
        }
    }

}
