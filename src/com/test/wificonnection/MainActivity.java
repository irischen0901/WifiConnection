package com.test.wificonnection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.test.wificonnection.R;

import android.R.integer;
import android.R.string;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;

// ref. https://github.com/mobilemerit/WIFI
//only for WPA_PSK
public class MainActivity extends Activity  implements OnClickListener{ 
	
	Button setWifi;
	WifiManager wifiManager;
	WifiReceiver receiverWifi;
	List<ScanResult> wifiLists;
	List<String> listOfProvider;
	List<WifiConfiguration> mWifiConfigurations;
	ListAdapter adapter;
	ListView listViwProvider;
	private EditText mPassword;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		listOfProvider = new ArrayList<String>();
		
		/*setting the resources in class*/
		listViwProvider = (ListView) findViewById(R.id.list_view_wifi);
		setWifi = (Button) findViewById(R.id.btn_wifi);
		
		setWifi.setOnClickListener(this);
		wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		/*checking wifi connection
		 * if wifi is on searching available wifi provider*/ 
		if (wifiManager.isWifiEnabled() == true) {
			setWifi.setText("ON");
			scaning();
			
		}
		/*opening a detail dialog of provider on click   */
		listViwProvider.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				connectNewWifi(position);
			}
			
		});
		listViwProvider.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				String mWifiDetails = (wifiLists.get(position).toString()).replace(", ", "\n");
				ImportDialog action = new ImportDialog(MainActivity.this, mWifiDetails);
				action.showDialog();
				return true;
			}
		});
	}

	private void scaning() {
		// wifi scaned value broadcast receiver
		receiverWifi = new WifiReceiver();
		// Register broadcast receiver
		// Broacast receiver will automatically call when number of wifi
		// connections changed
		registerReceiver(receiverWifi, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		wifiManager.startScan();
		
	}

	/*setting the functionality of ON/OFF button*/
	@Override
	public void onClick(View arg0) {
		/* if wifi is ON set it OFF and set button text "OFF" */
		if (wifiManager.isWifiEnabled() == true) {
			wifiManager.setWifiEnabled(false);
			setWifi.setText("OFF");
			listViwProvider.setVisibility(ListView.GONE);
		} 
		/* if wifi is OFF set it ON 
		 * set button text "ON" 
		 * and scan available wifi provider*/
		else if (wifiManager.isWifiEnabled() == false) {
			wifiManager.setWifiEnabled(true);
			setWifi.setText("ON");
			listViwProvider.setVisibility(ListView.VISIBLE);
			scaning();
		}
	}

	protected void onPause() {
		super.onPause();
		unregisterReceiver(receiverWifi);
	}

	protected void onResume() {
		registerReceiver(receiverWifi, new IntentFilter(
				WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		super.onResume();
	}

	class WifiReceiver extends BroadcastReceiver {

		// This method call when number of wifi connections changed
		@Override
		public void onReceive(Context context, Intent intent) {
			wifiLists = wifiManager.getScanResults();
			/* sorting of wifi provider based on level */
			listOfProvider.clear();
			String providerName;
			for (int i = 0; i < wifiLists.size(); i++) {
				/* to get SSID and BSSID of wifi provider*/
				providerName = (wifiLists.get(i).SSID).toString()
						+"\t"+(wifiLists.get(i).BSSID).toString()+"\n";
				listOfProvider.add(providerName);
			}
			/*setting list of all wifi provider in a List*/
			adapter = new ListAdapter(MainActivity.this, listOfProvider);
			listViwProvider.setAdapter(adapter);
			
			adapter.notifyDataSetChanged();
			
		}
	}
 
    // 指定配置好的網络進行連接  
    public void connectConfiguration(int index) { 
        // 索引大於配置好的網络索引返回  
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();  
        config.allowedGroupCiphers.clear();  
        config.allowedKeyManagement.clear();  
        config.allowedPairwiseCiphers.clear();  
        config.allowedProtocols.clear();  
        
        config.SSID =wifiLists.get(index).SSID;    
        config.preSharedKey = "\"" + mPassword.getText().toString() + "\"";
        config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);  
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);  
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);  
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);  
        config.status = WifiConfiguration.Status.ENABLED;  
       
        int netId = wifiManager.updateNetwork(config);
        boolean bSuccess = false;
		if (netId > 0) {
			wifiManager.disconnect();
			 // 連接配置好的指定ID的網络  
			if (wifiManager.enableNetwork(netId, true)) { bSuccess = wifiManager.reconnect(); }
		} else {
			netId = wifiManager.addNetwork(config);
			
			if (netId > 0) {
				wifiManager.disconnect();
				if (wifiManager.enableNetwork(netId, true)) { bSuccess = wifiManager.reconnect(); }
			}
		}
    } 
    
    
    private void connectNewWifi(final int position){
    	LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
    	final View mDialogView = inflater.inflate(R.layout.enter_password_dialog, null);
    	mPassword = (EditText)mDialogView.findViewById(R.id.editText1);
    	CheckBox mPasswordVisible = (CheckBox)mDialogView.findViewById(R.id.checkBox1);
		mPasswordVisible.setOnCheckedChangeListener(PasswordVisibleCheckListener);
		
    	new AlertDialog.Builder(MainActivity.this)
    	.setView(mDialogView)
    	.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				connectConfiguration(position);
			}
		}).show();
    }
    
    OnCheckedChangeListener PasswordVisibleCheckListener = new OnCheckedChangeListener(){

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
	        
	        if(!isChecked){
	        	mPassword.setTransformationMethod(new PasswordTransformationMethod());;
	        }else{
	        	mPassword.setTransformationMethod(null);
	        }
			
		}
    	
    };
    
}

