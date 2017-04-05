package test.jackson.extension;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.function.Consumer;

import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES;
import static com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES;
import static com.holi.utils.Lambda.curry;

/**
 * Created by holi on 4/6/17.
 */
public class JSONLooselyExtension extends com.holi.utils.FieldInitializationExtension {
    private static final Consumer<ObjectMapper> parsingLoosely = curry(ObjectMapper::enable, ALLOW_UNQUOTED_FIELD_NAMES, ALLOW_SINGLE_QUOTES);

    public JSONLooselyExtension() {
        super(ObjectMapper.class, parsingLoosely);
    }

}
