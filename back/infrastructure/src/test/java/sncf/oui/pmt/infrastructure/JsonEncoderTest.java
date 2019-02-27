package sncf.oui.pmt.infrastructure;

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class JsonEncoderTest {

    private final JsonMapEncoder encoder;

    private static final String jsonFlat = "{'key':'value','key2':'value2'}"
            .replace('\'', '"');
    private static final String jsonDeep = "{'key':'value','nested':{'subkey':'subvalue'},'nested2':{'bar':'foo','foo':'bar'}}"
            .replace('\'', '"');
    private static final String jsonDeep2 = "{\"header\":{\"test\":\"test\"},\"pageTitle\":\"bar\",\"welcome\":{\"bijour\":\"foo\",\"title1\":\"value\",\"title2\":\"subvalue\"}}";

    public JsonEncoderTest() {
        encoder = new JsonMapEncoder();
    }

    @Test
    public void testDecodesFlatJson() {
        final Map<String, String> decoded = encoder.decode(Collections.singletonList(jsonFlat)).block();
        assertEquals(decoded.get("key"), "value");
        assertEquals(decoded.get("key2"), "value2");
    }

    @Test
    public void testDecodesDeepJson() {
        final Map<String, String> decoded = encoder.decode(Collections.singletonList(jsonDeep)).block();
        assertEquals(decoded.get("key"), "value");
        assertEquals(decoded.get("nested/subkey"), "subvalue");
    }

    @Test
    public void testEncodesFlatJson() {
        final Map<String, String> decoded = new HashMap<>();
        decoded.put("key", "value");
        decoded.put("key2", "value2");
        final String encoded = encoder.encode(decoded).block()
                .replaceAll("\\s", "");
        assertEquals(jsonFlat, encoded);
    }

    @Test
    public void testEncodesDeepJson() {
        final Map<String, String> decoded = new HashMap<>();
        decoded.put("welcome/title1", "value");
        decoded.put("welcome/title2", "subvalue");
        decoded.put("pageTitle", "bar");
        decoded.put("welcome/bijour", "foo");
        decoded.put("header/test", "test");
        final String encoded = encoder.encode(decoded).block()
                .replaceAll("\\s", "");
        assertEquals(jsonDeep2, encoded);
    }
}
