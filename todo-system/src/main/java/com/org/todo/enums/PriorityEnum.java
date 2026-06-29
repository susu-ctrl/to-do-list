package com.org.todo.enums;

public enum PriorityEnum {
    LOW(0, "低"),
    MEDIUM(1, "中"),
    HIGH(2, "高");

    private final Integer code;
    private final String desc;

    PriorityEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() { return code; }
    public String getDesc() { return desc; }
}