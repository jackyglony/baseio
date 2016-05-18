package test;

import com.gifisan.nio.Encoding;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.client.ClientTCPConnector;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.component.protocol.DatagramPacket;
import com.gifisan.nio.plugin.jms.TextMessage;
import com.gifisan.nio.plugin.rtp.RTPException;
import com.gifisan.nio.plugin.rtp.client.RTPClient;
import com.gifisan.nio.plugin.rtp.client.UDPReceiveHandle;

public class TestUDPConnector2 {
	
	
	public static void main(String[] args) throws Exception {


		final ClientTCPConnector connector = ClientUtil.getClientConnector();
		
		connector.connect();

		ClientSession session = connector.getClientSession();
		
		final String customerID = "002";
		
		final String otherCustomerID = "001";
		
		
		final RTPClient client = new RTPClient(session, new UDPReceiveHandle() {
			
			public void onReceiveUDPPacket(RTPClient client,DatagramPacket packet) {
				String data = new String(packet.getData(),Encoding.GBK);
				System.out.println(data);
			}

			public void onInvite(RTPClient client,TextMessage message, Configuration configuration) {
				
				String roomID = configuration.getProperty("roomID");
				
				try {
					client.joinRoom(roomID);
					
					client.inviteReply(otherCustomerID);
				} catch (RTPException e) {
					e.printStackTrace();
				}
				
				client.setRoomID(roomID);
				
				for (int i = 0; i < 1000; i++) {
					
					byte [] data = (customerID+i).getBytes();
					long timestamp = System.currentTimeMillis();
					int sequenceNO = i;
					int roomIDNo = Integer.valueOf(roomID);
					
					DatagramPacket packet = new DatagramPacket(timestamp,sequenceNO,roomIDNo,data);
					
					try {
						client.sendDatagramPacket(packet);
					} catch (RTPException e) {
						e.printStackTrace();
					}
					
					ThreadUtil.sleep(1000);
				}
			}

			public void onInviteReplyed(RTPClient client,TextMessage message, Configuration configuration) {
				
				for (int i = 0; i < 1000; i++) {
					
					DatagramPacket packet = new DatagramPacket((customerID+i).getBytes());
					
					try {
						client.sendDatagramPacket(packet);
					} catch (RTPException e) {
						e.printStackTrace();
					}
					
					ThreadUtil.sleep(1000);
				}
			}
			
		}, customerID);

		client.login("admin", "admin100");
		
		ThreadUtil.sleep(99999500);
		CloseUtil.close(client);
		CloseUtil.close(connector);
		
	}
}