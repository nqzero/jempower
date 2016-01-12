import static spark.Spark.*;

// default port is 4567
// mvn dependency:copy-dependencies -DoutputDirectory=OUTPUT_DIR

public class SparkHello {
    public static void main(String[] args) {
        get("/hello", (request, response) -> {
            return "hello world";
        });
    }
}
