package sender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class SenderAckThread extends Thread {

	DatagramSocket socket;
	ByteBuffer[] packets;
	InetAddress		destination;
	int			port;
	boolean receiver;
	Thread timer;
	
	public SenderAckThread(DatagramSocket socket, ByteBuffer[] packets,InetAddress destination, int port, boolean receiver, Thread timer) {
		this.socket=socket;
		this.packets=packets;
		this.destination=destination;
		this.port=port;
		this.receiver=receiver;
		this.timer=timer;
		// TODO Auto-generated constructor stub
	}
	
	public void run(){
		for(;;){
			byte[] ack = new byte[4];
			DatagramPacket receivePacket = new DatagramPacket(ack,ack.length );
			
			try {
				socket.receive( receivePacket );
				if(timer.isAlive()){
					timer.interrupt();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			ByteBuffer ackBB = ByteBuffer.wrap(ack);
			char rb = ackBB.getChar(0);
			short sN = ackBB.getShort(2);
			
			boolean cont = find(rb,sN);
			if(!cont){
				return;
			}
			
		}
		
	}
	
	private boolean find(char rb, short sN){
		boolean cont=true;
		
		for(int x=0;x<packets.length;x++){
			if(packets[x]==null){
				return false;
			}
			ByteBuffer b = packets[x];
			char rbLocal = b.getChar(0);
			short sNLocal = b.getShort(2);
			if(rbLocal==rb && sN==sNLocal){
				try {
					//System.out.println("Re-Sending packet #"+sNLocal);
					socket.send(new DatagramPacket(b.array(),b.capacity(),destination,port));
					cont=true;
					return cont;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(x==9){
				if((rb==rbLocal && sN==sNLocal+1)|| (rb!=rbLocal && sN==0 )){
					return false;
				}
			}
		}
		
		
		return true;
	}

}
