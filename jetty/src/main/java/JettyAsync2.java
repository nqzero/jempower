

import java.util.Timer;
import java.util.TimerTask;
import javax.servlet.AsyncContext;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;
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
    AsyncContext [] ac1=new AsyncContext[100000], ac2=new AsyncContext[100000],
            acv=ac1, copy=ac2;
    ByteBuffer helloWorld = BufferUtil.toBuffer("hello world");
    HttpField contentType = new PreEncodedHttpField(HttpHeader.CONTENT_TYPE,MimeTypes.Type.TEXT_PLAIN.asString());
    LinkedBlockingQueue<AsyncContext []> q = new LinkedBlockingQueue<>();

    synchronized int swap() {
        int n2 = num;
        copy = acv;
        acv = (acv==ac1) ? ac2:ac1;
        num = 0;
        return n2;
    }
    
    AsyncContext [] wrap() {
        int n2 = swap();
        AsyncContext a2[]=new AsyncContext[n2];
        System.arraycopy(copy,0,a2,0,n2);
        Arrays.fill(copy,0,n2,null);
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
    void reply(AsyncContext [] wrap) {
        for (int ii=0; ii<wrap.length; ii++)
            reply(wrap[ii]);
    }
    void reply() {
        AsyncContext [] wrap = wrap();
        if (wrap.length==0 || q.add(wrap)) return;
        reply(wrap);
    }
    void poll() {
        try { reply(q.take()); }
        catch (Exception ex) {}
    }

    void timers() {
        int delta = 10, nt = 3;
        new Timer().schedule(new TimerTask() { public void run() {
            reply();
        } },delta,delta);
        
        for (int ii=0; ii<nt; ii++)
            new Thread(()-> { while (true) poll(); }).start();
    }
    { timers(); }
    
    public static void main(String[] args) throws Exception {
        Server server = new Server(9092);
        server.setHandler(new JettyAsync2());
        server.start();
    }

}



