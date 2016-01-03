
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Timer;
import java.util.TimerTask;
import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;


// 9091 async
// 9092 not async

public class JettyAsync extends HttpServlet
{
    final static String RESULTS_ATTR = "org.eclipse.jetty.demo.client";
    final boolean useasync;

    int num = 0;
    AsyncContext acv[] = new AsyncContext[10000];

    public JettyAsync(boolean async) {
        useasync = async;
    }

    
    
    synchronized void store(AsyncContext async) {
        if (async==null)
            while (num > 0) {
                acv[--num].dispatch();
                acv[num] = null;
            }
        else acv[num++] = async;
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        Object results = request.getAttribute(RESULTS_ATTR);

        if (useasync && results==null)
        {
            request.setAttribute(RESULTS_ATTR, new Object());
            final AsyncContext async = request.startAsync();
            async.setTimeout(30000);
            store(async);
            return;
        }
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("hello world async jetty");
        out.close();
    }
    

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doGet(request, response);
    }
    
    static Server setup(int port,boolean async) throws Exception {
        JettyAsync rest = new JettyAsync(async);
        Timer timer = new Timer();
        if (rest.useasync) timer.schedule(new TimerTask() { public void run() {
            rest.store(null);
        } },10,10);
        
        Server server = new Server(port);
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        ServletHolder holder = new ServletHolder(rest);
        context.addServlet(holder,"/hello");
        holder.setAsyncSupported(rest.useasync);
        server.setHandler(context);
        server.start();
        return server;
    }
    
    public static void main(String[] args) throws Exception
    {
        if (args.length==0) setup(9091,true);
        else                setup(9092,false);
    }

}



