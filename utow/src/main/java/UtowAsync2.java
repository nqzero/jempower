
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.SameThreadExecutor;
import java.nio.ByteBuffer;
import java.util.Arrays;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;

// 9097

public final class UtowAsync2 implements HttpHandler {

// http://lists.jboss.org/pipermail/undertow-dev/2014-August/000898.html

// at 3000 concurrency
// sending while synchronized takes a bunch of reps to jit, but hits 80-84k req/s
// fast-flip 90k req/s

    int num = 0;
    HttpServerExchange [] ac1=new HttpServerExchange[100000], ac2=new HttpServerExchange[100000],
            acv=ac1, copy=ac2;
    byte [] bytes = "hello world".getBytes();
    ByteBuffer buf = ByteBuffer.allocate(bytes.length).put(bytes);
    { buf.flip(); }
    LinkedBlockingQueue<HttpServerExchange []> q = new LinkedBlockingQueue<>();

    synchronized int swap() {
        int n2 = num;
        copy = acv;
        acv = (acv==ac1) ? ac2:ac1;
        num = 0;
        return n2;
    }

    HttpServerExchange [] wrap() {
        int n2 = swap();
        HttpServerExchange a2[]=new HttpServerExchange[n2];
        System.arraycopy(copy,0,a2,0,n2);
        Arrays.fill(copy,0,n2,null);
        return a2;
    }
    synchronized void store(HttpServerExchange async) {
        acv[num++] = async;
    }

    
    
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
        exchange.dispatch(SameThreadExecutor.INSTANCE, () -> store(exchange));
    }

    void reply(HttpServerExchange exchange) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send(buf.duplicate());
    }
    void reply(HttpServerExchange [] wrap) {
        for (int ii=0; ii<wrap.length; ii++)
            reply(wrap[ii]);
    }
    void reply() {
        HttpServerExchange [] wrap = wrap();
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
        Undertow.builder()
                .addHttpListener(9098,"0.0.0.0")
                .setHandler(Handlers.path().addPrefixPath("/hello",new UtowAsync2()))
                .build()
                .start();
    }

}
