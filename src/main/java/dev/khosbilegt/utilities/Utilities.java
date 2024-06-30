package dev.khosbilegt.utilities;

import io.vertx.core.json.JsonObject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

public class Utilities {
    public static void validateMissingParams(String source, JsonObject jsonObject, List<String> requiredParams) {
        for (String param : requiredParams) {
            if (!jsonObject.containsKey(param)) {
                throw new BadRequestException("Missing required parameter for [" + source + "]: '" + param + "'");
            }
        }
    }

    public static Response successResponse() {
        return Response.ok().entity(
                        new JsonObject()
                                .put("status", "SUCCESS"))
                .build();
    }

    public static Response successResponse(JsonObject jsonObject) {
        return Response.ok().entity(jsonObject).build();
    }

}
