package main.api.responses;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.HashMap;

public class GenericResponseObject {
    @JsonAnySetter
    private HashMap<String, Object> properties = new HashMap<>();

    public HashMap<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(HashMap<String, Object> properties) {
        this.properties = properties;
    }

    public void addField(String key, Object value){
        properties.put(key, value);
    }
}