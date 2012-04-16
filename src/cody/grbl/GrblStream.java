package cody.grbl;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cody.gcode.GCodeFile;
import cody.gcode.GCodeLine;
import cody.grbl.GrblConnection.SerialReader;

import com.badlogic.gdx.math.Vector3;


public class GrblStream
{
    public GrblStream(String portName, GCodeFile file) throws Exception
    {
        super();
    	gcode = file;
        connect(portName);
    }
    GCodeFile gcode;
    
    SerialPort serialPort;
    public OutputStream out;
    public Vector3 toolPosition = new Vector3();
    public Streamer streamer;
    Updater updater;
    
    synchronized static void write(OutputStream out, byte[] data) throws IOException {
    	out.write(data);
    }
    
    public boolean isHold() {
    	return is_paused;
    }
    boolean is_paused = false;
    
    public void pause() {
    	try {
			write(out, new byte[]{(byte) (is_paused ? '~' : '!')});
			is_paused = !is_paused;
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(4);
		}
    }
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
                               
                (new Thread(updater = new Updater(out))).start();
                
                Streamer r = streamer = new Streamer(in, out, gcode);
                //serialPort.addEventListener(r);
                //serialPort.notifyOnDataAvailable(true);
                (new Thread(r)).start();

            }
            else
            {
                System.out.println("Error: Only serial ports are handled by this example.");
            }
        }     
    }
    
    public class Streamer implements SerialPortEventListener, Runnable 
    {
        private InputStream in;
        OutputStream out;
        private byte[] buffer = new byte[1024];
        GCodeFile gcode;
        public int currentLine;
        public boolean exit = false;
        
        public Streamer ( InputStream in ,OutputStream out, GCodeFile gcode )
        {
            this.in = in;
            this.out = out;
            this.gcode = gcode;
        }
        
        public void run ()
        {
            try
            {
        		int data;
        		Thread.sleep(2000, 0);
                while (in.available() > 0) {
                	in.read();
                }
                
                System.out.println("GrblStream: Start streaming...");
                
                currentLine = 0;
            	for(GCodeLine line : gcode.gcode) {
            		if(exit)
            			break;
            		System.out.println("GrblStream Write: " + line.getContent());
            		write(out, (line.getContent() + "\n").getBytes());
                    currentLine ++;
            		
            		int len = 0;
                    while ( ( data = in.read()) > -1 )
                    {
                        if ( (data == '\n' || data == '\r')) {
                        	if(len > 0) {
                            String output = new String(buffer,0,len);
                        	len = 0;
                    		System.out.println("GrblStream Received: " + output);
                        	if(output.equals("ok")) {
                        		break;
                        	}
                        	else if(output.startsWith("MPos:")) {
                            	String[] s = output.split("[\\]\\[xyz,\\s]");
                            	toolPosition.x = Float.parseFloat(s[1]);
                            	toolPosition.y = Float.parseFloat(s[2]);
                            	toolPosition.z = Float.parseFloat(s[3]);
                        	}
                        	else {
                        		System.out.println("GrblStream Error: " + output);
                        		System.exit(2);
                        		return;
                        	}
                        	}
                        }
                        else
                        	buffer[len++] = (byte) data;
                    }
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

    public class Updater implements Runnable 
    {
        OutputStream out;
        public boolean exit = false;
        
        public Updater ( OutputStream out )
        {
            this.out = out;
        }
        
        public void run ()
        {
            try
            {
        		Thread.sleep(3000, 0);
            	
            	while(!exit) {
            		write(out, "?".getBytes());
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
    	streamer.exit = updater.exit = true;
    	serialPort.close();
    }

}