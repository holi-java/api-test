package test;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Objects;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created by holi on 5/29/17.
 */
public class MakingCommunicationProtocolsTest {
    final Publisher publisher = new Publisher();
    final Subscriber subscriber = mock(Subscriber.class);

    @Test
    void receivesDataWhenEventDataWasChanged() throws Throwable {
        publisher.subscribe(subscriber);

        publisher.publish("foo");

        verify(subscriber).onReceive(eq("foo"));
    }

    @Test
    void donotReceivesDataWhenEventDataWasNotChanged() throws Throwable {
        publisher.subscribe(subscriber);
        publisher.publish("foo");
        clearInvocations(subscriber);

        publisher.publish("foo");
        verifyNoMoreInteractions(subscriber);
    }

    private class Publisher {
        private Subscriber subscriber;
        private String last;

        public void subscribe(Subscriber subscriber) {
            this.subscriber = subscriber;
        }

        public void publish(String data) {
            if (Objects.equals(last, data)) {
                return;
            }
            last = data;
            subscriber.onReceive(data);
        }
    }

    private interface Subscriber {
        void onReceive(String data);
    }
}
