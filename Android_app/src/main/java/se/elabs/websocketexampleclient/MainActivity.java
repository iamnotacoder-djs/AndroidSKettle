package se.elabs.websocketexampleclient;

import android.app.Activity;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

public class MainActivity extends Activity {
    private WebSocketClient mWebSocketClient;

    public int status = 0;
    public int temperature = 0;
    public int waterlevel = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectWebSocket();

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            MainActivity xxx = (MainActivity)getActivity();
            TextView textview = (TextView) rootView.findViewById(R.id.textView3);
            textview.setText(xxx.temperature + "°");
            textview = (TextView) rootView.findViewById(R.id.textView9);
            textview.setText(xxx.waterlevel + "L");

            Button switch1 = (Button) rootView.findViewById(R.id.switch1);
            switch1.setText("Вкл/Выкл");

            Button switch2 = (Button) rootView.findViewById(R.id.switch2);

            switch2.setText("Вкл/Выкл");

            return rootView;
        }
    }

    private void connectWebSocket() {
        URI uri;
        try {
            uri = new URI("ws://10.1.79.50:3001");
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return;
        }

        mWebSocketClient = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                Log.i("Websocket", "Opened");

                mWebSocketClient.send("{\n" +
                        "   \"type\": \"clientTypeInfo\",\n" +
                        "   \"value\": \"app\"}");
            }

            @Override
            public void onMessage(String message) {
                final String s = message;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject obj = null;
                        try {
                            obj = new JSONObject(s);
                            String tmp = obj.getString("type");
                            Log.i("Websockets", tmp);
                            if (tmp.equals("info")) {
                                //obj = new JSONObject(tmp);
                                //mWebSocketClient.send("dasdasd");
                                temperature = Integer.parseInt(obj.getString("temp"));
                                waterlevel = Integer.parseInt(obj.getString("water"));

                                TextView textView3 = (TextView) findViewById(R.id.textView3);
                                TextView textView9 = (TextView) findViewById(R.id.textView9);
                                textView3.setText(temperature + "°");
                                textView9.setText((waterlevel==1?"0.5":(waterlevel==2?1:(waterlevel==3?"1.7":0))) + "L");

                            } else if (tmp.equals("status")) {

                                status = Integer.parseInt(obj.getString("value"));
                                TextView textView2 = (TextView) findViewById(R.id.textView2);
                                textView2.setText("Статус: " + (status == 0 ? "Чайник не подключен к серверу" : (status == 1 ? "Режим ожидания" : (status == 2 ? "Процесс подогрева воды" : "Режим автоподогрева"))));

                            }

                        } catch (JSONException e) {
                            //e.printStackTrace();
                        }
                        //TextView textView = (TextView)findViewById(R.id.messages);
                        //textView.setText(textView.getText() + "\n" + message);
                    }
                });


            }
            /*
            @Override
            public void onMessage(String s) {
                final String message = s;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //TextView textView = (TextView)findViewById(R.id.messages);
                        //textView.setText(textView.getText() + "\n" + message);
                    }
                });
            }*/

            @Override
            public void onClose(int i, String s, boolean b) {
                Log.i("Websocket", "Closed " + s);
            }

            @Override
            public void onError(Exception e) {
                Log.i("Websocket", "Error " + e.getMessage());
            }
        };
        mWebSocketClient.connect();
    }

    public void sendMessage(View view) {
        //EditText editText = (EditText)findViewById(R.id.message);
        //mWebSocketClient.send(editText.getText().toString());
        //editText.setText("");
    }

    public void but1(View view) {               Log.i("123123123", "hi");
        if (status==1){
            SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar2);
            mWebSocketClient.send("{\"type\": \"power\"," +
                    "\"value\":\"1\"," +
                    "\"temp\":\""+(seekBar.getProgress()*6/10+40)+"\"}");

        }else if (status == 2) {
            mWebSocketClient.send("{\"type\": \"power\"," +
                    "\"value\":\"0\"}");

        }else if (status ==3){
            mWebSocketClient.send("{\"type\": \"power\"," +
                    "\"value\":\"0\"}");
        }


    }

    public void but2(View view) {
        Log.i("123123123", "hi");
        if (status==1){
            SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar2);
            mWebSocketClient.send("{\"type\": \"power\"," +
                    "\"value\":\"2\"," +
                    "\"temp\":\""+(seekBar.getProgress()*6/10+40)+"\"}");

        }else if (status == 2) {
            SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar2);
            mWebSocketClient.send("{\"type\": \"power\"," +
                    "\"value\":\"0\"}");
            mWebSocketClient.send("{\"type\": \"power\",\" +\n" +
                    "                    \"\"value\":\"2\",\" +\n" +
                    "                    \"\"temp\":\""+(seekBar.getProgress()*6/10+40)+"\"}");

        }else if (status ==3){
            mWebSocketClient.send("{\"type\": \"power\"," +
                    "\"value\":\"0\"}");
        }
    }

}
