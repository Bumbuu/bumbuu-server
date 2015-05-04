package bumbuu.server;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.jcabi.http.Request;
import com.jcabi.http.Response;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.wire.RetryWire;

import java.io.*;
import java.util.*;

import static spark.Spark.*;

import bumbuu.server.Model.*;

public class Server {
    public static final String JSON_MIME = "application/json";
    static List<String> users = new ArrayList<>();
    static List<Post> posts = new ArrayList<>();
    static Gson gson = new Gson();

    public static void main(String... args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(Server.class.getResourceAsStream("/gcm.key")));
        String gcmkey = br.readLine();
        br.close();

        get("/posts", (req, res) -> {
            res.type(JSON_MIME);
            return posts;
        }, gson::toJson);

        post("/newPost", JSON_MIME, (req, res) -> {
            Post p = gson.fromJson(req.body(), Post.class);
            if (posts.stream().anyMatch(x -> x.msg.equals(p.msg)))
                halt(403);
            posts.add(p);
            asyncTellGCM(p, gcmkey);
            res.status(201);
            return "ok";
        });

        post("/newUser", JSON_MIME, (req, res) -> {
            User u = gson.fromJson(req.body(), User.class);
            if (u.registration_id.isEmpty())
                halt(400);
            users.add(u.registration_id);
            res.status(201);
            return "ok";
        });

        exception(JsonSyntaxException.class, (e, req, res) -> res.status(400));
    }

    static void asyncTellGCM(Post p, String gcmkey) {
        if (!users.isEmpty())
            new Thread(() -> {
                try {
                    Response res = new JdkRequest("https://android.googleapis.com/gcm/send")
                            .through(RetryWire.class)
                            .method(Request.POST)
                            .header("Authorization", "key=" + gcmkey)
                            .header("Content-Type", "application/json")
                            .body().set(gson.toJson(new GCMMessage(users, p))).back()
                            .fetch();
                    assert (res.status() == 200) :
                            String.format("GCM response was %d: %s\n%s\n", res.status(), res.reason(), res.body());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
    }
}
