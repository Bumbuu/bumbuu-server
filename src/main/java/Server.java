import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.*;

import static spark.Spark.*;

public class Server {
    static List<Buzz> buzzes = new ArrayList<>();
    static Gson gson = new Gson();
    static class Buzz { String name, msg; }

    public static void main(String... args) {
        get("/buzzes", (req, res) -> {
            res.type("application/json");
            return buzzes;
        }, gson::toJson);

        put("/newBuzz", "application/json", (req, res) -> {
            try {
                Buzz b = gson.fromJson(req.body(), Buzz.class);
                if (buzzes.stream().anyMatch(a -> a.msg.equals(b.msg))) // robot9000
                    res.status(400);
                else {
                    buzzes.add(b);
                    res.status(200);
                }
            } catch (JsonSyntaxException e) {
                res.status(400);
            }
            return "";
        });
    }
}
