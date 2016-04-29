package phisten.androfpv;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

//import java.io.*;


/**
 * Created by Phisten on 2016/4/1.
 */
public class SocketChannelPool {
    private static SocketChannelPool ourInstance = new SocketChannelPool();
    public static SocketChannelPool getInstance() {
        return ourInstance;
    }

    //Channel
    public ServerSocketChannel MasterServerChannel;
    public SocketChannel MasterChannel;
    public DatagramChannel DetecterChannel;
    public int DataChannelCount = 3;
    public ArrayList<DatagramChannel> DataChannels;

    //Connect Info
    public String LocalIP;
    InetSocketAddress RemoteISA;
    private String RemoteIP;
    protected Handler handler;
    protected Message msg;
    static final int MasterServerChannelPort = 27390;
    static final int DetecterChannelPort = 27380;

    private SocketChannelPool() {
        DataChannels = new ArrayList<>();
        try {
            MasterServerChannel = ServerSocketChannel.open();
            MasterChannel = SocketChannel.open();
            DetecterChannel = DatagramChannel.open();
            MasterServerChannel.socket().bind(new InetSocketAddress(SocketChannelPool.MasterServerChannelPort));
            DetecterChannel.socket().bind(new InetSocketAddress(SocketChannelPool.DetecterChannelPort));

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Runnable SocketChannelRun = new Runnable() {
        @Override
        public void run() {

        }
    };
    private Runnable SocketChannelRun2 = new Runnable() {
        @Override
        public void run() {

        }
    };



    public int SetRemoteIP(String host)
    {
        //TODO 驗證IP格式是否正確
        RemoteIP = host;
        return 0;
    }



    public int DetectServer() throws Exception {
        //Init DetectPacket Buffer
        ByteBuffer detectPacketBuf = ByteBuffer.allocate(5);
        detectPacketBuf.clear();
        detectPacketBuf.put(Packet.PacketType.MasterChannelConnectRequest.toByte());
        //填入本地IP
        detectPacketBuf.put(HostIpConvertToBytes(LocalIP));
        detectPacketBuf.flip();

        //TODO 廣播探索封包

        Log.d("test", "DetectServer: " +  Inet4Address.getLocalHost().toString());

        DetecterChannel.send(detectPacketBuf,InetSocketAddress.createUnresolved("192.168.0.255",DetecterChannelPort));

        return 0;
    }


    //將字串表示的IP轉換為4組byte表示的陣列
    public byte[] HostIpConvertToBytes(String hostIp) throws Exception {
        String[] ipStrArr = hostIp.split(".");
        if (ipStrArr.length != 4) {
            throw new Exception("func: HostIpConvertToBytes IP Input Error(String[] ipStrArr.length != 4)");
        }
        byte[] bs = new byte[4];
        for (int i = 0; i < 4; i++) {
            bs[i] = Byte.parseByte(ipStrArr[i]);
        }
        return bs;
    }
    //將4組byte之陣列所表示的IP轉換為字串表示
    public String HostIpConvertToString(byte[] hostIp) throws Exception {
        if (hostIp.length != 4) {
            throw new Exception("func: HostIpConvertToString IP Input Error(byte[] hostIp.length != 4)");
        }
        String str1 = String.format("%b.%b.%b.%b",hostIp[0],hostIp[1],hostIp[2],hostIp[3]);
        Log.d("UnitTest",str1);
        return str1;
    }


}
