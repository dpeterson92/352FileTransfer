package sender;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.file.Files;

public class Sender {
	
	static char rb;
	static DatagramSocket socket;
	static InetAddress destination;
	static int port;

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		destination = InetAddress.getByName( args[0] );
		socket = new DatagramSocket(10101);
		port = 10101;

		System.out.println( "datagram target is " + destination + " port " + port );
		//System.out.print( "Enter a string>>" );
		int file = 1;
		rb='r';
		while ( file<args.length )
		{
			transferFile(args[file]);
			file++;
		
			
			//sendPacket = new DatagramPacket( s.getBytes(), s.length(), destination, port );
			//socket.send( sendPacket );
			//System.out.print( "Enter a string>>" );
		}
		byte[] b = new byte[2];
		ByteBuffer bb = ByteBuffer.wrap(b);
		bb.putChar('o');
		System.out.println( "Normal end of sender." );
		socket.close();
	}

	private static void transferFile(String filename) throws IOException {
		File currentFile =new File(System.getProperty("user.dir")+"/"+filename);
		byte[] data = Files.readAllBytes(currentFile.toPath());
		int offset = 0;  //holds the program's place in the current file
		short sN = 0;
		boolean receiver=true;
		while(offset<data.length){
			ByteBuffer[] packets = new ByteBuffer[10];	//To be passed into the SAT, holds the list of packets to be sent this round
			Thread timer = new Thread();
			//SenderAckThread sat = new SenderAckThread(socket,packets,destination,port,receiver, timer);
			//timer = new SenderAckTimer(socket,packets,destination,port,0,receiver);
			//timer.start();
			//sat.start();
			boolean filecomplete=false;
			for(int p=0;p<10;p++){
				
				byte[] pLoad = new byte[512];
				ByteBuffer payload = ByteBuffer.wrap(pLoad);		//Sets aside 100 byte arrays to act as packet payloads
				//System.out.println(p+ " "+payload.position());
				payload.putChar(rb);
				payload.putShort(sN);
				payload.putInt(filename.length());
				payload.put(filename.getBytes());
				int datastart=(Short.SIZE/8)+
						(Character.SIZE/8)+
						(Integer.SIZE/8)+
						(currentFile.toPath().toString().getBytes().length);
				for(int x=0;x<(512-datastart);x++){
					if(x+offset>=data.length){
						filecomplete=true;
						break;
					}
					payload.put(data[x+offset]);
				}
				System.out.println("Sending Packet #"+sN);
				socket.send(new DatagramPacket(payload.array(),payload.capacity(),destination,port));
				packets[p]=payload;
				sN++;
				
				offset+=(512-datastart);
				if(sN==Short.MAX_VALUE){
					sN=0;
					if(rb=='r'){
						rb='b';
					}else{
						rb='r';
					}
				}
				if(filecomplete){
					break;
				}
			}
			SenderAckThread sat = new SenderAckThread(socket,packets,destination,port,receiver, timer);
			timer = new SenderAckTimer(socket,packets,destination,port,0,receiver);
			sat.start();
			timer.start();
			while(sat.isAlive()&&timer.isAlive()){continue;}
			timer.interrupt();
			if(!receiver){
				return;
			}
		}
		
	}

}
