package receiver;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Random;

public class Receiver {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		DatagramSocket	socket = new DatagramSocket( 10101 );
		byte []		payload = new byte[512];
		DatagramPacket	receivePacket = new DatagramPacket( payload, payload.length );
		boolean firstPacket=true;
		short currentSN = -1;
		char currentRB = 0;
		Random random = new Random();
		socket.setReuseAddress( true );

		Thread t = new Thread();
		InetAddress senderAddr=null;

		
		for( ;; )
		{
			
			socket.receive( receivePacket );
			if(random.nextInt(100)>(Integer.parseInt(args[0]))){continue;}

			senderAddr = receivePacket.getAddress();
			
			
			ByteBuffer bb = ByteBuffer.wrap(receivePacket.getData());
			char packetRB = bb.getChar(0);
			if(packetRB=='o'){
				t.interrupt();
				return;
			}
			short packetSN = bb.getShort(2);
			if(firstPacket){
				currentSN=packetSN;
				currentRB=packetRB;
				firstPacket=false;
				if(!t.isAlive()){
					t = new Timer(socket,currentSN,currentRB,senderAddr,Integer.parseInt(args[0]));
					t.start();
				}
				build(bb);
			}else{
				if(packetSN==currentSN+1){
					currentSN=packetSN;
					currentRB=packetRB;
					if(t.isAlive()){
						t.interrupt();
						t = new Timer(socket,currentSN,currentRB,senderAddr,Integer.parseInt(args[0]));
						t.start();
					}
					//System.out.println("Adding packet #"+currentSN+" to file");
					build(bb);
				}
			}
			
			
		}
	}

	private static synchronized void build(ByteBuffer bb) {
		// TODO Auto-generated method stub
		int fileNameLength=bb.getInt(4);
		byte[] fnArr = new byte[fileNameLength];
		for(int x=0;x<fileNameLength;x++){
			fnArr[x]=bb.get(x+8);
		}
		String fileName = new String(fnArr);
		File buildFile = new File(fileName);
		Path buildFilePath = buildFile.toPath();
		
		int offset=(Character.SIZE/8)+(Short.SIZE/8)+
					(Integer.SIZE/8)+fileName.length();
		
		//byte[] data = new byte[512-offset];
		ArrayList<Byte> data = new ArrayList<Byte>();
		for(int x=0;x<512-offset;x++){
			data.add(bb.get(x+offset));
		}
		data.trimToSize();
		Byte[] dataArray = data.toArray(new Byte[data.size()]);
		byte[] bytes = new byte[dataArray.length];
		for(int x=0;x<bytes.length;x++){
			bytes[x]=dataArray[x].byteValue();
		}
		
		
		try (OutputStream out = new BufferedOutputStream(
			      Files.newOutputStream(buildFilePath, CREATE, APPEND))) {
			      out.write(bytes, 0, bytes.length);
			    } catch (IOException x) {
			      System.err.println(x);
			    }
	}
	/*
	private static void add(ByteBuffer[] packets, ByteBuffer bb){
		for(int x=0;x<packets.length;x++){
			ByteBuffer b = packets[x];
			char rbLocal = b.getChar(0);
			short sNLocal = b.getShort(2);
			char rb = bb.getChar(0);
			short sN = bb.getShort(2);
			if(sN<sNLocal){
				
				return;
			}
		}
	}
	*/

}
