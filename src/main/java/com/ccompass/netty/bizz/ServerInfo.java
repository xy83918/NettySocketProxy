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
    private String host;
    private String path;
    private Integer port;
    private ServiceTypeEnum serviceTypeEnum;

}
