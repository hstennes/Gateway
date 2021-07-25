package com.logic.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JacksonTest {

    public static void main(String[] args){
        Map<String, Object> data = new HashMap<>();
        data.put("first", "Hank");
        data.put("last", "Stennes");
        data.put("age", 101);

        Map<String, Object> pet1 = new HashMap<>();
        pet1.put("type", "cat");
        pet1.put("name", "Ozzy");
        pet1.put("age", 5);

        Map<String, Object> pet2 = new HashMap<>();
        pet2.put("type", "imaginary");
        pet2.put("name", "dogg");
        pet2.put("age", 13);


        List<Map<String, Object>> list = new ArrayList<>();
        list.add(pet1);
        list.add(pet2);

        data.put("pets", list);

        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonResult = mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(data);
            System.out.println(jsonResult);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
