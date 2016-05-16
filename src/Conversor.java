import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

public class Conversor implements SerialPortEventListener {
	
	private SerialPort serialPort;
	OutputStream commOutputStream;
	InputStream commInputStream;
	
	Servidor  servidor;
	DataOutputStream serverOutputStream;
	DataInputStream  serverInputStream;
	
	public Conversor(int port){
		initComm();
		try {
			servidor = new Servidor(port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void initComm() {
		@SuppressWarnings("rawtypes")
		Enumeration portList;
		CommPortIdentifier portId;
		serialPort = null;
		commOutputStream = null;
		commInputStream = null;
		
		portList = CommPortIdentifier.getPortIdentifiers();
		if (portList.hasMoreElements()) {
			portId = (CommPortIdentifier) portList.nextElement();
			if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				try {
					serialPort = (SerialPort) portId.open("Conversor", 2000);
				} catch (PortInUseException e) {
				}
				try {
					commOutputStream = serialPort.getOutputStream();
					commInputStream = serialPort.getInputStream();
	
				} catch (IOException e) {
				} catch (NullPointerException e) {
				}
				try {
					serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
							SerialPort.PARITY_NONE);
					serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
					serialPort.addEventListener(this);
					serialPort.notifyOnDataAvailable(true);
				} catch (UnsupportedCommOperationException e ) {
				} catch (TooManyListenersException e) {
				}
			}
		}
		
	}

	public class Servidor extends Thread
	{
	   private ServerSocket serverSocket;
	   
	   public Servidor(int port) throws IOException
	   {
	      serverSocket = new ServerSocket(port);
	      serverSocket.setSoTimeout(10000);
	   }

	   public void run()
	   {
	      while(true)
	      {
	         try
	         {
	            System.out.println("Esperando a cliente en puerto: " +
	            serverSocket.getLocalPort() + "...");
	            Socket server = serverSocket.accept();
	            System.out.println("Se ha conectado "
	                  + server.getRemoteSocketAddress());
	            serverInputStream = new DataInputStream(server.getInputStream());
	            serverOutputStream = new DataOutputStream(server.getOutputStream());
	            
	            while(true){
	            	char[] a = serverInputStream.readUTF().toCharArray();
	 	            for(char c : a){
	 	            	commOutputStream.write(c);
	 	            }
	            }
	         }catch(SocketTimeoutException s)
	         {
	            System.out.println("Socket timed out!");
	            serverInputStream = null;
	            serverOutputStream = null;
	            break;
	         }catch(IOException e)
	         {
	            e.printStackTrace();
	            break;
	         }
	      }
	   }
	}

	@Override
	public void serialEvent(SerialPortEvent arg0) {
		 int data;         
         try
         {
             while ( ( data = commInputStream.read()) > -1 ) {
                 if(serverOutputStream != null) serverOutputStream.writeByte(data);
             }
         }
         
         catch ( IOException e )
         {
             e.printStackTrace();
             System.exit(-1);
         }             
     }

}
