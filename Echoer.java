import java.net.*;
import java.net.InetAddress;
import java.io.*;
import java.util.*;
import java.lang.Thread.*;


class ThreadHandler extends Thread
{
	Socket newsock;
	Socket infosocket;
	ThreadHandler(Socket s)
	{	
		newsock=s;
	}
	public void run()
	{
		try
		{
			infosocket = new Socket("8.8.8.8",53);
			BufferedReader inStream = new BufferedReader(new InputStreamReader(newsock.getInputStream()));
			String message = inStream.readLine();
			PrintWriter outp = new PrintWriter(newsock.getOutputStream(),true); 
			
			while(message != null)
			{
				if(message.equals("connect"))
				{
					System.out.println("\nconnection request from  " + infosocket.getLocalAddress().getHostAddress() + "  accepted\n");
					outp.println("connection accepted");
				}
				else if(message.equals("disconnect"));
				else
				{
					System.out.println("\tEchoing \n\n\t\tMessage: " + message + "\n\n\t to: IP = " + infosocket.getLocalAddress().getHostAddress() + "\n\t type = TCP");
					outp.println("" + message);
				}
				if(newsock.isConnected())
					message = inStream.readLine();
			}
		}
		catch(SocketException e)
		{
			System.out.println("\nConnection severed by remote host\n");
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				System.out.println("\nconnection from " + infosocket.getLocalAddress().getHostAddress() + " disconnected\n");
				newsock.close();
			}
			catch(Exception e)
			{
			}
		}
	}
}

class IncomingConnectionHandler extends Thread
{
	int tcp_port;
	IncomingConnectionHandler(int port)
	{
		tcp_port=port;
	}
	
	public void run()
	{
		int i=0;
		try
		{
			ServerSocket serverSocket = new ServerSocket(tcp_port);
			for(;;)
			{
				Socket newsock = serverSocket.accept();
				Thread t = new ThreadHandler(newsock);
				t.start();
			}	
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}

class UserInputHandler extends Thread
{
	int tcp_port;
	int udp_port;
	
	Socket[] client = new Socket[7];
	static int[] free = new int[7];
	PrintWriter outp;
	
	int TCP_outgoing_connection_count = 0;
	int TCP_incoming_connection_count = 0;
	String[] user_commands = new String[5];
	int input_words_count;
	String message;
	Socket infosocket;
	
	UserInputHandler(int tport,int uport)
	{
		tcp_port=tport;
		udp_port=uport;
		try
		{
			infosocket = new Socket("8.8.8.8",53);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public int validate_IP(String ip)
	{
		if(!ip.substring(0,1).matches("[0-9]"))
			return 2;
			
		StringTokenizer st1 = new StringTokenizer(ip,".");
		while (st1.hasMoreTokens())
		{
			if(Integer.parseInt(st1.nextToken()) > 254)
			{
				System.out.println("Enter a valid IP address (0-255)");
				return 0;
			}
		}
		return 1;
	}
	
	public static boolean validate_port(String port)
	{
		try
		{
			int port_num = Integer.parseInt(port);
			
			if(port_num > 60000)
			{
				System.out.println("Enter a valid port number (1 - 60000)");
				return false;
			}
			return true;
		}
		catch(NumberFormatException e)
		{
			System.out.println("\nNo alphabets allowed. Enter a valid port number (1 - 600000) ");
			return false;
		}
	}
	
	public void info()
	{
		try
		{
			System.out.println("\tIP address\thostname\t\t  udp port\ttcp port ");
			System.out.println("\t----------\t--------\t\t   -------\t--------");
			Socket info_socket = new Socket("8.8.8.8",53);
			System.out.println( "\t" + info_socket.getLocalAddress().getHostAddress() + 
								"\t" + InetAddress.getLocalHost().getHostName() +
								"\t   " + udp_port + 
								"\t\t" + tcp_port
							);
		}
		catch(UnknownHostException e)
		{
			System.out.println("Unable to find host");
		}
		catch(IOException e)
		{
		
		}
	}
	
	public void show()
	{
		try
		{
			if(TCP_outgoing_connection_count == 0)
				System.out.println("There are no Outgoing connections");
			else
			{
				System.out.println("\nconn. ID  |     IP     |   hostname \t\t      |  local port  | remote port\n ");
				for(int i=0;i<7;i++)
				{
					if(free[i] == 1)
					{
						InetSocketAddress abc = (InetSocketAddress)client[i].getRemoteSocketAddress();
						String temp = abc.toString().substring(1,abc.toString().indexOf(':'));
						
						String str = abc.getHostName();
						System.out.println("    " +  i + "     " 
												+ temp + "  "
												+ str + "\t    "
												+ client[i].getLocalPort() + "\t  "
												+ client[i].getPort()
											);
					}
				}	
			}
		}
		catch(Exception e)
		{
		}
	}
	public void run()
	{	
		BufferedReader d = new BufferedReader(new InputStreamReader(System.in));
		try
		{
			do
			{
				System.out.print("\n $ ");
				StringTokenizer st = new StringTokenizer(d.readLine());
				input_words_count = 0;
				while (st.hasMoreTokens())
					user_commands[input_words_count++] = st.nextToken();
				System.out.println();
				
				if(user_commands[0].equals("exit"))
				{
					//close all open threads. send info to connected peers
					System.exit(-1);
				}
				else if(user_commands[0].equals("info"))
				{
					if(input_words_count != 1)
					{
						System.out.println("\nCommand Usage: <info> \n");
						continue;
					}
					else
						info();
				}
				else if(user_commands[0].equals("connect"))
				{
					String ip = user_commands[1];
					int port = 0;
					
					if(input_words_count != 3)
					{
						System.out.println("\nCommand Usage: connect <ip> <port> \n");
						continue;
					}
						
					int ip_valid = validate_IP(ip);
					if(ip_valid == 2)
					{
						try
						{
							ip = InetAddress.getByName(ip).getHostAddress().toString();
						}
						catch(UnknownHostException e)
						{
							System.out.println("Unable to find host. Please try again");
							continue;
						}
					}
					else if(ip_valid == 0)
						continue;
					else;
					
					boolean port_valid = validate_port(user_commands[2]);					
					if(port_valid == true)
						port = Integer.parseInt(user_commands[2]);
					else
						continue;
					
					if(	
						(ip.equals("127.0.0.1") || ip.equals("127.0.1.1") ||
						(ip.equals(infosocket.getLocalAddress().getHostAddress().toString()))) 
						&& port == tcp_port 
					  )
						System.out.println("Cannot connect to self..");
						
					else if(TCP_outgoing_connection_count == 7)
						System.out.println("SEVEN connections already exist. Cannot create another connection");
						
					else
					{
						try
						{
							for(int i=0;i<7;i++)
							{
								if(TCP_outgoing_connection_count > 0)
								{
									InetSocketAddress abc = (InetSocketAddress)client[i].getRemoteSocketAddress();
									String temp = abc.toString().substring(1,abc.toString().indexOf(':'));
								
									if((client[i].getPort() == port)
										&& ( client[i].getInetAddress().toString().equals("/" + ip) || infosocket.getLocalAddress().getHostAddress().toString().equals(ip) || temp.equals(ip)) )
									{
										System.out.println("Connection to this host already exists. Cannot create a duplicate connection");
										break;
									}
								}
								if(free[i] == 0)
								{	
									SocketAddress sockaddr = new InetSocketAddress(ip,port);
									client[i] = new Socket();
									client[i].connect(sockaddr,10000);
									System.out.println("\tConnecting to " + user_commands[1] + " in port " + user_commands[2] + "...");
									outp = new PrintWriter(client[i].getOutputStream(),true); 
									outp.println("connect");
									BufferedReader inp = new BufferedReader(new InputStreamReader(client[i].getInputStream()));
									String inLine=inp.readLine().toString();
									if(inLine!= null)
									{
										System.out.println("Server says: " + inLine);
									}
									else
									{
										System.out.println("Unable to find host");
										break;
									}
									free[i] = 1;
									TCP_outgoing_connection_count++;
									break;
								}
							}
						}
						catch(NullPointerException e)
						{
							System.out.println("null pointer");
							e.printStackTrace();
						}
						catch(UnknownHostException e)
						{
							System.out.println("Unable to find host. Please try again");
							continue;
						}
						catch(SocketException e)
						{
							System.out.println("Unable to find host");
							
						}
						catch(SocketTimeoutException e)
						{
							System.out.println("Unable to find host.. Please try again");
							
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
					}
				}
				else if(user_commands[0].equals("show"))
				{
					if(input_words_count != 1)
					{
						System.out.println("\nCommand Usage: sendto <ip> <port> <message> \n");
						continue;
					}
					else
						show();
				}
				else if(user_commands[0].equals("send"))
				{
					if(input_words_count != 3)
					{
						System.out.println("\nCommand Usage: send <conn_id> <message> \n");
						continue;
					}
					
					int TCPconnection_ID = Integer.parseInt(user_commands[1]);
					
					message = "";
					for(int i=2;i<input_words_count;i++)
						message += user_commands[i] + " ";
					if(free[TCPconnection_ID] == 1)
					{
						InetAddress ip = client[TCPconnection_ID].getInetAddress();
						int port = client[TCPconnection_ID].getPort();
						PrintWriter outp = new PrintWriter(client[TCPconnection_ID].getOutputStream(),true); 
						outp.println("" + message);
						BufferedReader inStream = new BufferedReader(new InputStreamReader(client[TCPconnection_ID].getInputStream()));
						String received_message = inStream.readLine();
						System.out.println("Echoed Message: " + received_message);
					}					
					else
					{
						System.out.println("No such connection exists");
						show();
					}
				}
				else if(user_commands[0].equals("sendto"))
				{
					try
					{
						String ip = user_commands[1];
						if(input_words_count !=4)
						{
							System.out.println("\nCommand Usage: sendto <ip> <port> <message> \n");
							continue;
						}	
					
						int valid_ip = validate_IP(user_commands[1]);
						int sendto_port;
						boolean valid_port = validate_port(user_commands[2]);
						if(valid_port == true)
							sendto_port = Integer.parseInt(user_commands[2]);
						else
							continue;
						if(valid_ip == 2 || valid_ip == 1)
						{
							if(	( ip.equals("127.0.0.1")  	|| 		ip.equals("127.0.1.1") 		||
										ip.equals(infosocket.getLocalAddress().getHostAddress().toString()) )  &&
									(udp_port == sendto_port)
							  )
							{
								System.out.println("Cannot sendto self");
								continue;
							}
						}
						if(valid_ip==0)
							continue;
							
						String udp_message = "";
						for(int i=3;i<input_words_count;i++)
							udp_message += user_commands[i] + " ";
						
						DatagramSocket clientSocket = new DatagramSocket(); 
						
						byte[] sendData = udp_message.getBytes();
						InetAddress receiver_IP = InetAddress.getByName(ip);
						System.out.print("  Sending message \n\n\t" + udp_message + "\n\n  to " + user_commands[1] + 
										" at port " + sendto_port + "...  ");
						
						DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,receiver_IP,sendto_port); 
						clientSocket.send(sendPacket); 
						clientSocket.close();
						System.out.println("Message sent");
					}
					catch(NumberFormatException e)
					{
						System.out.println("\nNo alphabets allowed. Enter a valid IP address");
					}
				}
				
				else if(user_commands[0].equals("disconnect"))
				{
					int disconnect_ID;
					if(input_words_count != 2)
					{
						System.out.println("\nCommand Usage: disconnect <conn_id> \n");
						continue;
					}
					if(user_commands[1].substring(0,1).matches("[0-6]"))
						disconnect_ID = Integer.parseInt(user_commands[1]);
					else
					{
						System.out.println("Enter a valid connection ID");
						continue;
					}
					
					if(TCP_outgoing_connection_count == 0)
					{
						System.out.println("There are no outgoing TCP connections");
						continue;
					}
					
					free[disconnect_ID] = 0;
					PrintWriter outp = new PrintWriter(client[disconnect_ID].getOutputStream(),true); 
					outp.println("disconnect");
					System.out.println("\tDisconnected from " + disconnect_ID);
					TCP_outgoing_connection_count--;
					client[disconnect_ID].close();
					show();
					
				}
				else if(user_commands[0] == "");
				else
				{
					System.out.println("   Unknown Command ");
				}
			}while(true);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		catch(NullPointerException e)
		{
		}
	}
}
public class Echoer implements Runnable 
{
	DatagramSocket udpSocket;
	int udpport;
	Socket infosocket;
	Echoer(DatagramSocket s,int port)
	{
		udpport = port;
		udpSocket = s;
		try
		{
			infosocket = new Socket("8.8.8.8",53);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		try
		{
			byte[] buffer = new byte[500];
			DatagramPacket packet = new DatagramPacket(buffer, 500);
			udpSocket.receive(packet);
			String message = new String(buffer).trim();
			System.out.println("\tEchoing \n\n\t\tMessage: "  + message + "\n\n\t  to: IP = " + infosocket.getLocalAddress().getHostAddress() + "\n\t type = UDP");
			udpSocket.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	static int send_code = 0;
	public static void main(String [] args)
	{
		try
		{
			if(args.length != 2)
			{
				System.out.println("\nUsage: java Echoer <tcp_port> <udp_port>\n");
				System.exit(0);
			}
			
			boolean tcp_port_valid = UserInputHandler.validate_port(args[0]);
			boolean udp_port_valid = UserInputHandler.validate_port(args[1]);
			
			if(tcp_port_valid == false || udp_port_valid == false)
				System.exit(0);
				
			int tcp_port = Integer.parseInt(args[0]);
			int udp_port = Integer.parseInt(args[1]);
			
			if(tcp_port < 1024 || udp_port < 1024)
			{
				System.out.println("Port numbers less than 1024 are used by the OS. Enter a valid port number greater then 1024");
				System.exit(0);
			}
			
			
			Thread UserInputThread = new UserInputHandler(tcp_port,udp_port);
			Thread IncomingConnectionThread = new IncomingConnectionHandler(tcp_port);
			
			DatagramSocket mySocket = new DatagramSocket(udp_port);
			Runnable r = new Echoer(mySocket,udp_port);
			Thread udpThread = new Thread(r);
			udpThread.start();
			
			IncomingConnectionThread.start();
			UserInputThread.start();	
			
		}
		catch(NumberFormatException e)
		{
			System.out.println("\nNo alphabets allowed. Enter a valid port number (1 - 600000) ");
			System.exit(0);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
}
