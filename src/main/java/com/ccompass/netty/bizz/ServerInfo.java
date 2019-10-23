package com.ccompass.netty.bizz;

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
    private ServiceTypeEnum serviceTypeEnum;
    private String host;
    private Integer port;

}
