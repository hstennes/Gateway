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

        List<Integer> num1 = new ArrayList<>();
        num1.add(3);
        num1.add(4);

        List<Integer> num2 = new ArrayList<>();
        num2.add(7);
        num2.add(5);

        List<List<Integer>> numbers = new ArrayList<>();
        numbers.add(num1);
        numbers.add(num2);

        data.put("pets", numbers);

        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonResult = mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(data);
            System.out.println(jsonResult);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
    /*
    File format:
    components: [
        {
            id assigned when saving using list index
            type from CompType
            x, y, rotation
            input: [list of id or -1]
        }
    ]
    customs: [
        {
            name

        }
    ]

     */
}
