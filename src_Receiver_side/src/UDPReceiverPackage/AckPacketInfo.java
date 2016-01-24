package UDPReceiverPackage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AckPacketInfo {
	// AckPacket class defines the fields of the acknowledgment packet and
	// constructs the
	// acknowledgment packet to send

	// --------------fields---------------------------//

	// static final fields

	// length of acknowledgement number field in bytes - 4 bytes
	private static final int ACK_NUM_LENGTH = 4;
	// length of integrity check field in bytes - 4 bytes
	private static final int INTEGRITY_LENGTH = 4;
	// length of ack packet to be sent in bytes - 9 bytes
	private static final int SENT_MSG_SIZE = 9;

	// instance fields

	// Packet Type field in the ack packet
	private byte ackPacketType;
	// 4 byte ack number field in the packet to send
	private byte[] ackNumber = new byte[ACK_NUM_LENGTH];
	// 4 byte integrity check field in the packet to send
	private byte[] integrityCheck = new byte[INTEGRITY_LENGTH];
	// 9 byte ack packet
	private byte[] ackPacket = new byte[SENT_MSG_SIZE];
	// initializing the isResentAck variable to false
	private boolean isResentAck = false;

	// instance methods: constructor

	public AckPacketInfo(byte aPacketType, byte[] anAckNum, boolean isResentAcknowledgement) {
		ackPacketType = aPacketType;
		ackNumber = anAckNum;
		isResentAck = isResentAcknowledgement;
		// calling construct data packet method internally
		constructAckPacket();
	}

	// instance method: mutators

	public void setAckNumber(byte[] aAckNumber) {
		ackNumber = aAckNumber;
	}

	public void setPacketType(byte aAckPacketType) {
		ackPacketType = aAckPacketType;
	}

	// instance methods: accessors

	public byte[] getAckPacket() {
		return ackPacket;
	}

	// overriding toString() method to return the acknowledgment packet as
	// string
	public String toString() {
		// String buffer is used to create the String defining the packet
		StringBuffer ackPacketString = new StringBuffer();
		ackPacketString.append("[ ");
		for (int i = 0; i < SENT_MSG_SIZE; i++)
			ackPacketString.append(String.format("%x  ", ackPacket[i]));
		ackPacketString.append("]");
		return ackPacketString.toString();
	}

	// -----------------private methods-------------//
	// private method to construct the acknowledgment packet
	private void constructAckPacket() {
		try {
			// an object of ByteArrayOutputStream is used to construct the ack
			// packet
			ByteArrayOutputStream ackPacketStream = new ByteArrayOutputStream();
			// writing the byte sequence to the stream

			ackPacketStream.write(ackPacketType);
			ackPacketStream.write(ackNumber);
			// getting the byte sequence of the ack packet data that needs to be
			// encrypted
			byte[] ackDataToEncrypt = ackPacketStream.toByteArray();
			// creating an object of IntegrityCheck class
			IntegrityCheck integrityObj = new IntegrityCheck(ackDataToEncrypt);
			// execute only for first time ack packet
			if (!isResentAck) {
				// generate the new keyStream for this data packet
				int noOfBytesToAdd = 4 - ackDataToEncrypt.length % 4;
				integrityObj.computeKeyStream(ackDataToEncrypt.length + noOfBytesToAdd);
			}
			// getting the integrity check field compressed to 4 bytes
			integrityCheck = integrityObj.getIntegrityCheck();
			// writing the integrity check field to the stream
			ackPacketStream.write(integrityCheck);
			// the data packet is set to the byte sequence defined by the stream
			ackPacket = ackPacketStream.toByteArray();
		} catch (IOException ex) {
			// catch block for IO exception
			System.out.println(ex.toString());
		}
	}

}// class AckPacketInfo
