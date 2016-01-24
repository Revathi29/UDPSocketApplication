package UDPTransmitterPackage;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

public class TransmitterMainClass {

	// class which contains the main() method
	public static void main(String[] args) {
		// entry point of the transmitter side of program

		// port number on which the receiver is listening
		final int RECEIVER_PORT_NUM = 9998;
		// maximum payload size in bytes
		final int MPS = 30;
		// size in bytes of the data that needs to be sent
		final int MESSAGE_SIZE = 500;
		// initial value of the timer for the transmitter socket
		final int TIMEOUT_IN_MILLISEC = 1000;
		// the byte array containing the data to be sent
		byte[] messageData = new byte[MESSAGE_SIZE];

		try {
			// the IP address of the receiver
			byte[] ipAddress = InetAddress.getLocalHost().getAddress();
			// creating an object of the Synchronization class which
			// synchronizes the transmitter and receiver
			Synchronization syncObj = new Synchronization();
			// using the accessor method of Synchronization class to get secret
			// key
			byte[] secretKey = syncObj.getSecretKey();
			// using the accessor method of Synchronization class to get initial
			// sequence number
			// nextSequenceNumber variable defines the sequence number of the
			// next packet to send
			byte[] nextSequenceNumber = syncObj.getSequenceNumber();

			// calling the static method that initializes the S and T vectors in
			// integrity check object
			IntegrityCheck.initialize(secretKey);

			/*
			 * byte[] testString = {0x00, 0x00, 0x11,0x11}; IntegrityCheck
			 * checkObj = new IntegrityCheck(testString);
			 * checkObj.computeKeyStream(); checkObj.getIntegrityCheck();
			 */

			// creating an object of class Random
			Random randGen = new Random();
			// generating 500 bytes of the data to send using Random object
			randGen.nextBytes(messageData);
			// printing the entire data that is to be sent
			System.out.println("500 bytes of data to be sent: ");
			for (int i = 0; i < 500; i++)
				System.out.printf("%x ", messageData[i]);
			System.out.print("\n\n");

			// creating an object of Transmitter class
			Transmitter transmitterObj = new Transmitter(ipAddress, nextSequenceNumber);

			// setting instance variables using mutators
			// setting the port number using mutator
			transmitterObj.setPortNumber(RECEIVER_PORT_NUM);
			// setting the max payload size using mutator
			transmitterObj.setMps(MPS);
			// setting the initial timeout using mutator
			transmitterObj.setTimeout(TIMEOUT_IN_MILLISEC);
			// setting the data that needs to be sent
			transmitterObj.setMessage(messageData);

			// calling the method that starts the transmission of data
			transmitterObj.startTransmitting();

		} catch (UnknownHostException ex) {
			// catch block for UnknownHostException of InetAddress class
			System.out.println(ex.toString());
		}
	}// main()

}// class TransmitterMainClass
