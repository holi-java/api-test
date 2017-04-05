package test.jackson.stubs;

import java.util.Arrays;
import java.util.Objects;

/**
 * Created by holi on 4/6/17.
 */
public class Country {
    public static Country[] arrayOf(String... countries) {
        return Arrays.stream(countries).map(Country::valueOf).toArray(Country[]::new);
    }

    public static Country valueOf(String country) {
        Country it = new Country();
        it.country = country;
        return it;
    }

    public String country;

    public boolean equals(Object o) {
        Country that = (Country) o;
        return Objects.equals(country, that.country);
    }

}
