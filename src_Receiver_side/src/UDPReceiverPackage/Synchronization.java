package UDPReceiverPackage;

public class Synchronization {
	// Synchronization class sets the initial sequence number and the secret key
	// this class is common to transmitter and receiver

	// -----------------------fields----------------//

	// static final fields

	// length of secret key in bytes
	private static final int SECRET_KEY_LENGTH = 16;
	// secret key
	private static final byte[] secretKey = new byte[SECRET_KEY_LENGTH];
	// length of the sequence number
	private static final int SEQUENCE_NUM_LENGTH = 4;
	// the agreed upon initial sequence number
	private static final byte[] sequenceNumber = new byte[SEQUENCE_NUM_LENGTH];

	// ------------------public methods------------//

	// instance methods - constructor
	public Synchronization() {
		// assigns key
		assignSecretKey();
		// intializes sequence number
		initializeSequenceNumber();
	}

	// instance methods - accessors
	public byte[] getSecretKey() {
		return secretKey;
	}

	public byte[] getSequenceNumber() {
		return sequenceNumber;
	}

	// -----------private methods---------//
	private void assignSecretKey() {
		// assigns the agreed up on secret key
		secretKey[0] = (byte) 0x83;
		secretKey[1] = (byte) 0x45;
		secretKey[2] = (byte) 0x75;
		secretKey[3] = (byte) 0x6c;
		secretKey[4] = (byte) 0x5c;
		secretKey[5] = (byte) 0x1a;
		secretKey[6] = (byte) 0x3c;
		secretKey[7] = (byte) 0xba;
		secretKey[8] = (byte) 0x45;
		secretKey[9] = (byte) 0xb8;
		secretKey[10] = (byte) 0xb4;
		secretKey[11] = (byte) 0x8;
		secretKey[12] = (byte) 0xf2;
		secretKey[13] = (byte) 0x8d;
		secretKey[14] = (byte) 0x4a;
		secretKey[15] = (byte) 0x14;
	}

	private void initializeSequenceNumber() {
		// assigns the agreed up on initial sequence number
		sequenceNumber[0] = (byte) 0x19;
		sequenceNumber[1] = (byte) 0x55;
		sequenceNumber[2] = (byte) 0xf7;
		sequenceNumber[3] = (byte) 0x28;
	}

}// class Synchronization
