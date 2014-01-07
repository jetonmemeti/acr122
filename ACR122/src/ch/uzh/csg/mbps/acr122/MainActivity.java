package ch.uzh.csg.mbps.acr122;

import android.app.Activity;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import com.acs.smartcard.Reader;
import com.acs.smartcard.Reader.OnStateChangeListener;

public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";
	
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    
    private static final String[] stateStrings = { "Unknown", "Absent",
        "Present", "Swallowed", "Powered", "Negotiable", "Specific" };
	
	private UsbManager mManager;
	private Reader mReader;
	private PendingIntent mPermissionIntent;
	
	private TextView tv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
//		if (BluetoothAdapter.getDefaultAdapter().getAddress().equalsIgnoreCase("88:9b:39:d6:69:3a")) {
//			Log.e("MainActivity", "this is the emulated tag (csg nexus)");
//			return;
//		}
		
		Log.e("MainActivity", "this is the device with the attached nfc reader (rerg nexus)");
		
		
		 // Get USB manager
        mManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        tv = (TextView) findViewById(R.id.txtview);
        
        // Initialize reader
        mReader = new Reader(mManager);
        mReader.setOnStateChangeListener(new OnStateChangeListener() {
			
			@Override
			public void onStateChange(int slotNum, int prevState, int currState) {
				if (prevState < Reader.CARD_UNKNOWN || prevState > Reader.CARD_SPECIFIC) {
                    prevState = Reader.CARD_UNKNOWN;
                }

                if (currState < Reader.CARD_UNKNOWN || currState > Reader.CARD_SPECIFIC) {
                    currState = Reader.CARD_UNKNOWN;
                }

                // Create output string
                final String outputString = "Slot " + slotNum + ": " + stateStrings[prevState] + " -> " + stateStrings[currState];
                Log.e(TAG, outputString);
                
                try {
                	if (currState == 2) {
            			mReader.power(0, Reader.CARD_COLD_RESET);
            			Log.e(TAG, "powered");
//                	} else if (currState == 4) {
                		mReader.setProtocol(0, Reader.PROTOCOL_T0 | Reader.PROTOCOL_T1);
                		Log.e(TAG, "protocol set");
                		int state = mReader.getState(0);
                		if (state == 5) {
                			Log.e(TAG, "protocol negotiable");
                		} else if (state == 6) {
                			Log.e(TAG, "protocol specific");
                			byte[] recvBuffer = new byte[4096];
//                			byte[] sendBuffer = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 };
                			byte[] sendBuffer = createSelectAidApdu();
                			int length = mReader.transmit(0, sendBuffer, sendBuffer.length, recvBuffer, recvBuffer.length);
                			Log.e(TAG, "transmitted. received "+length+" bytes");
                			
                			byte[] result = new byte[length];
                			System.arraycopy(recvBuffer, 0, result, 0, length);
                			
                			StringBuilder builder = new StringBuilder();
                			for (byte b : result) {
                				builder.append(b);
                				builder.append(", ");
                			}
                			
                			Log.e(TAG, builder.toString());
                			Log.e(TAG, new String(result));
                		}
                	} else if (currState == 6) {
                		Log.e(TAG, "currState == 6");
                		
						byte[] recvBuffer = new byte[300];
						byte[] sendBuffer = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 };
						mReader.transmit(0, sendBuffer, sendBuffer.length, recvBuffer, recvBuffer.length);
						tv.setText(new String(recvBuffer));
						Log.e(TAG, "transmitted");
                	}
                } catch (Exception e) {
                	Log.e(TAG, "error", e);
                }
				
			}
		});
        
		// Register receiver for USB permission
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        registerReceiver(mReceiver, filter);
	}
	
	@Override
	public void onDestroy() {
		mReader.close();
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private static final byte[] CLA_INS_P1_P2 = { 0x00, (byte)0xA4, 0x04, 0x00 };
	private static final byte[] AID_ANDROID = { (byte)0xF0, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06 };
	
	private byte[] createSelectAidApdu() {
		byte[] result = new byte[6 + AID_ANDROID.length];
		System.arraycopy(CLA_INS_P1_P2, 0, result, 0, CLA_INS_P1_P2.length);
		result[4] = (byte) AID_ANDROID.length;
		System.arraycopy(AID_ANDROID, 0, result, 5, AID_ANDROID.length);
		result[result.length - 1] = 0;
		return result;
	}
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
            	for (UsbDevice device : mManager.getDeviceList().values()) {
                    if (mReader.isSupported(device)) {
                    	Log.i(TAG, "requesting permission");
                    	 mManager.requestPermission(device, mPermissionIntent);
                    	 break;
                    }
                }
            } else if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						if (device != null) {
							try {
								Log.i(TAG, "Opening device " + device.getDeviceName());
								mReader.open(device);
							} catch (Exception e) {
								Log.e(TAG, "Exception open, power, protocol for device " + device.getDeviceName(), e);
							}
						}
                    } else {
                        Log.e(TAG, "Permission denied for device " + device.getDeviceName());
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (device != null && device.equals(mReader.getDevice())) {
                    	Log.i(TAG, "closing device "+device.getDeviceName());
                    	mReader.close();
                    }
                }
            }
        }
    };
    
}
