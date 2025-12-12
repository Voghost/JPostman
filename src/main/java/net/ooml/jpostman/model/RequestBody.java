package net.ooml.jpostman.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.ooml.jpostman.model.enums.BodyType;

/**
 * Request body model
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestBody {

    @Builder.Default
    private BodyType type = BodyType.NONE;

    private String content; // Raw content (JSON, XML, plain text)

    public static RequestBody createEmpty() {
        return RequestBody.builder()
                .type(BodyType.NONE)
                .build();
    }

    public static RequestBody createJson(String jsonContent) {
        return RequestBody.builder()
                .type(BodyType.JSON)
                .content(jsonContent)
                .build();
    }

    public static RequestBody createRaw(String rawContent) {
        return RequestBody.builder()
                .type(BodyType.RAW)
                .content(rawContent)
                .build();
    }
}
