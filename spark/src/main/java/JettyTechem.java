


import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.MimeTypes;
import org.eclipse.jetty.http.PreEncodedHttpField;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.BufferUtil;
import java.io.IOException;
import java.nio.ByteBuffer;


// 9090
// simplified by srl based on the techem
// performance appears to be identical
// 91k-ish req/s

/**
 * An implementation of the TechEmpower benchmark tests using the Jetty web
 * server.  
 */
public final class JettyTechem 
{
    public static void main(String[] args) throws Exception
    {
        Server server = new Server(9090);
        ServerConnector connector = server.getBean(ServerConnector.class);
        HttpConfiguration config = connector.getBean(HttpConnectionFactory.class).getHttpConfiguration();
        config.setSendDateHeader(true);
        config.setSendServerVersion(true);

        PathHandler pathHandler = new PathHandler();
        server.setHandler(pathHandler);

        server.start();
        server.join();
    }
    
    public static class PathHandler extends AbstractHandler
    {
        ByteBuffer helloWorld = BufferUtil.toBuffer("Hello, World!");
        HttpField contentType = new PreEncodedHttpField(HttpHeader.CONTENT_TYPE,MimeTypes.Type.TEXT_PLAIN.asString());

        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
        {
            baseRequest.setHandled(true);
            baseRequest.getResponse().getHttpFields().add(contentType); 
            if ("/hello".equals(target))
                baseRequest.getResponse().getHttpOutput().sendContent(helloWorld.slice());
        }
        
    }
}
