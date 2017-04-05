package test.jackson.stubs;

/**
 * Created by holi on 4/6/17.
 */
public class User {

    public Country[] countries;

    public User() {

    }

    public User(Country[] countries) {
        this.countries = countries;
    }
}
