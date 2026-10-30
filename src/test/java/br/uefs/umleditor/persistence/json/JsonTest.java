package br.uefs.umleditor.persistence.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class JsonTest {

    @Test
    void readsAllValueTypes() throws JsonException {
        Object value = JsonParser.parse("{\"a\": [1, -2.5, true, false, null], \"b\": {\"c\": \"x\"}, \"d\": 3e2}");
        Map<?, ?> map = (Map<?, ?>) value;
        assertEquals(List.of(1L, -2.5, true, false), ((List<?>) map.get("a")).subList(0, 4));
        assertEquals(null, ((List<?>) map.get("a")).get(4));
        assertEquals(Map.of("c", "x"), map.get("b"));
        assertEquals(300.0, map.get("d"));
    }

    @Test
    void readsEscapes() throws JsonException {
        assertEquals("a\"b\\c\n\t\u00ABd\u00BB/", JsonParser.parse("\"a\\\"b\\\\c\\n\\t\\u00abd\u00BB\\/\""));
    }

    @Test
    void writtenTextIsReadBack() throws JsonException {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("text", "line 1\nline \"2\" \\ \u00ABcreate\u00BB");
        data.put("number", 12.0);
        data.put("fraction", 0.25);
        data.put("flag", true);
        data.put("list", List.of(1, 2, 3));
        data.put("objects", List.of(Map.of("id", 0), Map.of()));
        data.put("empty", List.of());
        String json = JsonWriter.write(data);
        Map<?, ?> read = (Map<?, ?>) JsonParser.parse(json);
        assertEquals(data.get("text"), read.get("text"));
        assertEquals(12L, read.get("number"));
        assertEquals(0.25, read.get("fraction"));
        assertEquals(List.of(1L, 2L, 3L), read.get("list"));
        assertEquals(List.of(), read.get("empty"));
        assertEquals(JsonWriter.write(read), json);
    }

    @Test
    void rejectsMalformedText() {
        assertThrows(JsonException.class, () -> JsonParser.parse("{\"a\": }"));
        assertThrows(JsonException.class, () -> JsonParser.parse("[1, 2"));
        assertThrows(JsonException.class, () -> JsonParser.parse("{} extra"));
        assertThrows(JsonException.class, () -> JsonParser.parse("\"\\x\""));
    }
}
