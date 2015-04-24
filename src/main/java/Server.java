import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.jcabi.http.Request;
import com.jcabi.http.Response;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.wire.RetryWire;

import java.io.*;
import java.util.*;

import static spark.Spark.*;

public class Server {
    static class Buzz { String name, msg; }

    static List<String> bees = new ArrayList<>();
    static List<Buzz> buzzes = new ArrayList<>();

    static Gson gson = new Gson();

    public static void main(String... args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(Server.class.getResourceAsStream("gcm.key")));
        String gcmkey = br.readLine();
        br.close();

        get("/buzzes", (req, res) -> {
            res.type("application/json");
            return buzzes;
        }, gson::toJson);

        put("/newBuzz", "application/json", (req, res) -> {
            Buzz newBuzz = gson.fromJson(req.body(), Buzz.class);
            if (buzzes.stream().anyMatch(candidate -> candidate.msg.equals(newBuzz.msg))) halt(400);
            buzzes.add(newBuzz);
            pushNotif(newBuzz, gcmkey);
            res.status(200);
            return "";
        });

        exception(JsonSyntaxException.class, (e, req, res) -> res.status(400));
    }

    static void pushNotif(Buzz b, String gcmkey) {
        class GCMMessage { // JSON data
            List<String> registration_ids = bees;
            Buzz data;
            int time_to_live = 30; // seconds
            //boolean delay_while_idle = true;
        }

        if (!bees.isEmpty())
            new Thread(() -> {
                GCMMessage gcm = new GCMMessage();
                gcm.data = b;
                try {
                    Response res = new JdkRequest("https://android.googleapis.com/gcm/send")
                            .through(RetryWire.class)
                            .method(Request.POST)
                            .header("Authorization", "key=" + gcmkey)
                            .header("Content-Type", "application/json")
                            .body().set(gson.toJson(gcm)).back()
                            .fetch();
                    assert (res.status() == 200) :
                            String.format("GCM response was %d: %s\n%s\n", res.status(), res.reason(), res.body());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
    }
}
