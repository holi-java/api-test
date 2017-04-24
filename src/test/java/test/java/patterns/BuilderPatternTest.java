package test.java.patterns;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static test.java.patterns.BuilderPatternTest.AddressBuilder.anAddress;
import static test.java.patterns.BuilderPatternTest.UserBuilder.*;

/**
 * Created by holi on 4/24/17.
 */
public class BuilderPatternTest {

    @Test
    void standard() throws Throwable {
        User user = building(anUser().withName("bob").withEmail("bob@example.com").with(anAddress().in("CN").of("GuangZhou")));

        assertThat(user.name, equalTo("bob"));
        assertThat(user.email, equalTo("bob@example.com"));
        assertThat(user.address, equalTo(Address.of("CN", "GuangZhou")));
    }

    @Test
    void composingConfigurers() throws Throwable {
        User user = building(anUser().with(name("bob")).with(email("bob@example.com")).with(address("CN", "GuangZhou")));

        assertThat(user.name, equalTo("bob"));
        assertThat(user.email, equalTo("bob@example.com"));
        assertThat(user.address, equalTo(Address.of("CN", "GuangZhou")));
    }

    @Test
    void chainingConfigurers() throws Throwable {
        User user = building(anUser().named("bob").and().emailAddress("bob@example.com").and().address().in("CN").of("GuangZhou").end());

        assertThat(user.name, equalTo("bob"));
        assertThat(user.email, equalTo("bob@example.com"));
        assertThat(user.address, equalTo(Address.of("CN", "GuangZhou")));
    }


    private <T> T building(Builder<T> builder) {
        return builder.build();
    }

    interface Builder<T> {
        T build();
    }

    interface Configurer<T, B extends Builder<T>> {
        void configure(B builder);

    }

    static abstract class BacktrackingConfigurer<T, B extends Builder<T>> implements Configurer<T, B> {

        private B builder;

        protected BacktrackingConfigurer(B builder) {
            this.builder = builder;
        }

        public final B and() {
            return builder;
        }

        public final B end() {
            return builder;
        }

    }

    static class BacktrackingConfigurerAdapter<T, B extends Builder<T>> extends BacktrackingConfigurer<T, B> {

        private Configurer<T, B> configurer;

        protected BacktrackingConfigurerAdapter(B builder, Configurer<T, B> configurer) {
            super(builder);
            this.configurer = configurer;
        }

        public final void configure(B builder) {
            configurer.configure(builder);
        }

    }

    abstract static class ConfiguredBuilder<T, B extends Builder<T>> implements Builder<T> {
        private Set<Configurer<T, B>> configs = new HashSet<>();

        public <C extends Configurer<T, B>> ConfiguredBuilder<T, B> with(C config) {
            apply(config);
            return this;
        }

        public <A, R extends Configurer<T, B>> R apply(Function<A, R> mapper, A arg) {
            return apply(mapper.apply(arg));
        }

        public <C extends Configurer<T, B>> C apply(C config) {
            configs.add(config);
            return config;
        }

        final public T build() {
            configure();
            return doBuild();
        }

        private void configure() {
            for (Configurer<T, B> config : configs) {
                config.configure((B) this);
            }
        }

        protected abstract T doBuild();
    }

    static class UserBuilder extends ConfiguredBuilder<User, UserBuilder> {
        private String name;
        private String email;
        private Builder<Address> address;

        public static Configurer<User, UserBuilder> name(String name) {
            return it -> it.withName(name);
        }

        public static Configurer<User, UserBuilder> email(String email) {
            return it -> it.withEmail(email);
        }

        public static Configurer<User, UserBuilder> address(String country, String city) {
            return it -> it.with(anAddress().in(country).of(city));
        }

        public static UserBuilder anUser() {
            return new UserBuilder();
        }

        @Override
        protected User doBuild() {
            return new User(name, email, address == null ? null : address.build());
        }

        public UserBuilder withName(String name) {
            this.name = name;
            return this;
        }

        public UserBuilder withEmail(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder with(Builder<Address> address) {
            this.address = address;
            return this;
        }

        public BacktrackingConfigurerAdapter<User, UserBuilder> named(String name) {
            return handle(apply(UserBuilder::name, name));
        }


        public BacktrackingConfigurerAdapter<User, UserBuilder> emailAddress(String email) {
            return handle(apply(UserBuilder::email, email));
        }


        private BacktrackingConfigurerAdapter<User, UserBuilder> handle(Configurer<User, UserBuilder> configurer) {
            return apply(new BacktrackingConfigurerAdapter<>(this, configurer));
        }

        public AddressBacktrackingConfigurer address() {
            return apply(new AddressBacktrackingConfigurer(this, UserBuilder::address));
        }


        private class AddressBacktrackingConfigurer extends BacktrackingConfigurer<User, UserBuilder> {
            private final BiFunction<String, String, Configurer<User, UserBuilder>> configurerMapper;
            private String country;
            private String city;

            protected AddressBacktrackingConfigurer(UserBuilder builder, BiFunction<String, String, Configurer<User, UserBuilder>> configurerMapper) {
                super(builder);
                this.configurerMapper = configurerMapper;
            }

            public AddressBacktrackingConfigurer in(String country) {
                this.country = country;
                return this;
            }

            public AddressBacktrackingConfigurer of(String city) {
                this.city = city;
                return this;
            }

            @Override
            public void configure(UserBuilder builder) {
                configurerMapper.apply(country, city).configure(builder);
            }
        }
    }

    public static class AddressBuilder extends ConfiguredBuilder<Address, AddressBuilder> {
        private String country;
        private String city;

        public static AddressBuilder anAddress() {
            return new AddressBuilder();
        }

        @Override
        public Address doBuild() {
            return Address.of(country, city);
        }

        public AddressBuilder in(String country) {
            this.country = country;
            return this;
        }

        public AddressBuilder of(String city) {
            this.city = city;
            return this;
        }

    }

    static class User {
        String name;
        String email;
        Address address;

        public User(String name, String email, Address address) {
            this.name = name;
            this.email = email;
            this.address = address;
        }
    }

    static class Address {
        private final String country;
        private final String city;

        private Address(String country, String city) {
            this.country = country;
            this.city = city;
        }

        @Override
        public boolean equals(Object obj) {
            Address that = (Address) obj;
            return Objects.equals(city, that.city) && Objects.equals(country, that.country);
        }

        @Override
        public String toString() {
            return city + ", " + country;
        }

        public static Address of(String country, String city) {
            return new Address(country, city);
        }
    }
}
