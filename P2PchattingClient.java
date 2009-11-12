import java.math.BigInteger;

import simulip.net.*;


public class P2PchattingClient extends Application {
	
	public void run(){
		String ipserver = system.in.read();
		String name = system.in.read();
		
		DatagramSocket s = new DatagramSocket(this);
		
		
		if(name.length() <= 25){
			byte[] data = new byte[1+ 1 + name.length()];
			data[0] = 'c';
			data[1] = new Integer(name.length()).byteValue();
			for(int i = 0; i < name.length(); i++)
				data[i + 2] = (byte)name.charAt(i);
			try{
				DatagramPacket p = new DatagramPacket(data, data.length, ipserver, 530);
				s.send(p);
				data = new byte[33];
				p.setData(data);
				s.receive(p);
				data = p.getData();
				if(data[0] == 'y'){
					byte[] addchat = new byte[4];
					addchat[0] = data[1];
					addchat[1] = data[2];
					addchat[2] = data[3];
					addchat[3] = data[4];
					NetworkAddress ipchat = new NetworkAddress(addchat);
					byte[] port = new byte[3];
					port[0] = 0;
					port[1] = data[5];
					port[2] = data[6];
					BigInteger porti = new BigInteger(port);
					DatagramSocket sc = new DatagramSocket(this);
					String mess = new String();
					while(!mess.equals("by")){
						mess = system.in.read();
						Integer ml = new Integer(mess.length());
						byte[] messb = new byte[1 + mess.length()];
						messb[0] = ml.byteValue();
						for(int i = 0; i < mess.length(); i++)
							messb[i + 1] = (byte)mess.charAt(i);
						DatagramPacket ps = new DatagramPacket(messb, messb.length, ipchat.getStrAddress(), porti.intValue());
						sc.send(ps);
						messb = new byte[255];
						ps.setData(messb);
						sc.receive(ps);
						messb = ps.getData();
						Integer rml = new Integer(messb[0]);
						byte[] rmessb = new byte[rml];
						for(int i = 0; i < rml; i++)
							rmessb[i] = messb[i + 1];
						system.out.println(new String(rmessb));						
					}
					
					
				}
			}
			catch(NetworkAddressFormatException nafe){
				
			}
		}
		else
			system.out.println("name too long " + name + "\nname must be lower or equals than 25 ");
	}

}
