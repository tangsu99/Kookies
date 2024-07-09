package org.kookies.mirai.pojo.entity.api.response.joke;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author General_K1ng
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SingleResponse implements JokeResponse, Serializable {
    private boolean error;
    private String category;
    private String type;
    private String joke;
    private String lang;
}