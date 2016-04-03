package phisten.androfpv;

import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * Created by Phisten on 2016/4/1.
 */
public class SocketPool {
    private static SocketPool ourInstance = new SocketPool();

    public static SocketPool getInstance() {
        return ourInstance;
    }


    public Socket MasterSocket;
    public Socket ListenerSocket;
    public ArrayList<Socket> DataSockets;


    private String RemoteIP;
    protected Handler handler;
    protected Message msg;
    private OutputStream outStream;
    private InputStream inStream;

    private SocketPool() {



    }




//    private Runnable ListenerThread = new Runnable() {// this
//        @Override
//        public void run() {
//            try {
//                s = new Socket();
//                SocketAddress remoteAddr = new InetSocketAddress(IP, port);
//                s.connect(remoteAddr, connect_time);// 連線時間 秒數
//                os = s.getOutputStream();
//                is = s.getInputStream();
//                showMessage(CONNECT_SUCCESSFUL);
//                setInitiativeClose(false);
//                correctTimestamp(is);
//                SendOK = true;
//                while(s != null && is != null)
//                {
//                    TestSendReturn(is);
//                }
//                return;
//            } catch (UnknownHostException e) {// IP不正確
//                showMessage(CONNECT_SERVER_UNKNOWHOST);
//            } catch (IOException e) {// 連不到主機
//                showMessage(CONNECT_SERVER_IOEXEPTION);
//            }
//            s = null;
//            os = null;
//            is = null;
//        }
//    };
}
