package UDPReceiverPackage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Receiver {
	// Receiver class which sends ack packets to TX and receives data packet
	// from TX

	// --------------fields------------------//

	// instance fields

	// the expected sequence number field
	private byte[] expectedSequenceNumber;
	// previously received sequence number field
	private byte[] lastReceivedSeqNumber;
	// the port number on which the receiver is listening
	int receiverPortNumber = 9998;
	// maximum packet size in bytes - 30 bytes
	int mps = 30;

	// ------------public methods-----------------//

	// instance methods: constructor

	public Receiver(byte[] aSequenceNumber) {
		expectedSequenceNumber = aSequenceNumber;
	}
	// instance methods: mutators

	public void setPortNumber(int aPortNumber) {
		receiverPortNumber = aPortNumber;
	}

	public void setMps(int aMps) {
		mps = aMps;
	}

	// -------------public methods------------//

	// public method to start receiving the data packet
	public void startReceiving() {
		// -------------public methods------------//

		// public method to start receiving the data packet
		final int MAX_MSG_SIZE = 40;
		// location in the data packet received where payload field is stored
		final int PAYLOAD_LENGTH_LOC = 5;
		// location in the received data packet from where payload is stored
		final int REC_PAYLOAD_START_LOC = 6;

		// creating the byte array which receives the data packet
		byte[] receivedMessage = new byte[MAX_MSG_SIZE];
		// creating the DatagramPacket object to receive data from TX
		DatagramPacket receivePacket = new DatagramPacket(receivedMessage, receivedMessage.length);
		// initializing boolean variable isPacketDiscarded to be false
		boolean isPacketDiscarded = false;
		// initializing boolean variable isDuplicatePacket to be false
		boolean isDuplicatePacket = false;
		// an object of ByteArrayOutputStream is used to store payload
		ByteArrayOutputStream payloadStream = new ByteArrayOutputStream();

		try {
			// creating the UDP receiver socket
			DatagramSocket receiverSocket = new DatagramSocket(receiverPortNumber);

			while (true) {
				System.out.println("Waiting for a packet");
				// receiving the data packet from the transmitter
				receiverSocket.receive(receivePacket);
				// creating InetAddress object for the transmitter
				// getAddress() method gets the IPAddress from the received
				// packet
				InetAddress transmitterAddress = receivePacket.getAddress();
				// transmitter port is set
				int transmitterPort = receivePacket.getPort();
				// getting data from the received packet
				byte[] receivedData = receivePacket.getData();
				// getting payload length from the received packet
				int payloadLength = (int) receivedData[PAYLOAD_LENGTH_LOC];

				// checking whether its a correct expected packet, duplicate
				// packet or incorrect packet
				switch (checkIfSeqNumCorrect(receivedData, receivePacket.getLength(), expectedSequenceNumber)) {
				case "expected":
					// not a duplicate packet
					isDuplicatePacket = false;
					if (checkForIntegrity(receivedData, receivePacket.getLength(), isPacketDiscarded)) {
						// integrity check correct
						if (checkIfPacketTypeCorrect(receivedData)) {
							// packet type correct
							if (checkIfPayloadLengthCorrect(receivedData, receivePacket.getLength(), mps)) {
								// payload length correct
								// not a discarded packet
								// correct in order packet is received
								// ack packet needs to be sent with next
								// expected sequence number
								isPacketDiscarded = false;
								// getting received payload from the packet
								byte[] receivedPayload = Arrays.copyOfRange(receivedData, REC_PAYLOAD_START_LOC,
										REC_PAYLOAD_START_LOC + payloadLength);
								payloadStream.write(receivedPayload);
								// updating the received sequence number field
								lastReceivedSeqNumber = expectedSequenceNumber;
								// finding out the next expected sequence number
								expectedSequenceNumber = findNextAckNum(expectedSequenceNumber, payloadLength);
								// method which constructs and sends the ack
								// packet
								sendAckPacket(receiverSocket, transmitterAddress, transmitterPort,
										expectedSequenceNumber, isDuplicatePacket);
								// set the length of rceivePacket to default
								// value for receiving the next packet
								receivePacket.setLength(MAX_MSG_SIZE);

								// printing all the received bytes after the
								// final packet is received
								if (Byte.compare(receivedData[0], (byte) 0xaa) == 0) {
									byte[] completeDataPayload = payloadStream.toByteArray();
									System.out.print("\nThe complete received data:\n[  ");
									for (int i = 0; i < completeDataPayload.length; i++)
										System.out.printf("%x  ", completeDataPayload[i]);
									System.out.print("]\n");
								}

							} else {
								// incorrect payload length
								// packet discarded - no need to progress RC4
								// algorithm for next packet
								isPacketDiscarded = true;
								System.out.println("Data packet discarded due to wrong payload length");
							}
						} else {
							// incorrect packet type
							// packet discarded - no need to progress RC4
							// algorithm for next packet
							isPacketDiscarded = true;
							System.out.println("Data packet discarded due to wrong packet type");
						}
					} else {
						// integrity fails
						// packet discarded - no need to progress RC4 algorithm
						// for next packet
						isPacketDiscarded = true;
						System.out.println("Data packet discarded due to integrity fail");
					}
					break;
				case "duplicate":
					// the received data packet is a duplicate packet
					System.out.println("Duplicate packet: discarded; ACK with expected seq num sent");
					// resending acknowledgment packet
					isDuplicatePacket = true;
					// calling method which constructs and sends the ack packet
					sendAckPacket(receiverSocket, transmitterAddress, transmitterPort, expectedSequenceNumber,
							isDuplicatePacket);
					// set the length of rceivePacket to default value for
					// receiving the next packet
					receivePacket.setLength(MAX_MSG_SIZE);
					break;
				case "wrong":
					// the received data packet has incorrect sequence number
					// packet discarded - since the RC4 algorithm did not
					// progress no need to set the boolean flag
					System.out.println("Data packet discarded due to wrong sequence number");
					break;
				default:
					break;
				}
			}

		} catch (SocketException ex) {
			// catch block for SocketException
			System.out.println(ex.toString());
		} catch (IOException ex) {
			// catch block for IOException
			System.out.println(ex.toString());
		}
	}// startReceiving()

	// -------------private methods------------//

	// private method to send acknowledgment packet
	private void sendAckPacket(DatagramSocket receiverSocket, InetAddress transmitterAddress, int transmitterPort,
			byte[] expectedSequenceNumber, boolean isDuplicatePacket) {
		// size of the ack packet in bytes - 9 bytes
		final int SENT_MSG_SIZE = 9;
		// 9 bytes ack to be send
		byte[] sentMessage = new byte[SENT_MSG_SIZE];
		// creating ackPacketInfo object to construct ack packet
		AckPacketInfo ackPacketObj = new AckPacketInfo((byte) 0xff, expectedSequenceNumber, isDuplicatePacket);
		sentMessage = ackPacketObj.getAckPacket();
		// creating DatagramPacket object to send the ack packet
		DatagramPacket sentPacket = new DatagramPacket(sentMessage, sentMessage.length);
		// setting TX IP address
		sentPacket.setAddress(transmitterAddress);
		// setting TX port number
		sentPacket.setPort(transmitterPort);
		// setting ack packet length
		sentPacket.setLength(SENT_MSG_SIZE);
		long ackNumberInLong = new BigInteger(expectedSequenceNumber).longValue();
		System.out.println("ACK packet with ACK number " + ackNumberInLong + " is sent\n" + ackPacketObj.toString());
		try {
			// sending ack packet
			receiverSocket.send(sentPacket);
		} catch (IOException ex) {
			// catch block for IOException
			System.out.println(ex.toString());
		}

	}

	// private method to check if sequence number is same as expected
	private boolean checkForIntegrity(byte[] receivedData, int receivedDataLength, boolean isPacketDiscarded) {
		// start location of the packet type field
		final int REC_PACKET_TYPE_START_LOC = 0;
		// start location of the integrity field
		final int INTEGRITY_FIELD_LENGTH = 4;
		// integrity field in the received data packet
		byte[] receivedIntegrityCheck = Arrays.copyOfRange(receivedData, receivedDataLength - INTEGRITY_FIELD_LENGTH,
				receivedDataLength);
		// getting the data to check integrity on
		byte[] dataToCheck = Arrays.copyOfRange(receivedData, REC_PACKET_TYPE_START_LOC,
				receivedDataLength - INTEGRITY_FIELD_LENGTH);
		// creating integrity check object
		IntegrityCheck integrityObj = new IntegrityCheck(dataToCheck);
		if (!isPacketDiscarded) {
			// valid packet
			int noOfBytesToAdd = 4 - dataToCheck.length % 4;
			// generating new keystream byte array
			integrityObj.computeKeyStream(dataToCheck.length + noOfBytesToAdd);
		}
		// System.out.print("The locally computed integrity check field for
		// received packet: ");
		// locally compute the integrity check
		byte[] localIntegrityCheck = integrityObj.getIntegrityCheck();
		// check if received integrity equal to locally computed one
		return Arrays.equals(localIntegrityCheck, receivedIntegrityCheck);
	}

	// private method to check whether the received data packet sequence num is
	// same as expected
	private String checkIfSeqNumCorrect(byte[] receivedData, int receivedDataLength, byte[] expectedSeqNum) {
		// start location of the sequence num field
		final int SEQ_NUM_START_LOC = 1;
		// end location of the sequence number field
		final int SEQ_NUM_END_LOC = 5;
		byte[] receivedSeqNum = Arrays.copyOfRange(receivedData, SEQ_NUM_START_LOC, SEQ_NUM_END_LOC);
		// getting long value of received and expected sequence number for easy
		// readability
		long longReceivedSeqNum = new BigInteger(receivedSeqNum).longValue();
		long longExpectedSeqNum = new BigInteger(expectedSeqNum).longValue();
		System.out.println("\nExpected sequence number:  " + longExpectedSeqNum);
		System.out.println("\nReceived a data packet with sequence number " + longReceivedSeqNum);
		// printing received data
		System.out.print("[  ");
		for (int i = 0; i < receivedDataLength; i++)
			System.out.printf("%x  ", receivedData[i]);
		System.out.print("]\n");
		if (Arrays.equals(expectedSeqNum, receivedSeqNum)) {
			// expected sequence number
			return "expected";
		} else {
			if (lastReceivedSeqNumber != null) {
				if (Arrays.equals(lastReceivedSeqNumber, receivedSeqNum)) {
					// sequence number same as last received
					return "duplicate";
				}
			}
			// wrong sequence number
			return "wrong";
		}
	}

	// private method to check whether received payload length is correct
	private boolean checkIfPayloadLengthCorrect(byte[] receivedData, int receivedDataLength, int MPS) {
		// start location of the payload field
		final int PAYLOAD_FIELD_START_LOC = 6;
		// start location of the integrity field
		final int INTEGRITY_FIELD_LENGTH = 4;
		// getting payload length
		byte payloadLengthField = receivedData[5];
		int receivedPayloadLength = receivedDataLength - INTEGRITY_FIELD_LENGTH - PAYLOAD_FIELD_START_LOC;
		if ((Byte.compare(payloadLengthField, (byte) MPS) <= 0) && (receivedPayloadLength == payloadLengthField)) {
			// payload length is correct
			return true;
		} else {
			// payload length is incorrect
			return false;
		}

	}

	// private method to check whether received packet type is correct
	private boolean checkIfPacketTypeCorrect(byte[] receivedData) {
		// start location of the packet type field
		final int REC_PACKET_TYPE_START_LOC = 0;
		byte receivedDataPacketType = receivedData[REC_PACKET_TYPE_START_LOC];
		if ((Byte.compare(receivedDataPacketType, (byte) 0x55) == 0)
				|| (Byte.compare(receivedDataPacketType, (byte) 0xaa) == 0)) {
			// received packet type is correct
			return true;
		} else {
			// received packet type is incorrect
			return false;
		}
	}

	// private method to find the next ack number
	private byte[] findNextAckNum(byte[] currentAckNumber, int payloadLength) {
		// using byte buffer to convert sequence number to int
		ByteBuffer buf = ByteBuffer.wrap(currentAckNumber);
		int ackInInt = buf.getInt();
		// add payload length to sequence number
		ackInInt += payloadLength;
		return ByteBuffer.allocate(4).putInt(ackInInt).array();
	}
}// class Receiver
