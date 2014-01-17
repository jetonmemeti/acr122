package ch.uzh.csg.mbps.acr122;

import android.util.Log;

import com.acs.smartcard.Reader;
import com.acs.smartcard.ReaderException;

public class TransceiveTask implements Runnable {
	
	private ITransceiverCallback callback;
	private byte[] toSend;
	private Reader reader;
	
	public TransceiveTask(ITransceiverCallback callback, byte[] toSend, Reader reader) {
		this.callback = callback;
		this.toSend = toSend;
		this.reader = reader;
	}

	@Override
	public void run() {
		byte[] result;
		try {
			byte[] recvBuffer = new byte[100];
			int length = 0;
			synchronized (reader) {
				 length = reader.transmit(0, toSend, toSend.length, recvBuffer, recvBuffer.length);
			}
			result = new byte[length];
			System.arraycopy(recvBuffer, 0, result, 0, length);
		} catch (ReaderException e) {
//			Log.e("TransceiveTask", "reader exception!", e);
			Log.e("TransceiveTask", "reader exception!");
			result = null;
		}
		callback.onReceived(result);
	}

}
