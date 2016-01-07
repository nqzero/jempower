
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import java.nio.ByteBuffer;
import io.undertow.util.SameThreadExecutor;

import java.util.Timer;
import java.util.TimerTask;

// 9097

public final class UtowAsync2 implements HttpHandler {

  public static void main(String[] args) throws Exception {
    Undertow.builder()
        .addHttpListener(9098,"0.0.0.0")
        .setHandler(Handlers.path().addPrefixPath("/hello",new UtowAsync2()))
        .build()
        .start();
  }

// http://lists.jboss.org/pipermail/undertow-dev/2014-August/000898.html

// at 3000 concurrency
// sending while synchronized takes a bunch of reps to jit, but hits 80-84k req/s
// fast-flip 90k req/s

    int num = 0;
    HttpServerExchange acv[] = new HttpServerExchange[100000];

    synchronized HttpServerExchange [] wrap() {
        HttpServerExchange [] a2 = new HttpServerExchange[num];
        System.arraycopy(acv,0,a2,0,num);
        num = 0;
        return a2;
    }
    synchronized void store(HttpServerExchange async) {
        acv[num++] = async;
    }

    byte [] bytes = "hello world".getBytes();
    ByteBuffer buf = ByteBuffer.allocate(bytes.length).put(bytes);
    { buf.flip(); }
    
    
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
        exchange.dispatch(SameThreadExecutor.INSTANCE, () -> store(exchange));
    }

    void reply(HttpServerExchange exchange) {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send(buf.duplicate());
    }
    void reply() {
        HttpServerExchange [] wrap = wrap();
        for (HttpServerExchange exchange : wrap)
            reply(exchange);
    }
    
    {
        int delta = 1, nt = 3;
        for (int ii=0; ii < nt; ii++)
            new Timer().schedule(new TimerTask() { public void run() {
                UtowAsync2.this.reply();
            } },delta,delta);
    }
}
