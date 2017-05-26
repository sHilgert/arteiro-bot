package bot;

import static spark.Spark.*;

import java.util.*;

public class MainServer {
    public static void main(String[] args) {

        // Get port config of heroku on environment variable
        ProcessBuilder process = new ProcessBuilder();
        Bot bot = new Bot();
        int myPort;
        if (process.environment().get("PORT") != null) {
            myPort = Integer.parseInt(process.environment().get("PORT"));
        } else {
            myPort = 8080;
        }
        port(myPort);

        //Data is sent by telegram API on this route
        post("/readMessages", (req, res) -> {
            bot.read(req.bodyAsBytes());
            return "Success";
        });

    }
}