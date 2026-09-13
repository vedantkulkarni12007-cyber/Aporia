package com.aporia.knowledge.json;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class MiniJsonTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testParseObject() {
        String json = "{\"name\":\"Aporia\", \"version\":1.0, \"active\":true}";
        Map<String, Object> map = (Map<String, Object>) MiniJson.parse(json);
        
        assertEquals("Aporia", map.get("name"));
        assertEquals(1.0, (Double) map.get("version"), 0.001);
        assertTrue((Boolean) map.get("active"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testParseArray() {
        String json = "[1, \"two\", false, null]";
        List<Object> list = (List<Object>) MiniJson.parse(json);
        
        assertEquals(4, list.size());
        assertEquals(1.0, (Double) list.get(0), 0.001);
        assertEquals("two", list.get(1));
        assertFalse((Boolean) list.get(2));
        assertNull(list.get(3));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testNestedStructures() {
        String json = "{\"pages\": [{\"title\": \"Black Hole\", \"id\": 123}]}";
        Map<String, Object> root = (Map<String, Object>) MiniJson.parse(json);
        List<Object> pages = (List<Object>) root.get("pages");
        Map<String, Object> firstPage = (Map<String, Object>) pages.get(0);
        
        assertEquals("Black Hole", firstPage.get("title"));
        assertEquals(123.0, (Double) firstPage.get("id"), 0.001);
    }
    
    @Test
    public void testStringEscaping() {
        // Test parsing JSON strings containing escaped quotes and unicode
        String json = "{\"text\": \"He said \\\"Hello\\\". \\u2605\"}";
        @SuppressWarnings("unchecked")
        Map<String, Object> root = (Map<String, Object>) MiniJson.parse(json);
        assertEquals("He said \"Hello\". \u2605", root.get("text"));
    }

    @Test
    public void testNullOrEmpty() {
        assertNull(MiniJson.parse(null));
        assertNull(MiniJson.parse("   "));
    }
}
