package ch.uzh.csg.mbps.acr122;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

public class MyHostApduService extends HostApduService {

	private int messageCounter = 0;

	@Override
	public byte[] processCommandApdu(byte[] apdu, Bundle extras) {
		if (selectAidApdu(apdu)) {
			Log.i("MyHostApduService", "Application selected");
			return getNextMessage();
		} else {
			Log.e("MyHostApduService", "Received: " + new String(apdu));
			return getNextMessage();
		}
	}

	private byte[] getNextMessage() {
		return ("Message from rerg nexus: " + messageCounter++).getBytes();
	}

	private boolean selectAidApdu(byte[] apdu) {
		return apdu.length >= 2 && apdu[0] == (byte)0 && apdu[1] == (byte)0xa4;
	}

	@Override
	public void onDeactivated(int reason) {
		//TODO jeton: check reason
		Log.e("MyHostApduService", "Deactivated: " + reason);
	}
	
}
