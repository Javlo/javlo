package org.javlo.component.dynamic;

import org.javlo.fields.Field;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Group {

    private String name = "";
    private int groupSize = 0;
    private int groupNumber = 0;

    private List<Integer> groupNumberList = new ArrayList<>();

    private Map<String, Field> fields = new HashMap<>();
    private Map<String, Field> fieldsForDisplay = new HashMap<>();

    public Group(String name, int groupSize, int groupNumber) {
        this.name = name;
        this.groupSize = groupSize;
        this.groupNumber = groupNumber;

    }

    public String getName() {
        return name;
    }

    public int getGroupSize() {
        return groupSize;
    }

    public int getGroupNumber() {
        return groupNumber;
    }

    public Map<String,Field> getFields() {
        return fields;
    }

    public Map<String, Field> getFieldsForDisplay() {
        return fieldsForDisplay;
    }

    public List<Integer> getGroupNumberList() {
        return groupNumberList;
    }
}


