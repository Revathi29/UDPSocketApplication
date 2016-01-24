package UDPTransmitterPackage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DataPacketInfo {

	// DataPacket class defines the fields of the data packet and constructs the
	// packet to send

	// --------------fields---------------------------//

	// static final fields

	// length of sequence number field in bytes - 4 bytes
	private static final int SEQUENCE_NUM_LENGTH = 4;
	// length of integrity check field in bytes - 4 bytes
	private static final int INTEGRITY_CHECK_LENGTH = 4;

	// instance fields

	// Packet Type field in the packet to send
	private byte packetType;
	// Sequence Number field in the packet to send
	private byte[] sequenceNumber = new byte[SEQUENCE_NUM_LENGTH];
	// payload length field in the packet to send
	private byte payloadLength;
	// the variable length payload field
	private byte[] payload = new byte[payloadLength];
	// 4 byte integrity check field in the packet to send
	private byte[] integrityCheck = new byte[INTEGRITY_CHECK_LENGTH];
	// the data packet that needs to be sent
	private byte[] dataPacket;

	// --------------public methods-------------------//

	// mutators
	public void setPacketType(byte aPacketType) {
		packetType = aPacketType;
	}

	public void setSequenceNumber(byte[] aSequenceNumber) {
		sequenceNumber = aSequenceNumber;
	}

	public void setPayloadLength(byte aPayloadLength) {
		payloadLength = aPayloadLength;
	}

	public void setPayload(byte[] aPayload) {
		payload = aPayload;
	}

	// accessors
	public byte getPayloadLength() {
		return payloadLength;
	}

	public byte[] getDataPacket() {
		return dataPacket;
	}

	public int getDataPacketLength() {
		return dataPacket.length;
	}

	// public method to construct the data packet
	public void constructDataPacket() {
		try {
			// an object of ByteArrayOutputStream is used to construct the data
			// packet
			ByteArrayOutputStream dataPacketStream = new ByteArrayOutputStream();
			// writing the byte sequence to the stream

			dataPacketStream.write(packetType);
			dataPacketStream.write(sequenceNumber);
			dataPacketStream.write(payloadLength);
			dataPacketStream.write(payload);
			// getting the byte sequence of the data that needs to be encrypted
			byte[] dataToEncrypt = dataPacketStream.toByteArray();
			// creating an object of IntegrityCheck class
			IntegrityCheck integrityObj = new IntegrityCheck(dataToEncrypt);
			// generate the new keystream for this data packet
			int noOfBytesToAdd = 4 - dataToEncrypt.length % 4;
			integrityObj.computeKeyStream(dataToEncrypt.length + noOfBytesToAdd);
			// getting the integrity check field compressed to 4 bytes
			integrityCheck = integrityObj.getIntegrityCheck();
			// writing the integrity check field to the stream
			dataPacketStream.write(integrityCheck);
			// the data packet is set to the byte sequence defined by the stream
			dataPacket = dataPacketStream.toByteArray();
		} catch (IOException ex) {
			// catch block for IO exception
			System.out.println(ex.toString());
		}
	}

	// overriding toString() method to return the data packet as string
	public String toString() {
		// String buffer is used to create the String defining the packet
		StringBuffer dataPacketString = new StringBuffer();
		dataPacketString.append("[ ");
		for (int i = 0; i < dataPacket.length; i++)
			dataPacketString.append(String.format("%x  ", dataPacket[i]));
		dataPacketString.append("]");
		return dataPacketString.toString();
	}
}// class DataPacket
