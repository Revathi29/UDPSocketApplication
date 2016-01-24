package UDPReceiverPackage;

public class ReceiverMainClass {

	// class which contains the main() method
	public static void main(String[] args) throws Exception {
		// entry point of the transmitter side of program

		// receiver port number is set to 9998
		final int RECEIVER_PORT_NUM = 9998;
		// maximum payload size field in bytes - 30 bytes
		final int MPS = 30;
		// creating synchronization class object
		Synchronization syncObj = new Synchronization();
		// calling the method that assigns the secret key. The secret key is set
		// to a value that is known by both transmitter and receiver
		byte[] secretKey = syncObj.getSecretKey();
		// calling the method that initializes sequence number to the value
		// previously agreed upon by transmitter and receiver
		byte[] expectedSequenceNumber = syncObj.getSequenceNumber();
		// invoking initialize method to set state and temp vector
		IntegrityCheck.initialize(secretKey);
		// creating receiver class object
		Receiver receiverObj = new Receiver(expectedSequenceNumber);
		// setting port number
		receiverObj.setPortNumber(RECEIVER_PORT_NUM);
		// setting MPS
		receiverObj.setMps(MPS);
		// receiving the data packet send by the transmitter
		receiverObj.startReceiving();

	}// main()

}// ReceiverMain Class
