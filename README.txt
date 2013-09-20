The program takes in arguments from the command line and responds to them. It also serves incoming requests.

COMMANDS IMPLEMENTED:

connect <ip> <tcp_port>		connect 128.205.36.8 3000
	When given a valid IP that is not found, connect's timout set to 12 sec will return and display a message saying " Host not found. Please try again"
info				info
show				show
send <conn_id> <message>		send 0 Hello there!! How are you?
sendto <ip> <udp_port> <message>	sendto 128.205.36.8 3000 Hello there!! How are you?
disconnect <conn_id>		disconnect 1

COMPILING :	$ make
		$ java Echoer <tcp-port> <udp-port>

PROJECT DONE BY: 

Vijayalakahsmi Srinivasaraghavan
Pavan Pujar

