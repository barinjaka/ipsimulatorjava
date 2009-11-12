import simulip.net.*;
import java.net.BindException;
import java.util.*;
import java.math.*;

public class P2Pserver extends Application {
	
	private Vector<Record> records = new Vector<Record>();
	private byte[] data = new byte[29];
	
	public void run(){
		try{
			simulip.net.DatagramSocket d = new simulip.net.DatagramSocket(this,530);
			simulip.net.DatagramPacket p = new simulip.net.DatagramPacket(data,13);
			while(true){
				d.receive(p);
				data = p.getData();
				if(data[0] == 'r'){
					system.out.println("recording request");
					NetworkAddress add = p.getAddress();					
					system.out.println("from " + add.getStrAddress());
					byte[] pnum = new byte[3];
					pnum[0] = 0;
					pnum[1] = data[1];
					pnum[2] = data[2];
					BigInteger pbi = new BigInteger(pnum);
					system.out.println("chat at port " + pbi.toString());
					
					byte[] blname = new byte[1];
					blname[0] = data[3];
					BigInteger it = new BigInteger(blname);
					byte[] bname = new byte[it.intValue()];
					for(int i = 0; i < bname.length; i++)
						bname[i] = data[i + 4];
					String name = new String(bname);
					system.out.println("name : " + name);
					byte[] twobyteport = new byte[2];
					twobyteport[0] = pnum[1];
					twobyteport[1] = pnum[2];
					Record rec = new Record(add.getStrAddress(), twobyteport,name);
					records.add(rec);
					byte[] ack = new byte[1];
					ack[0] = 'y';
					p.setData(ack);
				}
				else if (data[0] == 'c'){
					system.out.println("contacting request");
					byte[] blname = new byte[1];
					blname[0] = data[1];
					BigInteger it = new BigInteger(blname);
					byte[] bname = new byte[it.intValue()];
					for(int i = 0; i < bname.length; i++)
						bname[i] = data[i + 2];
					String name = new String(bname);
					system.out.println("ask for : " + name);
					Enumeration<Record> er = records.elements();
					Record rec = null;
					boolean found = false;
					while(!found && er.hasMoreElements()){
						rec = er.nextElement();
						found = rec.name.equals(name);
					}
					if(!found){
						byte[] nack = new byte[1];
						nack[0] = 'n';
						p.setData(nack);
					}
					else{
						byte[] resp = new byte[7];
						try{
							resp[0] = 'y';
							byte[] add = NetworkAddress.toBytes((new NetworkAddress(rec.address)).getBits());
							resp[1] = add[0];
							resp[2] = add[1];
							resp[3] = add[2];
							resp[4] = add[3];
							resp[5] = rec.port[0];
							resp[6] = rec.port[1];
							p.setData(resp);
							
							
						}
						catch(NetworkAddressFormatException nafe){
							
						}
					}
				}
				else {
					byte[] nack = new byte[1];
					nack[0] = 'n';
					p.setData(nack);
					system.out.println("unknow request ");
				}								
				p.setAddress(p.getAddress());
				p.setPort(p.getPort());
				d.send(p);
			}
			}catch(BindException b){
				system.out.println(b.getMessage());
			}		
		}
		
	
	
	private class Record {
		public String address;
		public byte[] port = new byte[2];
		public String name;
		
		public Record(String a, byte[] p, String n){
			address = new String(a);
			port = p;
			name = new String(n);
		}
		
	}

}
