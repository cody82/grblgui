package cody.grbl;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.badlogic.gdx.math.Vector3;


public class GrblConnection
{
    public GrblConnection(String portName) throws Exception
    {
        super();
        connect(portName);
    }
    
    SerialPort serialPort;
    public OutputStream out;
    public Vector3 toolPosition = new Vector3();
    
    void connect ( String portName ) throws Exception
    {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        if ( portIdentifier.isCurrentlyOwned() )
        {
            System.out.println("Error: Port is currently in use");
        }
        else
        {
            CommPort commPort = portIdentifier.open(this.getClass().getName(),2000);
            
            if ( commPort instanceof SerialPort )
            {
                serialPort = (SerialPort) commPort;
                serialPort.setSerialPortParams(9600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
                
                InputStream in = serialPort.getInputStream();
                out = serialPort.getOutputStream();
                               
                (new Thread(new SerialWriter(out))).start();
                
                serialPort.addEventListener(new SerialReader(in));
                serialPort.notifyOnDataAvailable(true);

            }
            else
            {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }     
    }
    
    /**
     * Handles the input coming from the serial port. A new line character
     * is treated as the end of a block in this example. 
     */
    public class SerialReader implements SerialPortEventListener 
    {
        private InputStream in;
        private byte[] buffer = new byte[1024];
        
        public SerialReader ( InputStream in )
        {
            this.in = in;
        }
        
        public void serialEvent(SerialPortEvent arg0) {
            int data;
          
            try
            {
                int len = 0;
                while ( ( data = in.read()) > -1 )
                {
                    if ( data == '\n' || data == '\r') {
                        break;
                    }
                    buffer[len++] = (byte) data;
                }
                
                if(len > 0){
                String output = new String(buffer,0,len);
                if(output.startsWith("MPos:")){
                	String[] s = output.split("[\\]\\[xyz,\\s]");
                	toolPosition.x = Float.parseFloat(s[1]);
                	toolPosition.y = Float.parseFloat(s[2]);
                	toolPosition.z = Float.parseFloat(s[3]);
                	//System.out.println(toolPosition.x + " " + toolPosition.y + " " + toolPosition.z);
                }
                }
            }
            catch ( IOException e )
            {
                e.printStackTrace();
                System.exit(-1);
            }             
        }

    }

    /** */
    public static class SerialWriter implements Runnable 
    {
        OutputStream out;
        
        public SerialWriter ( OutputStream out )
        {
            this.out = out;
        }
        
        public void run ()
        {
            try
            {                
                /*int c = 0;
                while ( ( c = System.in.read()) > -1 )
                {
                    this.out.write(c);
                } */
            	
            	while(true) {
            		this.out.write("?".getBytes());
            		Thread.sleep(1000/10, 0);
            	}
            }
            catch ( IOException e )
            {
                e.printStackTrace();
                System.exit(-1);
            } catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}            
        }
    }
    
    public void dispose() {
    	serialPort.close();
    }

}