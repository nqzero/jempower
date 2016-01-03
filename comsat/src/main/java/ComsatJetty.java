

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


// 9096
// duplicate for easy running

public class ComsatJetty {
    
    public static String doSleep() throws SuspendExecution {
        try {
            if (false) Strand.sleep(1);
            return "hello world";
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void main(String[] args) throws Exception {
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
        System.out.println("http://localhost:9096/hello");
        System.out.println("Jetty started. Hit enter to stop it...");
        System.in.read();
        server.stop();
    }

}
