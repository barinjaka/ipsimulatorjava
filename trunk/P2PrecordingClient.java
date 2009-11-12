import simulip.net.*;

import java.math.*;

public class P2PrecordingClient extends Application {

	public void run(){
		try{
		
			String ipserver = system.in.read();
			String name = system.in.read();
			String port = system.in.read();
			BigInteger intport = null;
			byte[] data = new byte[29];
			try{
				intport = new BigInteger(port);
				int pn = intport.intValue();
				data[0] = 'r';
				if(pn > 65535 || pn < 0)
					throw new NumberFormatException(intport.toString());				
				byte[] portbyte = new byte[2];
				portbyte = intport.toByteArray();
				if(pn <= 127){
					data[1] = 0;
					data[2] = portbyte[0];
				}
				else if(pn >= 128 && pn <= 32767){
					data[1] = portbyte[0];
					data[2] = portbyte[1];
				}
					
				else {
					data[1] = portbyte[1];
					data[2] = portbyte[2];
				}
				Integer nl = new Integer(name.length());
				if(nl <= 25){
					data[3] = nl.byteValue();
					byte[] bname = new byte[nl];
					bname = name.getBytes();
					for(int i = 0; i < nl; i++)
						data[i+4] = bname[i];
					simulip.net.DatagramSocket d = new simulip.net.DatagramSocket(this);
					simulip.net.DatagramPacket p = new simulip.net.DatagramPacket(data,data.length,ipserver,530);
					d.send(p);
					d.receive(p);
					byte[] resp = new byte[1];
					resp[0] = p.getData()[0];
					if(resp[0] == 'y'){
						system.out.println(name + " is ready to chat\nand wait for someone");
						simulip.net.DatagramSocket dchat = new simulip.net.DatagramSocket(this,pn);
						String receving = new String("");
						while(!receving.equals("by")){
							byte[] rec = new byte[255];
							simulip.net.DatagramPacket pr = new simulip.net.DatagramPacket(rec,rec.length);
							dchat.receive(pr);
							rec = pr.getData();
							byte[] blmess = new byte[1];
							blmess[0] = rec[0];
							BigInteger itlmess = new BigInteger(blmess);
							byte[] rbmess = new byte[itlmess.intValue()];
							for(int i = 0; i < rbmess.length; i++)
								rbmess[i] = rec[i + 1];
							receving  = new String(rbmess);
							system.out.println(receving);	
							String respm = system.in.read();
							Integer rl = respm.length();
							byte[] respmb = new byte[1 + respm.length()];
							respmb[0] = rl.byteValue();
							for(int i = 0; i < respm.length(); i++)
								respmb[i+ 1] = (byte)respm.charAt(i);
							pr.setData(respmb);
							pr.setAddress(pr.getAddress());
							pr.setPort(pr.getPort());
							dchat.send(pr);
						}
					}
					else
						system.out.println(name + " is not ready to chat\n");
				}
				else
					system.out.println("name to long :  " + name + "\nname must be lower or equals than 25 ");
					
				
			}
			catch(NumberFormatException nf){
				system.out.println("bad number " + nf.getMessage());
			}
			
		}
		catch(Exception e){
			system.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
}
