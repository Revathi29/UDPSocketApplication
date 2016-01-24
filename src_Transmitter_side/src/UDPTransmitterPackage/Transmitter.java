package UDPTransmitterPackage;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.math.BigInteger;

public class Transmitter {
	// Transmitter class which sends data packets to RX and receives ACK from RX

	// --------------fields------------------//

	// instance fields

	// the port number on which the receiver is listening
	private int receiverPortNumber;
	// maximum payload size in bytes
	private int mps;
	// size of message sent by transmitter in bytes
	private int messageSize;
	// message data that needs to be sent to the RX
	private byte[] messageData;
	// timeout interval in milliseconds
	private int timeoutInMilliSeconds;
	// the sequence number of the data packet to be sent currently
	private byte[] currentSequenceNumber;
	// the IP address of the receiver
	private byte[] receiverIpAddress;
	// the expected ACK number from the receiver
	private byte[] expectedAckNumber;
	// if this boolean variable is set, the RC4 algorithm does not progress to
	// next stream generation
	private boolean shouldAckStateBePrev;

	// ------------public methods-----------------//

	// instance methods: constructor
	public Transmitter(byte[] aReceiverIpAddress, byte[] aSeqNum) {
		receiverIpAddress = aReceiverIpAddress;
		currentSequenceNumber = aSeqNum;
		expectedAckNumber = currentSequenceNumber;
	}

	// instance methods: mutators

	public void setPortNumber(int aPortNumber) {
		receiverPortNumber = aPortNumber;
	}

	public void setMps(int aMps) {
		mps = aMps;
	}

	public void setTimeout(int aTimeout) {
		timeoutInMilliSeconds = aTimeout;
	}

	public void setMessage(byte[] aMessage) {
		messageData = aMessage;
		messageSize = aMessage.length;
	}

	// method that starts the transmission
	public void startTransmitting() {

		// size of acknowledgement received by transmitter in bytes
		final int RECEIVED_MSG_SIZE = 9;
		// number of timeout events that have occurred for the current data
		// packet
		int numberOfTimeout = 0;
		// calculate total number of packets to send
		int noOfPackets = (int) Math.ceil((double) messageSize / mps);
		// if the message size is not a multiple of mps, this variable contains
		// the last packet size
		int remainingLastBytes = messageSize % mps;

		try {

			// creating InetAddress object for the receiver using IP address
			// InetAddress receiver = InetAddress.getLocalHost();
			InetAddress receiver = InetAddress.getByAddress(receiverIpAddress);

			// creating the UDP transmitter socket (randomly chosen port number)
			DatagramSocket transmitterSocket = new DatagramSocket();
			// DatagramPacket object for the packet to send
			DatagramPacket sentPacket;

			// creating the byte array which receives the ACK packet
			byte[] receivedMessage = new byte[RECEIVED_MSG_SIZE];
			// creating DatagramPacket object for receiving ACK
			DatagramPacket receivedPacket = new DatagramPacket(receivedMessage, receivedMessage.length);

			// each iteration of the for loop transmits data packet and receives
			// ACK for the packet
			for (int i = 0; i < noOfPackets; i++) {
				// this variable tells if the packet needs to be retransmitted
				boolean isRetransmit = false;
				// creating an object of class DataPacket
				DataPacketInfo dataPacketObj = new DataPacketInfo();
				// set the sequence number of data packet using mutator
				dataPacketObj.setSequenceNumber(currentSequenceNumber);

				// checking if it is the last packet
				if (i == noOfPackets - 1) {
					// last packet to send
					dataPacketObj.setPacketType((byte) 0xaa);
					dataPacketObj.setPayloadLength((byte) remainingLastBytes);
				} else {
					// not last packet

					dataPacketObj.setPacketType((byte) 0x55);
					dataPacketObj.setPayloadLength((byte) mps);
				}

				// set the payload of packet by copying from the message data
				// array
				dataPacketObj.setPayload(
						Arrays.copyOfRange(messageData, i * mps, i * mps + dataPacketObj.getPayloadLength()));

				// construct the data packet to send
				// if(i!= 1)
				// dataPacketObj.constructDataPacket(false, isRetransmit);
				// else
				dataPacketObj.constructDataPacket();
				/*
				 * byte temp = dataPacketObj.getDataPacket()[0]; byte[]
				 * corruptedData = dataPacketObj.getDataPacket(); if(i == 1) {
				 * corruptedData[0] =0x28; }
				 */
				// call the datagram packet object constructor
				sentPacket = new DatagramPacket(dataPacketObj.getDataPacket(), dataPacketObj.getDataPacketLength(),
						receiver, receiverPortNumber);
				while (true) {// sending loop - run the loop until an ack is
								// received or timeout occurs
					/*
					 * if(isRetransmit && i ==1) {
					 * dataPacketObj.constructDataPacket(false, isRetransmit);
					 * sentPacket = new
					 * DatagramPacket(dataPacketObj.getDataPacket(),
					 * dataPacketObj.getDataPacketLength(), receiver,
					 * receiverPortNumber); }
					 */
					// sending the data packet
					transmitterSocket.send(sentPacket);
					// keeping record of the time when the packet was sent
					long timeWhenSend = System.currentTimeMillis();

					// printing the data packet that was sent
					// printing the sequence number in long for easy
					// interpretation by user
					long seqNumberInLong = new BigInteger(currentSequenceNumber).longValue();
					System.out.println("\nSending data packet number with sequence number: " + seqNumberInLong + "\n"
							+ dataPacketObj.toString());

					// incrementing the expected ACK number to sum of current
					// sequence number and payload length
					expectedAckNumber = findNextSeqNum(currentSequenceNumber, dataPacketObj.getPayloadLength());
					System.out.println("\nWaiting for ACK ");

					while (true) {// receiving loop - runs until a correct ACK
									// is received or timeout occurs
						try {
							// set the timeout interval for transmitter socket
							try {
								transmitterSocket.setSoTimeout(timeoutInMilliSeconds);
							} catch (SocketException ex) {
								System.out.println(ex.toString());
							}
							// set received packet length to default
							receivedPacket.setLength(RECEIVED_MSG_SIZE);
							// the code is blocked here until a packet is
							// received
							transmitterSocket.receive(receivedPacket);
							// printing the received ACK packet
							System.out.println("ACK packet received");
							System.out.print("[  ");
							for (int j = 0; j < RECEIVED_MSG_SIZE; j++)
								System.out.printf("%x  ", receivedPacket.getData()[j]);
							System.out.println("]");
							// check if the received ACK satisfies all
							// conditions
							if (checkAckPacket(receivedPacket.getData(), expectedAckNumber)) {
								// all conditions satisfied
								// retransmission not needed
								isRetransmit = false;
								// set the timeout value to default value
								if (numberOfTimeout > 0) {
									timeoutInMilliSeconds = 1000;
									numberOfTimeout = 0;
								}
								// break out of receive loop - send next packet
								// after this
								break;
							} else {
								// the received ACK does not satisfy all
								// conditions
								// set timeout value to current timeout value
								// minus the elapsed time
								long elapsedTimeInMilliSec = System.currentTimeMillis() - timeWhenSend;
								transmitterSocket
										.setSoTimeout((int) (transmitterSocket.getSoTimeout() - elapsedTimeInMilliSec));
								// continue waiting for an ACK
							}
						} catch (InterruptedIOException ex) {
							// a timeout occurs
							// need to retransmit the packet
							isRetransmit = true;
							// incrementing counter for number of timeouts
							numberOfTimeout++;
							System.out.println("\nTimeout occured; Timeout number: " + numberOfTimeout);
							if (numberOfTimeout < 4) {
								// no of timeouts less than 4 - double the
								// timeout interval
								timeoutInMilliSeconds *= 2;
								System.out.println("New timer value is " + timeoutInMilliSeconds / 1000 + "secs");
							} else {
								// no of timeouts is equal to 4
								// declare communication failure and exit the
								// program
								System.out.println("\nCommunication Failure; " + ex.toString());
								// closing the socket
								transmitterSocket.close();
								System.exit(0);
							}
							// break out of receiving loop when a timeout occurs
							// go back to sending loop for the current packet -
							// packet retransmit
							break;
						}
					}
					if (!isRetransmit) {
						// retransmission not needed
						// breaking the sending loop for current packet
						break;
					}

				}
				// this point is reeached when a correct ACK is received
				// set the current packet sequence number to the incremented
				// value
				currentSequenceNumber = Arrays.copyOf(expectedAckNumber, expectedAckNumber.length);

			} // for loop
				// this point reached when all packets are sent and all ACK
				// received
				// close the socket
			transmitterSocket.close();
		} catch (UnknownHostException ex) {
			// catch block for UnknownHostException
			System.out.println(ex.toString());
		} catch (SocketException ex) {
			// catch block for SocketException
			System.out.println(ex.toString());
		} catch (IOException ex) {
			// catch block for IOException
			System.out.println(ex.toString());
		}
	}// startTransmitting()

	// -------------private methods------------//

	private boolean checkAckPacket(byte[] receivedAckData, byte[] expectedAckNumber) {
		// this method checks the correctness of received ACK packet
		if (checkIfAckNumCorrect(receivedAckData, expectedAckNumber)) {
			// ack number correct
			if (checkForIntegrity(receivedAckData)) {
				// integrity correct
				if (checkIfPacketTypeCorrect(receivedAckData)) {
					// packetType correct
					shouldAckStateBePrev = false;
					return true;
				} else {
					// packetType wrong
					shouldAckStateBePrev = true;
					System.out.println("ack discarded due to wrong packet type");
				}
			} else {
				// integrity fails
				shouldAckStateBePrev = true;
				System.out.println("ack discarded due to integrity fail");
			}
		} else {
			// ack number wrong
			System.out.println("ack discarded due to wrong ack num");
		}
		return false;
	}

	private boolean checkIfAckNumCorrect(byte[] receivedAck, byte[] expectedAckNum) {
		// this method checks if ACK number is same as expected

		// start location of ACK number field
		final int ACK_NUM_START_LOC = 1;
		// start location of integrity field
		final int ACK_INTEGRITY_START_LOC = 5;

		// ACK number field in the received ACK packet
		byte[] receivedAckNumber = Arrays.copyOfRange(receivedAck, ACK_NUM_START_LOC, ACK_INTEGRITY_START_LOC);

		// printing ACK number in long for readability
		long ackNumberInLong = new BigInteger(receivedAckNumber).longValue();
		System.out.println("ACK number of the received ACK: " + ackNumberInLong);
		// check if the ACK number field is same as expected value
		return Arrays.equals(expectedAckNum, receivedAckNumber);
	}

	private boolean checkForIntegrity(byte[] receivedAck) {
		// this method checks integrity of the received packet

		// size of acknowledgement received by transmitter in bytes - 9 bytes
		final int RECEIVED_MSG_SIZE = 9;
		// start location of packet type field in ACK
		final int ACK_PACKET_TYPE_START_LOC = 0;
		// start location of integrity check field in ACK
		final int ACK_INTEGRITY_START_LOC = 5;
		// integrity check field of the received ACK packet
		byte[] receivedIntegrityCheck = Arrays.copyOfRange(receivedAck, ACK_INTEGRITY_START_LOC, RECEIVED_MSG_SIZE);
		// the byte sequence for which integrity needs to be computed
		byte[] dataToCheck = Arrays.copyOfRange(receivedAck, ACK_PACKET_TYPE_START_LOC, ACK_INTEGRITY_START_LOC);
		// creating IntegrityCheck object
		IntegrityCheck ackIntegrityObj = new IntegrityCheck(dataToCheck);
		// checking if the integrity check algorithm needs to be progressed
		if (!shouldAckStateBePrev) {
			int noOfBytesToAdd = 4 - dataToCheck.length % 4;

			// generate new keystream
			ackIntegrityObj.computeKeyStream(dataToCheck.length + noOfBytesToAdd);
		}
		// locally compute the integrity check for the received ACK
		byte[] localIntegrityCheck = ackIntegrityObj.getIntegrityCheck();
		// checking if locally computed integrity check is equal to integrity
		// field of received ACK
		return Arrays.equals(localIntegrityCheck, receivedIntegrityCheck);

	}

	private boolean checkIfPacketTypeCorrect(byte[] receivedAck) {
		// this method checks if packet type of received ACK is correct

		// start location of packet type field
		final int ACK_PACKET_TYPE_START_LOC = 0;
		// expected packet type is ff
		byte expectedPacketType = (byte) 0xff;
		// received packet type field
		byte receivedAckPacketType = receivedAck[ACK_PACKET_TYPE_START_LOC];
		// comparing received packet type to expected packet type
		if (Byte.compare(receivedAckPacketType, expectedPacketType) == 0) {
			return true;
		} else {
			return false;
		}
	}

	private byte[] findNextSeqNum(byte[] currentSeqNum, int payloadLength) {
		// this method returns the incremented sequence number

		// using byte buffer to convert sequence number to int
		ByteBuffer buf = ByteBuffer.wrap(currentSeqNum);
		int seqInInt = buf.getInt();
		// add payload length to sequence number
		seqInInt += payloadLength;
		// return the new sequence number as bytes
		return ByteBuffer.allocate(4).putInt(seqInInt).array();
	}

}// class Transmitter
