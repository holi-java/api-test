package test.java.patterns;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
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
        static <A, T, B extends Builder<T>, R extends Configurer<T, B>> R mapping(Function<A, R> mapper, A arg) {
            return mapper.apply(arg);
        }

        default EnclosedConfigurer<T, B> enclosing(B builder) {
            Configurer<T, B> delegate = Configurer.this;
            return new EnclosedConfigurer<T, B>(builder) {
                public final void configure(B builder) {
                    delegate.configure(builder);
                }
            };
        }

        void configure(B builder);
    }

    static abstract class EnclosedConfigurer<T, B extends Builder<T>> implements Configurer<T, B> {

        private B builder;

        protected EnclosedConfigurer(B builder) {
            this.builder = builder;
        }

        public final B and() {
            return builder;
        }

        public final B end() {
            return builder;
        }

    }

    abstract static class ConfiguredBuilder<T, B extends Builder<T>> implements Builder<T> {
        private Set<Configurer<T, B>> configs = new HashSet<>();

        public <C extends Configurer<T, B>> ConfiguredBuilder<T, B> with(C config) {
            apply(config);
            return this;
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
            configs.forEach(it -> it.configure((B) this));
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

        public EnclosedConfigurer<User, UserBuilder> named(String name) {
            return apply(Configurer.mapping(UserBuilder::name, name).enclosing(this));
        }


        public EnclosedConfigurer<User, UserBuilder> emailAddress(String email) {
            return apply(Configurer.mapping(UserBuilder::email, email).enclosing(this));
        }


        public AddressEnclosedConfigurer address() {
            return apply(mapping(anAddress()));
        }

        private AddressEnclosedConfigurer mapping(AddressBuilder address) {
            return new AddressEnclosedConfigurer(this);
        }


        private class AddressEnclosedConfigurer extends EnclosedConfigurer<User, UserBuilder> {
            private final AddressBuilder address = anAddress();

            protected AddressEnclosedConfigurer(UserBuilder builder) {
                super(builder);
            }

            public AddressEnclosedConfigurer in(String country) {
                address.in(country);
                return this;
            }

            public AddressEnclosedConfigurer of(String city) {
                address.of(city);
                return this;
            }

            @Override
            public void configure(UserBuilder builder) {
                builder.with(address);
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
