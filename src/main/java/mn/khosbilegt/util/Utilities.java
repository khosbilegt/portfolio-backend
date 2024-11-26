package mn.khosbilegt.util;

import io.vertx.core.json.JsonObject;

public class Utilities {
    public static JsonObject createSuccessResponse() {
        return new JsonObject().put("status", "Success");
    }
}
