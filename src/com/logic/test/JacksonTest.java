package com.logic.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logic.components.LComponent;
import com.logic.files.JSONFile;
import com.logic.ui.CircuitPanel;

import java.util.List;

public class JacksonTest {

    public static void main(String[] args){
        /*
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

        int[][] pogNumbers = new int[][] {new int[] {1, 2, 3}, new int[] {4, 5, 6}};
        data.put("pets", pogNumbers);

        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonResult = mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(data);
            System.out.println(jsonResult);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        try {
            testObjectThing();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
         */
    }

    public static void testSave(List<LComponent> lcomps) {
        JSONFile file = new JSONFile(lcomps);
        String jsonResult = null;
        try {
            jsonResult = new ObjectMapper().writerWithDefaultPrettyPrinter()
                    .writeValueAsString(file);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        System.out.println(jsonResult);
    }

    public static void testLoad(CircuitPanel cp){
        String str = "{\n" +
                "  \"components\" : [ {\n" +
                "    \"type\" : \"SWITCH\",\n" +
                "    \"x\" : 288,\n" +
                "    \"y\" : 79,\n" +
                "    \"name\" : \"Untitled component\",\n" +
                "    \"com\" : \"No comments\",\n" +
                "    \"input\" : [ ]\n" +
                "  }, {\n" +
                "    \"type\" : \"SWITCH\",\n" +
                "    \"x\" : 507,\n" +
                "    \"y\" : 72,\n" +
                "    \"rot\" : 1,\n" +
                "    \"name\" : \"Untitled component\",\n" +
                "    \"com\" : \"No comments\",\n" +
                "    \"input\" : [ ],\n" +
                "    \"state\" : true\n" +
                "  }, {\n" +
                "    \"type\" : \"NOR\",\n" +
                "    \"x\" : 750,\n" +
                "    \"y\" : 82,\n" +
                "    \"name\" : \"Untitled component\",\n" +
                "    \"com\" : \"No comments\",\n" +
                "    \"input\" : [ [ ], [ ] ]\n" +
                "  }, {\n" +
                "    \"type\" : \"CLOCK\",\n" +
                "    \"x\" : 368,\n" +
                "    \"y\" : 297,\n" +
                "    \"name\" : \"Untitled component\",\n" +
                "    \"com\" : \"No comments\",\n" +
                "    \"input\" : [ ],\n" +
                "    \"delay\" : 300\n" +
                "  } ]\n" +
                "}";
        try {
            JSONFile file = new ObjectMapper().readValue(str, JSONFile.class);
            System.out.println(file.components[1].type + " " + file.components[1].state);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }



    /*
    File format:
    components: [
        {
            id assigned when saving using list index
            type from CompType OR customID
            x, y, rotation
            name, comments (if present?)
            connect: [
                [id, output_number, signal]
            ]

            state (just for switch?)
            delay (just for clock)
        }
    ]
    customs: [
        {
            typeID
            label
            components: [
                {
                    Same as above
                }
            ]
        }
    ]
     */
}
