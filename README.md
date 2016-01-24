# UDPSocketApplication
Distributed network application with reliable data transfer and cryptographic authentication using RC4 encryption
The project is aimed at designing a networking application which implements reliable data transfer protocol functionality over unreliable User Datagram Protocol (UDP) communication as well as cryptographic authentication. The application uses JAVA’s UDP socket to interface transmitter and receiver and uses stop and wait protocol for data transfer.
In our approach to this project, we have defined four classes to perform the task of synchronization, integrity check, construct data packet / acknowledgment packet, initialize socket parameters & send/receive packets at both transmitter and receiver side.
The functions performed by the two sides of the application are described as follows:
I. Transmitter side:
•	Initializing socket parameters.
•	Providing the agreed upon secret key to the RC4 algorithm
•	Constructing data packets as per the agreed upon sequence number, maximum payload size 
•	Calculating the integrity check field of data packet
•	Sending the data packets and receiving acknowledgement packets as per stop and wait protocol
•	Checking the ACK number, packet type and integrity of the received ACK. If any of these is wrong the ACK is discarded
•	At timeout, the data packet is retransmitted. On the fourth timeout, communication failure is declared
II. Receiver side:
•	Initializing socket parameters.
•	Providing the agreed upon secret key to the RC4 algorithm
•	Receiving the data packets and checking for the correct sequence number as per previous agreement. 
•	Checking the integrity, packet type, payload length of the received data packet. If any of these are wrong, the data packet is discarded.
•	If the sequence number indicates that the received packet is duplicate, an ACK with the expected sequence number as ACK number is resent. (Handles loss of ACK)
•	Constructing acknowledgment packet with integrity check field calculated as per algorithm.
•	If correct data is received, sending acknowledgment packet to the transmitter with expected sequence number as ACK number.

 
