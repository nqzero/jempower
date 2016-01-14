
// mvn clean package dependency:copy-dependencies -DoutputDirectory=target
// java -cp "target/*" -javaagent:target/quasar-core-0.7.2-jdk8.jar ComsatJetty




import co.paralleluniverse.fibers.Fiber;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.fibers.servlet.FiberHttpServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;


// 9096
// duplicate for easy running

public class ComsatJetty {
    private static int delay = 0;
    private static final byte[] helloWorld = "hello world".getBytes(StandardCharsets.ISO_8859_1);

    public static void main(String[] args) throws Exception {
        if (args.length > 0) delay = Integer.valueOf(args[0]);
        final Server server = new Server(9096);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        server.setHandler(context);

        context.addServlet(new ServletHolder(new FiberHttpServlet() {
            @Override
            @Suspendable
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                try {
                    // from techempower
                    if (delay > 0) Fiber.sleep(delay);
                    resp.setContentType("text/plain");
                    resp.setHeader("Server", "comsat-servlet");
                    resp.getOutputStream().write(helloWorld);
                }
                catch (InterruptedException | SuspendExecution e) {}
            }
        }), "/hello");
        server.start();
    }

}
