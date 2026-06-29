package com.org.todo.enums;

public enum TaskStatusEnum {
    PENDING(0, "待办"),
    IN_PROGRESS(1, "进行中"),
    COMPLETED(2, "已完成"),
    OVERDUE(3, "已逾期");

    private final Integer code;
    private final String desc;

    TaskStatusEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() { return code; }
    public String getDesc() { return desc; }
}