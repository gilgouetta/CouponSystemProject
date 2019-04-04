//Test went successfuly

package com.database.utils;

import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;

import com.database.ConnectionPool;

public class ConnectionPoolTester {
	static List<Connection> connections = new LinkedList<>();
static long sleepTime = 200;


	static Runnable getConnections = new Runnable()  {
		public synchronized void run() {
			for (int i = 0; i < 50; i++) {
				Connection connection= ConnectionPool.getInstance().getConnection();
						connections.add(connection);
				System.out.println(connection + "Connection number " + (i+1));
				try {
					Thread.sleep(sleepTime);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	};
	
	static Runnable restoreConnections = new Runnable() { 
		
		public synchronized void run(){
			for (int i = 0; i < connections.size(); ) {
					ConnectionPool.getInstance().restoreConnection(connections.get(0));
					connections.remove(0);
					System.out.println("Connection restored");
					try {
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
		}
	
		
	;

	public static void main(String[] args) {
		Thread t1 = new Thread(getConnections);
		Thread t2 = new Thread(restoreConnections);
		try {
		t1.start();
			Thread.sleep(2000L);
			t2.start();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}

}
