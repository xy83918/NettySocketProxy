package com.ccompass.netty.proxy;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author albert on 10/16/19
 */
@NoArgsConstructor
public enum ServerTypeEnum {


    ZERO(0, "inbound"),
    ONE(1, "第1种类型"),
    TWO(2, "第2种类型"),
    THREE(3, "第3种类型"),
    FOUR(4, "proxy"),
    MAIN(5, "main"),

    ;

    public static final Map<Integer, ServerTypeEnum> keyMap = new ConcurrentHashMap();

    static {
        keyMap.put(ZERO.id, ZERO);
        keyMap.put(ONE.id, ONE);
        keyMap.put(TWO.id, TWO);
        keyMap.put(THREE.id, THREE);
        keyMap.put(FOUR.id, FOUR);
        keyMap.put(MAIN.id, MAIN);
    }

    @Getter
    private String description;
    @Getter
    private Integer id;

    ServerTypeEnum(Integer id, String description) {
        this.id = id;
        this.description = description;
    }

    public static ServerTypeEnum getById(Integer code) {
        return keyMap.get(code);
    }
}
