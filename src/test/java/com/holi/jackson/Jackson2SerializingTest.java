package com.holi.jackson;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.NopAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.jayway.jsonassert.JsonAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.fasterxml.jackson.annotation.PropertyAccessor.FIELD;
import static com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS;
import static com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter.serializeAllExcept;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Created by holi on 3/20/17.
 */
public class Jackson2SerializingTest {
    private final User bob = new User("bob", "123456", "bob@example.com");
    private final ObjectMapper jackson2 = new ObjectMapper();


    @BeforeEach
    void configure() {
        jackson2.registerModules(configuration());
        //write {} for empty bean
        jackson2.disable(FAIL_ON_EMPTY_BEANS);
        jackson2.setFilterProvider(serializeAll());
        jackson2.setVisibility(FIELD, Visibility.ANY);
    }

    private Module configuration() {
        return new SimpleModule() {
            @Override
            public void setupModule(SetupContext context) {
                context.insertAnnotationIntrospector(filterIdAsClassName());
            }
        };
    }

    private NopAnnotationIntrospector filterIdAsClassName() {
        return new NopAnnotationIntrospector() {
            @Override
            public Object findFilterId(Annotated ann) {
                return ann.getRawType().getName();
            }

            @Override
            public Version version() {
                return super.version();
            }
        };
    }

    private SimpleFilterProvider serializeAll() {
        SimpleFilterProvider filters = new SimpleFilterProvider();
        //serialize all if filter not found,rather than throws an exception
        filters.setFailOnUnknownId(false);
        return filters;
    }

    @Test
    void serializingAll() throws JsonProcessingException {
        String json = jackson2.writer().writeValueAsString(bob);

        JsonAssert.with(json)
                .assertThat("$[*]", hasSize(3))
                .assertEquals("name", bob.name)
                .assertEquals("password", bob.password)
                .assertEquals("mail", bob.mail);
    }

    @Test
    void serializingAllExceptIgnoredProperties() throws JsonProcessingException {
        @JsonIgnoreProperties({"name", "version"})
        class JSONRoot {
            String name = "jackson";
            String version = "2.0";
        }
        JSONRoot root = new JSONRoot();

        String json = jackson2.writer().writeValueAsString(root);

        assertEquals("{}", json);
    }

    @Test
    void serializingAllExceptIgnoredProperty() throws JsonProcessingException {
        class JSONRoot {
            String name = "jackson";
            @JsonIgnore
            String version = "2.0";
        }
        JSONRoot root = new JSONRoot();

        String json = jackson2.writer().writeValueAsString(root);

        JsonAssert.with(json)
                .assertThat("$[*]", hasSize(1))
                .assertEquals("name", root.name);
    }

    @Test
    void serializingAllExceptIgnoredType() throws JsonProcessingException {
        @JsonIgnoreType
        class Ignored {
        }
        class JSONRoot {
            Ignored ignored = new Ignored();
        }

        String json = jackson2.writer().writeValueAsString(new JSONRoot());

        assertEquals("{}", json);
    }

    @Test
    void serializingMixInAnnotations() throws JsonProcessingException {
        jackson2.addMixIn(User.class, ExcludingMail.class);

        String json = jackson2.writer().writeValueAsString(bob);

        JsonAssert.with(json)
                .assertThat("$[*]", hasSize(2))
                .assertEquals("name", bob.name)
                .assertEquals("password", bob.password);
    }

    @Test
    void serializingUseFiltersToExcludingProperties() throws JsonProcessingException {
        String json = jackson2.writer(excludes("password")).writeValueAsString(bob);

        JsonAssert.with(json)
                .assertThat("$[*]", hasSize(2))
                .assertEquals("name", bob.name)
                .assertEquals("mail", bob.mail);
    }

    @Test
    void serializingRootTypePropertiesOnly() throws JsonProcessingException {
        String json = jackson2.writerFor(Contactable.class).writeValueAsString(bob);

        JsonAssert.with(json)
                .assertThat("$[*]", hasSize(1))
                .assertEquals("mail", bob.mail);
    }

    @Test
    void serializingViewPropertiesOnly() throws JsonProcessingException {
        String json = jackson2.writerWithView(Public.class).writeValueAsString(bob);

        JsonAssert.with(json)
                .assertThat("$[*]", hasSize(2))
                .assertEquals("name", bob.name)
                .assertEquals("mail", bob.mail);
    }

    private SimpleFilterProvider excludes(String... propertyNames) {
        SimpleFilterProvider filters = serializeAll();
        filters.addFilter(User.class.getName(), serializeAllExcept(propertyNames));
        return filters;
    }


    private class User implements Contactable {
        final String name;
        @JsonView(Privacy.class)
        String password;
        @JsonView(Public.class)
        final String mail;

        public User(String name, String password, String mail) {
            this.name = name;
            this.password = password;
            this.mail = mail;
        }

        public String getMail() {
            return mail;
        }
    }

    @interface Public {
    }

    @interface Privacy {
    }

    private interface Contactable {
        String getMail();
    }

    @JsonIgnoreProperties("mail")
    private class ExcludingMail {
    }
}
