
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import kilim.Pausable;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

// arg0 is the delay in msec
// java -cp target/\* -Dkilim.class.path=target/jempower-kilim-jetty-0.2.jar KilimJetty 1

public class KilimJetty extends HttpServlet {
    final static String RESULTS_ATTR = "org.eclipse.jetty.demo.client";
    private static int delay = 0;

    public static class Task extends kilim.Task {
        AsyncContext ac;
        String val;
        public void execute() throws Pausable {
            if (delay > 0) Task.sleep(delay);
            val = "hello world";
            ac.dispatch();
            ac = null;
        }
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        Object results = request.getAttribute(RESULTS_ATTR);

        if (results==null) {
            Task task = new Task();
            request.setAttribute(RESULTS_ATTR,task);
            task.ac = request.startAsync();
            task.ac.setTimeout(30000);
            task.start();
            return;
        }
        Task task = (Task) results;
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println(task.val);
        out.close();
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doGet(request, response);
    }
    
    
    public static void main(String[] args) throws Exception
    {
        if (kilim.tools.Kilim.trampoline(false,args)) return;

        if (args.length > 0) delay = Integer.valueOf(args[0]);

        KilimJetty rest = new KilimJetty();
        
        Server server = new Server(9089);
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        ServletHolder holder = new ServletHolder(rest);
        context.addServlet(holder,"/hello");
        holder.setAsyncSupported(true);
        server.setHandler(context);
        server.start();
    }

}



