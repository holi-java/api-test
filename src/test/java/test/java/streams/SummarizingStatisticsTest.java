package test.java.streams;

import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Created by holi on 5/21/17.
 */
public class SummarizingStatisticsTest {

    static/**/
    class Product {
        public static final int UNASSIGNED_ID = -1;
        public final String name;
        public final String category;
        public final String type;
        public final int id;

        public Product(int id, String name, String category, String type) {
            this.name = name;
            this.category = category;
            this.type = type;
            this.id = id;
        }

        public static Product of(String name, String category, String type) {
            return new Product(UNASSIGNED_ID, name, category, type);
        }

        //todo: implements equals() & hashCode() for Map key
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

        //for test purpose
        @Override
        public String toString() {
            return String.format("%s-%s-%s", name, category, type);
        }
    }

    static/**/
    class Item {
        public final Product product;
        public final double cost;
        public final int qty;

        public Item(Product product, double cost, int qty) {
            this.product = product;
            this.cost = cost;
            this.qty = qty;
        }

        public Item add(Item that) {
            if (!Objects.equals(this.product, that.product)) {
                throw new IllegalArgumentException("Can't be add items of diff products!");
            }
            return of(product, this.cost + that.cost, this.qty + that.qty);
        }

        public static Item of(Product product, double cost, int qty) {
            return new Item(product, cost, qty);
        }

        //for test purpose
        @Override
        public boolean equals(Object obj) {
            Item that = (Item) obj;
            return Arrays.asList(this.product, this.cost, this.qty).equals(
                    Arrays.asList(that.product, that.cost, that.qty)
            );
        }

        //for test purpose
        @Override
        public String toString() {
            return String.format("%s - qty: %d, cost: %.2f", product, qty, cost);
        }
    }

    @Test
    void mergesItemsWithSameProduct() throws Throwable {
        List<Item> items = from(
        /**/"prod1       cat2     t1      100.23\n" +
        /**/"prod2       cat1     t2      50.23\n" +
        /**/"prod1       cat1     t3      200.23\n" +
        /**/"prod3       cat2     t1      150.23\n" +
        /**/"prod1       cat2     t1      100.23"
        );

        Collection<Item> summarized = items.stream().collect(Collectors.toMap(
                it -> it.product,
                Function.identity(),
                Item::add
        )).values();

        assertThat(summarized, containsInAnyOrder(
                Item.of(Product.of("prod1", "cat1", "t3"), 200.23, 1),
                Item.of(Product.of("prod1", "cat2", "t1"), 200.46, 2),
                Item.of(Product.of("prod2", "cat1", "t2"), 50.23, 1),
                Item.of(Product.of("prod3", "cat2", "t1"), 150.23, 1)
        ));
    }

    private List<Item> from(String items) {
        return Stream.of(items.split("\n"))
                .map(it -> it.split("\\s+"))
                .map(fields -> Stream.of(fields).map(String::trim).toArray(String[]::new))
                .map(fields -> Item.of(Product.of(fields[0], fields[1], fields[2]), Double.parseDouble(fields[3]), 1))
                .collect(Collectors.toList());
    }
}
