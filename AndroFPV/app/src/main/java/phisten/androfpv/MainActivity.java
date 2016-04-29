package phisten.androfpv;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    static final String LogTag_Error = "Error";


    SocketChannelPool scp = SocketChannelPool.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //取得IP 寫入資訊列
        String localIP = getMyIp();
        Log.i("Info", "curIP: " + getMyIp());
        scp.LocalIP = localIP;
        SetTextViewText(R.id.textView_LocalIP,R.string.textview_LocalIP,localIP);


        //UI Init
        Button DectectBtn = (Button) findViewById(R.id.DectectBtn);
        DectectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //發出探索封包,請求IP回報
                SocketChannelPool scp;
                byte[] localIpBytes;
                try {
                    scp = SocketChannelPool.getInstance();

                    //本機IP轉換為網段下廣播
                    String localIP = scp.LocalIP;
                    localIpBytes =  scp.HostIpConvertToBytes(localIP);
                    localIpBytes[3] = 0;
                    String broadcastIP = scp.HostIpConvertToString(localIpBytes);


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Button ConnectBtn = (Button) findViewById(R.id.ConnectBtn);
        ConnectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //向指定IP請求masterCh連接

            }
        });



    }
    //取得IP
    private String getMyIp(){
        //新增一個WifiManager物件並取得WIFI_SERVICE
        WifiManager wifi_service = (WifiManager)getSystemService(WIFI_SERVICE);

        //取得wifi資訊
        WifiInfo wifiInfo = wifi_service.getConnectionInfo();
        //取得IP，換算
        int ipAddress = wifiInfo.getIpAddress();
        //利用位移運算和AND運算計算IP
        String ip = String.format("%d.%d.%d.%d",(ipAddress & 0xff),(ipAddress >> 8 & 0xff),(ipAddress >> 16 & 0xff),(ipAddress >> 24 & 0xff));
        return ip;
    }

    //設定資訊列內的文字,titleId輸入0可僅顯示text
    public int SetTextViewText(int textViewId, int titleId , String text)
    {
        try {
            TextView tv = (TextView) findViewById(textViewId);
            if (titleId > 0) {
                tv.setText(getString(titleId) + " :" + text);
            } else {
                tv.setText(text);
            }
        }catch (Exception ex) {
            Log.e(LogTag_Error, "SetTextViewText: textViewId Error");
        }
        return 0;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
