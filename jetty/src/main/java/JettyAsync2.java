

import java.util.Timer;
import java.util.TimerTask;
import javax.servlet.AsyncContext;


// 9091 async
// 9092 not async

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.http.PreEncodedHttpField;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.BufferUtil;

public class JettyAsync2 extends AbstractHandler {
    int num = 0;
    AsyncContext acv[] = new AsyncContext[1000000];
    ByteBuffer helloWorld = BufferUtil.toBuffer("hello world");
    HttpField contentType = new PreEncodedHttpField(HttpHeader.CONTENT_TYPE,MimeTypes.Type.TEXT_PLAIN.asString());

    synchronized AsyncContext [] wrap() {
        AsyncContext [] a2 = new AsyncContext[num];
        System.arraycopy(acv,0,a2,0,num);
        num = 0;
        return a2;
    }
    
    synchronized void store(AsyncContext async) {
        acv[num++] = async;
    }

    
    
    
    public void handle(String target,Request br,HttpServletRequest request,HttpServletResponse response) {
        final AsyncContext async = request.startAsync();
        async.setTimeout(30000);
        store(async);
    }
    void reply(AsyncContext async) {
        try {
            Request br = (Request) async.getRequest();
            br.setHandled(true);
            br.getResponse().getHttpFields().add(contentType); 
            if ("/hello".equals(br.getPathInfo()))
                br.getResponse().getHttpOutput().sendContent(helloWorld.slice());
            async.complete();
        } catch (IOException ex) {}
    }
    void reply() {
        for (AsyncContext async : wrap())
            reply(async);
    }
    
    public static void main(String[] args) throws Exception {
        JettyAsync2 rest = new JettyAsync2();
        
        Server server = new Server(9092);
        server.setHandler(rest);
        server.start();

        int delta = 1, nt = 3;
        for (int ii=0; ii < nt; ii++)
            new Timer().schedule(new TimerTask() { public void run() {
                rest.reply();
            } },delta,delta);
    }

}



