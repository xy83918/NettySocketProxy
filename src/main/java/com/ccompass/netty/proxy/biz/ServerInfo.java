package com.ccompass.netty.proxy.biz;

import lombok.*;

/**
 * @author albert on 10/16/19
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
public class ServerInfo {

    private String instanceId;
    private ServerTypeEnum serverTypeEnum;
    private String host;
    private Integer port;

}
