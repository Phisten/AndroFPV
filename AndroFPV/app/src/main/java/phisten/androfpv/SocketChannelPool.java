package phisten.androfpv;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


/**
 * Created by Phisten on 2016/4/1.
 */
public class SocketChannelPool {
    static public SocketChannelPool getInstance() {
        return ourInstance;
    }
    static private SocketChannelPool ourInstance = new SocketChannelPool();

    //Channel
    private Selector MasterThreadSelector;
    //private SelectionKey MasterThreadSelectionKey;
    private ServerSocketChannel MasterServerChannel;
    private SocketChannel MasterChannel;
    private DatagramChannel DetecterChannel;
    private int DataChannelCount = 3;
    private ArrayList<DatagramChannel> DataChannels;

    //Connect Info
    public boolean isServer = true;
    public String LocalIP;
    InetSocketAddress RemoteISA;
    private String RemoteIP;
    protected Message msg;
    static final int MasterServerChannelPort = 27390;
    static final int DetecterChannelPort = 27380;


    public Handler UiHandler;
    static public final int HANDLERMSG_SHOWTEXT = 1;
    static public final int HANDLERMSG_SET_TARGET_IP = 2;
    static public final int HANDLERMSG_ACCEPT = 3;

    private SocketChannelPool() {
        DataChannels = new ArrayList<>();
        try {
            MasterServerChannelInit();

            DetecterChannel = DatagramChannel.open();
            DetecterChannel.socket().bind(new InetSocketAddress(SocketChannelPool.DetecterChannelPort));
            DetecterChannel.configureBlocking(false);

            MasterThreadSelector = Selector.open();

            SelectionKey k1 = MasterServerChannel.register(MasterThreadSelector,SelectionKey.OP_ACCEPT);
            SelectionKey k3 = DetecterChannel.register(MasterThreadSelector,SelectionKey.OP_READ);

//            Log.d(LogTag.Test, "SocketChannelPool: MasterChannelThread TryStart");
            MasterChannelThread = new Thread(MasterChannelRunnable);
            MasterChannelThread.start();
//            Log.d(LogTag.Test, "SocketChannelPool: MasterChannelThread Start");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    // ThreadControl --------------------------------------------------------------------
//    Handler MasterChannelHandler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//        }
//    };
    Thread MasterChannelThread;
    private Runnable MasterChannelRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(LogTag.Test, "run: MasterChannelThread Start");
            while(true) {
                //send Queue --------------------------------------------------------

                //recive Event ------------------------------------------------------
                int readyChannels = 0;
                try {
                    readyChannels = MasterThreadSelector.select();

                    //Log.d(LogTag.Test, "readyChannels=" + readyChannels);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(readyChannels == 0) continue;
                Set selectedKeys = MasterThreadSelector.selectedKeys();
                Iterator keyIterator = selectedKeys.iterator();
                while(keyIterator.hasNext()) {
                    SelectionKey key = (SelectionKey) keyIterator.next();
                    if (key.channel().equals(DetecterChannel))
                    {
                        if(key.isAcceptable()) {
                            // a connection was accepted by a ServerSocketChannel.
                        } else if (key.isConnectable()) {
                            // a connection was established with a remote server.
                        } else if (key.isReadable()) {
                            //接收資料進byteBuf
                            ByteBuffer byteBuf = ByteBuffer.allocate(5);
                            byteBuf.clear();
                            try {
                                SocketAddress srcSocketAddr = DetecterChannel.receive(byteBuf);
                                Log.d(LogTag.OutputTest, "run: srcSocketAddr = " + srcSocketAddr.toString());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            byteBuf.flip();

                            //將資料由byteBuf讀出  第一位元為PacketType,用於分辨資料處理方式
                            byte[] srcIp = new byte[4];
                            byte PacketType = byteBuf.get();
                            if (PacketType == Packet.PacketType.MasterChannelConnectRequest.toByte())
                            {
                                Log.d(LogTag.OutputTest, "PacketType == MasterChannelConnectRequest");
                                byteBuf.get(srcIp);

                                String srcIPstr = HostIpConvertToString(srcIp);
                                UiHandler.sendMessage(UiHandler.obtainMessage(HANDLERMSG_SET_TARGET_IP,srcIPstr));
                            }
                        }
                    }else if (key.channel().equals(MasterChannel))
                    {
                        if(key.isConnectable()) {
                            // a connection was established with a remote server.
                        } else if (key.isReadable()) {

                        } else if (key.isWritable()) {
                            // a channel is ready for writing
                        }
                    }else if (key.channel().equals(MasterServerChannel))
                    {
                        if(key.isAcceptable()) {
                            try {
                                if (MasterChannel != null){
                                    MasterChannel.close();
                                }
                                MasterChannel = MasterServerChannel.accept();
                                SelectionKey k2 = MasterChannel.register(MasterThreadSelector,SelectionKey.OP_READ | SelectionKey.OP_WRITE | SelectionKey.OP_CONNECT);
                                UiHandler.sendMessage(UiHandler.obtainMessage(HANDLERMSG_ACCEPT,"accept"));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else if (key.isConnectable()) {
                            // a connection was established with a remote server.
                        }
                    }



                    keyIterator.remove();
                }
            }



        }
    };
//    private Runnable ReceiveRunnable = new Runnable() {
//        @Override
//        public void run() {
//
//        }
//    };
    private Runnable SendRunnable = new Runnable() {
        @Override
        public void run() {

        }
    };


    // public method --------------------------------------------------------------------
    public int MasterServerChannelInit()
    {
        try {
            MasterServerChannel = ServerSocketChannel.open();
            MasterServerChannel.socket().bind(new InetSocketAddress(SocketChannelPool.MasterServerChannelPort));
            MasterServerChannel.configureBlocking(false);
            isServer = true;

            if (MasterChannel != null && MasterChannel.isOpen()) {
                MasterChannel.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public int MasterChannelConnect(String hostIp) {
        try {
            if (MasterChannel != null && MasterChannel.isOpen()) {
                MasterChannel.close();
            }
            MasterChannel = SocketChannel.open();
            MasterChannel.configureBlocking(false);
            SelectionKey k2 = MasterChannel.register(MasterThreadSelector,SelectionKey.OP_READ | SelectionKey.OP_WRITE | SelectionKey.OP_CONNECT);
            isServer = false;
            if (MasterServerChannel != null && MasterServerChannel.isOpen()) {
                MasterServerChannel.close();
            }

            Log.d(LogTag.OutputTest, "MasterChannelConnect: " + hostIp + ":" + MasterServerChannelPort);
            MasterChannel.connect(new InetSocketAddress(hostIp,MasterServerChannelPort));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
    // ReceiveData ----------------------------------------------------------------------
    // SendData -------------------------------------------------------------------------
    public int DetectServer(String targetIP) throws Exception {
        Log.d(LogTag.OutputTest, "DetectServer: " +  targetIP + " , local:" + LocalIP);
        Thread t = new Thread(new Runnable() {
            String targetIP;
            public Runnable init(String targetIP) {
                this.targetIP = targetIP;
                return(this);
            }
            @Override
            public void run() {
                try {
                    //Init DetectPacket Buffer
                    ByteBuffer detectPacketBuf = ByteBuffer.allocate(5);
                    detectPacketBuf.clear();
                    detectPacketBuf.put(Packet.PacketType.MasterChannelConnectRequest.toByte());
                    //填入本地IP
                    detectPacketBuf.put(SocketChannelPool.HostIpConvertToBytes(LocalIP));
                    //Log.d(LogTag.Test, "run: " + HostIpConvertToString(HostIpConvertToBytes(LocalIP)));
                    detectPacketBuf.flip();

                    //發出封包
                    InetSocketAddress inetSocketAddr = new InetSocketAddress(targetIP,DetecterChannelPort);
                    int bytesSent = DetecterChannel.send(detectPacketBuf,inetSocketAddr);

                    Log.d(LogTag.OutputTest,"DetectServer: bytesSent = " + String.format("%d",bytesSent));
                }catch(Exception e) {
                    Log.e(LogTag.Error,e.getMessage());
                }finally {
                    Log.d(LogTag.OutputTest,"DetectServer: Thread End");
                }
            }
        }.init(targetIP));
        t.start();
        return 0;
    }




    // Info GetSet --------------------------------------------------------------------
    public int SetRemoteIP(String host) throws Exception {
        if (validIP(host))
        {
            RemoteIP = host;
            return Succeed;
        }
        throw new Exception("IP規格不符");
        //return UndefinedError;
    }


    // IP Convert --------------------------------------------------------------------
    public static boolean validIP(String ip) {
        if (ip == null || ip.isEmpty()) return false;
        ip = ip.trim();
        if ((ip.length() < 6) & (ip.length() > 15)) return false;

        try {
            Pattern pattern = Pattern.compile("^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$");
            Matcher matcher = pattern.matcher(ip);
            return matcher.matches();
        } catch (PatternSyntaxException ex) {
            return false;
        }
    }
    /*將字串表示的IP轉換為4個int組成的陣列*/
    static public int[] HostIpConvertToInts(String hostIp){
        String[] ipStrArr = hostIp.split("\\.");
        int[] ints = new int[4];
        if (ipStrArr.length != 4) {
            Log.e(LogTag.Error, "HostIpConvertToInts:  IP Input Error(String[] ipStrArr.length != 4)", new Exception("func: HostIpConvertToInts IP Input Error(String[] ipStrArr.length == " + ipStrArr.length + " != 4 ) hostIp = " + hostIp.toString()));
            return ints;
        }
        for (int i = 0; i < 4; i++) {
            ints[i] = Integer.parseInt(ipStrArr[i]);
        }
        return ints;
    }
    /*將4個int組成之陣列所表示的IP轉換為字串表示*/
    static public String HostIpConvertToString(int[] hostIp){
        if (hostIp.length != 4) {
            Log.e(LogTag.Error, "HostIpConvertToInts:  IP Input Error(int[] hostIp.length != 4)", new Exception("func: HostIpConvertToString IP Input Error(byte[] hostIp.length == " + hostIp.length + " != 4)"));
        }
        String str1 = String.format("%d.%d.%d.%d",hostIp[0],hostIp[1],hostIp[2],hostIp[3]);
        Log.d(LogTag.OutputTest,str1);
        return str1;
    }
    /*將4個byte組成之陣列所表示的IP轉換為字串表示*/
    static public String HostIpConvertToString(byte[] hostIp){
        if (hostIp.length != 4) {
            Log.e(LogTag.Error, "HostIpConvertToInts:  IP Input Error(byte[] hostIp.length != 4)", new Exception("func: HostIpConvertToString IP Input Error(byte[] hostIp.length == " + hostIp.length + " != 4)"));
        }
        int intConverter = 0;
        String str1 = String.format("%d.%d.%d.%d",intConverter | hostIp[0],intConverter | hostIp[1],intConverter | hostIp[2],intConverter | hostIp[3]);
        Log.d(LogTag.OutputTest,str1);
        return str1;
    }
    /*將字串表示的IP轉換為4組byte組成的陣列*/
    static public byte[] HostIpConvertToBytes(String hostIp){
        String[] ipStrArr = hostIp.split("\\.");
        if (ipStrArr.length != 4) {
            Log.e(LogTag.Error, "HostIpConvertToInts:  IP Input Error(String[] ipStrArr.length != 4)"
            , new Exception("func: HostIpConvertToInts IP Input Error(String[] ipStrArr.length == " + ipStrArr.length + " != 4 ) hostIp = " + hostIp.toString()));
        }
        byte[] bs = new byte[4];
        for (int i = 0; i < 4; i++) {
            bs[i] = Byte.parseByte(ipStrArr[i]);
        }
        return bs;
    }
    /*將4個int組成之陣列所表示的IP轉換為byte陣列*/
    static public byte[] HostIpConvertToBytes(int[] hostIp){
        if (hostIp.length != 4) {
            Log.e(LogTag.Error, "HostIpConvertToInts:  IP Input Error(int[] hostIp.length != 4)"
                    , new Exception("func: HostIpConvertToString IP Input Error(int[] hostIp.length == " + hostIp.length + " != 4)"));
        }
        byte[] bs = new byte[4];
        for (int i = 0; i < 4; i++) {
            bs[i] = (byte)(hostIp[i] & 0xff);
        }
        return bs;
    }

    // return Code --------------------------------------------------------------------
    static public final int Succeed = 0;
    static public final int UndefinedError = -1;


}
