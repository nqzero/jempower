
import java.io.EOFException;
import java.io.IOException;

import kilim.Pausable;
import kilim.http.HttpRequest;
import kilim.http.HttpResponse;
import kilim.http.HttpServer;
import kilim.http.HttpSession;

// 9093
// sudo bash -c "ulimit -n 102400; su -mc '$JAVA_HOME/bin/java -cp \"dist/*\" -Xmx1024M tools.Hello'"
// sudo bash -c "ulimit -n 102400; ab -n 1000000 -k -c 4000 localhost:8083/hello" 
// 71k req/s

public class KilimHello extends HttpSession {
    byte [] bytes = "hello world".getBytes();
    
    public static void main(String[] args) throws IOException {
        new HttpServer(9093, KilimHello.class);
    }
    
    public void execute() throws Pausable, Exception {
        try {
            HttpRequest req = new HttpRequest();
            HttpResponse resp = new HttpResponse();
            while (true) {
                super.readRequest(req);
                if (req.keepAlive())
                    resp.addField("Connection", "Keep-Alive");
                resp.getOutputStream().write(bytes);
                sendResponse(resp);
                if (!req.keepAlive()) 
                    break;
            }
        }
        catch (EOFException e) {}
        super.close();
    }
}
