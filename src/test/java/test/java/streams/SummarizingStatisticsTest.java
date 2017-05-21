package test.java.streams;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Created by holi on 5/21/17.
 */
public class SummarizingStatisticsTest {

    static/**/
    class Product {
        public String name;
        public String category;
        public String type;
        public int id;

        public static Product from(String name, String category, String type) {
            Product it = new Product();
            it.name = name;
            it.category = category;
            it.type = type;
            return it;
        }

        @Override
        public boolean equals(Object obj) {
            Product that = (Product) obj;
            return Arrays.asList(name, category, type).equals(
                    Arrays.asList(that.name, that.category, that.type)
            );
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name, category, type);
        }

        @Override
        public String toString() {
            return String.format("%s-%s-%s", name, category, type);
        }
    }

    static/**/
    class Item {
        public Product product;
        public double cost;

        public static Item from(Product product, double cost) {
            Item it = new Item();
            it.product = product;
            it.cost = cost;
            return it;
        }
    }

    @Test
    void summarizing() throws Throwable {
        List<Item> items = from(
        /**/"prod1       cat2     t1      100.23\n" +
        /**/"prod2       cat1     t2      50.23\n" +
        /**/"prod1       cat1     t3      200.23\n" +
        /**/"prod3       cat2     t1      150.23\n" +
        /**/"prod1       cat2     t1      100.23"
        );
        Map<Product, DoubleSummaryStatistics> stat = items.stream().collect(Collectors.groupingBy(
                it -> it.product,
                Collectors.summarizingDouble(it -> it.cost)
        ));


        String[] output = stat.entrySet().stream().map(it -> String.format("%s - count: %d, total cost: %.2f"
                , it.getKey(), it.getValue().getCount(), it.getValue().getSum())).toArray(String[]::new);


        assertThat(output, arrayContainingInAnyOrder(
                "prod1-cat1-t3 - count: 1, total cost: 200.23",
                "prod1-cat2-t1 - count: 2, total cost: 200.46",
                "prod2-cat1-t2 - count: 1, total cost: 50.23",
                "prod3-cat2-t1 - count: 1, total cost: 150.23"
        ));
    }

    private List<Item> from(String items) {
        List<Item> result = new ArrayList<>();
        for (String item : items.split("\n")) {
            String[] fields = Arrays.stream(item.split("\\s+")).map(String::trim).toArray(String[]::new);
            Product product = Product.from(fields[0], fields[1], fields[2]);
            result.add(Item.from(product, Double.parseDouble(fields[3])));
        }
        return result;
    }
}
