package com.instabtbu.controler;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import app.akexorcist.bluetoothspp.BluetoothSPP;
import app.akexorcist.bluetoothspp.BluetoothSPP.BluetoothConnectionListener;
import app.akexorcist.bluetoothspp.BluetoothSPP.OnDataReceivedListener;
import app.akexorcist.bluetoothspp.BluetoothState;
import app.akexorcist.bluetoothspp.DeviceList;
public class MainActivity extends Activity {
BluetoothSPP bt;
	
	TextView textRead,textStatus;
	Menu menu;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		textRead = (TextView)findViewById(R.id.textView_rec );
		textStatus = (TextView)findViewById(R.id.textView_state);
		bt = new BluetoothSPP(this);

		if(!bt.isBluetoothAvailable()) {
			Toast.makeText(getApplicationContext()
					, "蓝牙不可用"
					, Toast.LENGTH_SHORT).show();
            finish();
		}
		

		
		bt.setBluetoothConnectionListener(new BluetoothConnectionListener() {
			public void onDeviceDisconnected() {
				textStatus.setText("状态:未连接");
				menu.clear();
				getMenuInflater().inflate(R.menu.connection, menu);
				
			}
			
			public void onDeviceConnectionFailed() {
				textStatus.setText("状态:连接失败");
			}
			
			public void onDeviceConnected(String name, String address) {
				textStatus.setText("状态:已连接上" + name);
				menu.clear();
				getMenuInflater().inflate(R.menu.disconnection, menu);
			}
		});
		
		bt.setOnDataReceivedListener(new OnDataReceivedListener() {
			public void onDataReceived(byte[] data, String message) {
				textRead.setText(message);
			}
		});
		
	}
	
	public void send(View v)
	{
		Button button = (Button)v;
		String sendString = button.getText().toString();
			if(bt.getServiceState()==BluetoothState.STATE_CONNECTED)
			{
			bt.send(sendString, true);
			}else {
				xuanze();
			}
	}
	 
	public void xuanze()
	{
		bt.setDeviceTarget(BluetoothState.DEVICE_OTHER);
		Intent intent = new Intent(getApplicationContext(), DeviceList.class);
		intent.putExtra("bluetooth_devices", "蓝牙设备");
		intent.putExtra("no_devices_found", "没有发现设备");
		intent.putExtra("scanning", "搜索中");
		intent.putExtra("scan_for_devices", "搜索");
		intent.putExtra("select_device", "选择");
		startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
	}
	public boolean onCreateOptionsMenu(Menu menu) {
		this.menu = menu;
		getMenuInflater().inflate(R.menu.connection, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == R.id.menu_device_connect) {
			xuanze();  
		} else if(id == R.id.menu_disconnect) {
			if(bt.getServiceState() == BluetoothState.STATE_CONNECTED)
    			bt.disconnect();
		}
		return super.onOptionsItemSelected(item);
	}

	public void onDestroy() {
        super.onDestroy();
        bt.stopService();
    }
	
	public void onStart() {
        super.onStart();
        if (!bt.isBluetoothEnabled()) {
        	Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if(!bt.isServiceAvailable()) { 
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
                setup();
            }
        }
    }
	
	public void setup() {
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
			if(resultCode == Activity.RESULT_OK)
                bt.connect(data);
		} else if(requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if(resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_ANDROID);
                setup();
            } else {
                Toast.makeText(getApplicationContext()
                		, "Bluetooth was not enabled."
                		, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}  