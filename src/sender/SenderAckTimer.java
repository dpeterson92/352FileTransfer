package sender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class SenderAckTimer extends Thread {
	DatagramSocket socket;
	ByteBuffer[] packets;
	InetAddress destination;
	int port;
	int iteration;
	boolean receiver;
	
	public SenderAckTimer(DatagramSocket socket, ByteBuffer[] packets, InetAddress destination, int port,
			int iteration, boolean receiver){
		this.socket=socket;
		this.packets=packets;
		this.destination=destination;
		this.port=port;
		this.iteration=iteration;
	}
	
	public void run(){
		
		while(iteration<25&&!this.isInterrupted()){
			iteration=time();
			if(iteration==-1){
				return;
			}
		}
		receiver=false;
		return;
		
	}
	
	private int time(){
		try {
			sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			return -1;
		}
		for(int x=0;x<packets.length;x++){
			try {
				socket.send(new DatagramPacket(packets[x].array(), packets[x].array().length,destination,port));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return iteration++;
	}
}
