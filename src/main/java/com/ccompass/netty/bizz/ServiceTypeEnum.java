package com.ccompass.netty.bizz;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author albert on 10/16/19
 */
@NoArgsConstructor
public enum ServiceTypeEnum {


    ZERO(0, "inbound"),
    ONE(1, "第1种类型"),
    TWO(2, "第2种类型"),
    THREE(3, "第3种类型"),
    FOUR(4, "proxy"),
    MAIN(5, "main"),

    ;

    public static final Map<Integer, ServiceTypeEnum> keyMap = new ConcurrentHashMap();

    static {
        keyMap.put(ZERO.getId(), ZERO);
        keyMap.put(ONE.getId(), ONE);
        keyMap.put(TWO.getId(), TWO);
        keyMap.put(THREE.getId(), THREE);
        keyMap.put(FOUR.getId(), FOUR);
        keyMap.put(MAIN.getId(), MAIN);
    }

    @Getter
    private String description;
    @Getter
    private Integer id;

    ServiceTypeEnum(Integer id, String description) {
        this.id = id;
        this.description = description;
    }

    public static ServiceTypeEnum getById(Integer code) {
        return keyMap.get(code);
    }
}
