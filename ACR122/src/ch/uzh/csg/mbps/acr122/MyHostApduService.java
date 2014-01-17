package ch.uzh.csg.mbps.acr122;

import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

public class MyHostApduService extends HostApduService {

	private int messageCounter = 0;
	
//	private String[] STRINGS = new String[] {
//			"Sense child do state to defer mr of forty. Become latter but nor abroad wisdom waited. Was delivered gentleman acuteness but daughters. In as of whole as match asked. Pleasure exertion put add entrance distance drawings. In equally matters showing greatly it as. Want name any wise are able park when. Saw vicinity judgment remember finished men throwing.",
//			"Attention he extremity unwilling on otherwise. Conviction up partiality as delightful is discovered. Yet jennings resolved disposed exertion you off. Left did fond drew fat head poor. So if he into shot half many long. China fully him every fat was world grave.",
//			"Certain but she but shyness why cottage. Gay the put instrument sir entreaties affronting. Pretended exquisite see cordially the you. Weeks quiet do vexed or whose. Motionless if no to affronting imprudence no precaution. My indulged as disposal strongly attended. Parlors men express had private village man. Discovery moonlight recommend all one not. Indulged to answered prospect it bachelor is he bringing shutters. Pronounce forfeited mr direction oh he dashwoods ye unwilling.",
//			"Call park out she wife face mean. Invitation excellence imprudence understood it continuing to. Ye show done an into. Fifteen winding related may hearted colonel are way studied. County suffer twenty or marked no moment in he. Meet shew or said like he. Valley silent cannot things so remain oh to elinor. Far merits season better tended any age hunted.",
//			"Give lady of they such they sure it. Me contained explained my education. Vulgar as hearts by garret. Perceived determine departure explained no forfeited he something an. Contrasted dissimilar get joy you instrument out reasonably. Again keeps at no meant stuff. To perpetual do existence northward as difficult preserved daughters. Continued at up to zealously necessary breakfast. Surrounded sir motionless she end literature. Gay direction neglected but supported yet her."
//	};

	@Override
	public byte[] processCommandApdu(byte[] apdu, Bundle extras) {
		if (selectAidApdu(apdu)) {
			Log.i("MyHostApduService", "Application selected");
			return getNextMessage();
		} else {
			Log.e("MyHostApduService", "Received: " + messageCounter + "("+apdu[0]+")");
			return getNextMessage();
		}
	}

	private byte[] getNextMessage() {
//		messageCounter++;
//		byte[] bytes = null;
//		for (int i=0; i<messageCounter; i++) {
//			if (i==0) {
//				bytes = new byte[messageCounter];
//			}
//			bytes[i] = (byte) i;
//		}
//		
//		return bytes;
//		return STRINGS[messageCounter-1].getBytes();
		
		messageCounter++;
		byte[] bytes = new byte[100];
		for (int i=0; i<100; i++) {
			bytes[i] = 0x01;
		}
		return bytes;
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
