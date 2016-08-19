import static spark.Spark.*;

public class SparkHello {
    public static void main(String[] args) {
	port(9099);
        get("/hello", (request, response) -> {
            return "hello world";
        });
    }
}
