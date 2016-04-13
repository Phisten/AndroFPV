package phisten.androfpv;


import java.io.IOException;
import java.net.ServerSocket;
import java.nio.channels.*;
import java.nio.Buffer.*;


/**
 * Created by Phisten on 2016/4/7.
 */
public class BilateralSocketChannel {
    Selector selector = Selector.open();
    ServerSocketChannel ssc;
    SocketChannel sc;
    DatagramChannel dc;

    Object _Channel;
    String RemoteIP;
    Protocol _Protocol;
    int port;

    public enum Protocol
    {
        TCP,UDP
    }

    public BilateralSocketChannel() throws IOException {

        ssc = ServerSocketChannel.open();
        sc = SocketChannel.open();
        dc = DatagramChannel.open();
        ssc.configureBlocking(false);
        sc.configureBlocking(false);
        dc.configureBlocking(false);
//        SelectionKey key = channel.register(selector,
//                Selectionkey.OP_READ);

    }
    public void Connect(String ip,int port,Protocol protocol)
    {


    }
    public void Bind() throws IOException {
        ssc = ServerSocketChannel.open();

        _Channel = ssc;

    }




}
