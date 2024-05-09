package org.javlo.component.dynamic;

public class Group {

    private String name = "";
    private int groupSize = 0;
    private int groupNumber = 0;

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
}
