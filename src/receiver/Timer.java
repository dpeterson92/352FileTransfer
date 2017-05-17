package receiver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Random;

public class Timer extends Thread {
	
	DatagramSocket socket;
	short currentSN;
	char currentRB;
	int port;
	InetAddress senderAddr;
	int threshold;
	
	public Timer(DatagramSocket socket, short currentSN, char currentRB, InetAddress senderAddr, int threshold){
		this.socket=socket;
		this.currentSN=currentSN;
		this.currentRB=currentRB;
		this.senderAddr=senderAddr;
		this.threshold=threshold;
		port=10101;
	}
	
	public void run(){
		
		while(!this.isInterrupted()){
			try {
				time();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				return;
			}
		}
	}
	
	private void time() throws InterruptedException{
		//sleep(1000);
		
		byte[] b = new byte[4];
		ByteBuffer bb = ByteBuffer.wrap(b);
		if(currentSN==Short.MAX_VALUE){
			if(currentRB=='r'){
				bb.putChar('b');
			}else if(currentRB=='b'){
				bb.putChar('r');
			}
			bb.putShort((short) 0);
			try {
				Random random = new Random();
				
				if(random.nextInt(100)<=threshold){
					socket.send(new DatagramPacket(bb.array(),bb.capacity(),senderAddr,port));
				}else{
					System.out.println("Packet #"+currentSN+" dropped");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			bb.putChar(currentRB);
			short nextSN=(short) (currentSN+1);
			bb.putShort(nextSN);
			
			try {
				Random random = new Random();
				
				if(random.nextInt(100)<=threshold){
					//System.out.println("Sending nack #"+nextSN);
					socket.send(new DatagramPacket(bb.array(),bb.capacity(),senderAddr,port));
				}else{
					System.out.println("Ack #"+(currentSN+1)+" dropped");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		sleep(1000);
	}
}
