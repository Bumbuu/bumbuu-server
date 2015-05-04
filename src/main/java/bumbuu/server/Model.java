package bumbuu.server;

import java.util.List;

public class Model {
    static class Post {
        String name, msg;

        public Post(String name, String msg) {
            this.name = name;
            this.msg = msg;
        }
    }

    static class User {
        String registration_id;
    }

    static class GCMMessage {
        List<String> registration_ids;
        Post data;
        int time_to_live = 30; // seconds
        //boolean delay_while_idle = true;

        public GCMMessage(List<String> registration_ids, Post data) {
            this.registration_ids = registration_ids;
            this.data = data;
        }
    }
}
