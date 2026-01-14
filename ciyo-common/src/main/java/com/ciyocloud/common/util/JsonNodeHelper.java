package com.ciyocloud.common.util;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

public class JsonNodeHelper {
    private final JsonNode node;

    public JsonNodeHelper(JsonNode node) {
        this.node = node;
    }

    public String getString(String key) {
        JsonNode valueNode = node.get(key);
        return valueNode != null ? valueNode.asText() : null;
    }

    public Integer getInt(String key) {
        JsonNode valueNode = node.get(key);
        return valueNode != null && valueNode.isInt() ? valueNode.asInt() : null;
    }

    public Boolean getBoolean(String key) {
        JsonNode valueNode = node.get(key);
        return valueNode != null ? valueNode.asBoolean() : null;
    }

    public JsonNodeHelper getObject(String key) {
        JsonNode objNode = node.get(key);
        return (objNode != null && objNode.isObject()) ? new JsonNodeHelper(objNode) : null;
    }

    public List<JsonNodeHelper> getArray(String key) {
        JsonNode arrNode = node.get(key);
        List<JsonNodeHelper> list = new ArrayList<>();
        if (arrNode != null && arrNode.isArray()) {
            for (JsonNode item : arrNode) {
                list.add(new JsonNodeHelper(item));
            }
        }
        return list;
    }

    public List<String> getStringArray(String key) {
        JsonNode arrNode = node.get(key);
        List<String> list = new ArrayList<>();
        if (arrNode != null && arrNode.isArray()) {
            for (JsonNode item : arrNode) {
                list.add(item.asText());
            }
        }
        return list;
    }

    public boolean has(String key) {
        return node.has(key);
    }

    public JsonNode getRawNode() {
        return node;
    }
}
