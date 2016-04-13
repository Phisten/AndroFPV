package phisten.androfpv;

import java.io.Serializable;

/**
 * Created by Phisten on 2016/4/3.
 */
public class Packet implements Serializable {
    /**
     *
     */
    public enum PacketType
    {
        //DetecterChannel Packet
//        EchoRequest(1), //探索同網段內可連接的伺服器
//        Ping(0), //回報本機在線
        MasterChannelConnectRequest(10), //請求連接MasterChannel | 來源IP(4byte)
        MasterChannelWattingForConnect(11), //告知MasterChannel等待連接中

        //MasterChannel Packet
        SendByteStream(20), //請求傳送位元串流
        SendByteStreamByDataSocket(21), //請求傳送位元串流

        //MasterChannel Packet For DataSockets management
        DataChannelConnectRequest(30), //請求連接DataChannel
        DataChannelWattingForConnect(11), //告知DataChannel等待連接中

        ;

        private final int value;
        private PacketType(int num) {
            value = num;
        }
        public byte toByte(){
            return (byte)value;
        }
        public int toInt(){
            return value;
        }

    }



}
