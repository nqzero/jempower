

import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.fibers.servlet.FiberHttpServlet;
import co.paralleluniverse.strands.Strand;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;


// mvn clean package dependency:copy-dependencies -DoutputDirectory=target
// java -cp "target/*" -javaagent:target/quasar-core-0.7.2-jdk8.jar ComsatJetty


// 9096
// duplicate for easy running

public class ComsatJetty {
    private static int delay = 0;

    
    
    public static String doSleep() throws SuspendExecution {
        try {
            if (delay > 0) Strand.sleep(delay);
            return "hello world";
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0) delay = Integer.valueOf(args[0]);
        final Server server = new Server(9096);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        server.setHandler(context);

        context.addServlet(new ServletHolder(new FiberHttpServlet() {
            @Override
            @Suspendable
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                try (PrintWriter out = resp.getWriter()) {
                    try {
                        out.println(doSleep());
                    } catch (SuspendExecution suspendExecution) {
                        new AssertionError(suspendExecution);
                    }
                }
            }
        }), "/hello");
        server.start();
    }

}
