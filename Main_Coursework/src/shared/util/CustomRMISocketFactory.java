package shared.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.server.RMISocketFactory;

/**
 * {DESCRIPTION}
 *
 * @author Huw Jones
 * @since 14/12/2016
 */
public class CustomRMISocketFactory extends RMISocketFactory {
    /**
     * Creates a client socket connected to the specified host and port.
     *
     * @param host the host name
     * @param port the port number
     * @return a socket connected to the specified host and port.
     * @throws IOException if an I/O error occurs during socket creation
     * @since JDK1.1
     */
    @Override
    public Socket createSocket(String host, int port) throws IOException {
        Socket socket = new Socket();
        //socket.setSoTimeout( 1500 );
        socket.setSoLinger( false, 0 );
        socket.connect( new InetSocketAddress( host, port ), 250 );
        return socket;
    }

    /**
     * Create a server socket on the specified port (port 0 indicates
     * an anonymous port).
     *
     * @param port the port number
     * @return the server socket on the specified port
     * @throws IOException if an I/O error occurs during server socket
     *                     creation
     * @since JDK1.1
     */
    @Override
    public ServerSocket createServerSocket(int port) throws IOException {
        return new ServerSocket( port );
    }
}
