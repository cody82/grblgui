package cody.grbl;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cody.gcode.GCodeFile;
import cody.gcode.GCodeLine;

import com.badlogic.gdx.math.Vector3;


public class GrblStream
{
    public GrblStream(String portName, GCodeFile file) throws Exception
    {
    	gcode = file;
        connect(portName);
        stream(file);
    }
    public GrblStream(String portName) throws Exception
    {
        connect(portName);
        createReader();
    }
    
    GCodeFile gcode;
    
    SerialPort serialPort;
    public OutputStream out;
    public InputStream in;
    public Vector3 toolPosition = new Vector3();
    public Streamer streamer;
    Updater updater;
    Reader reader;
    Thread updater_thread;
    Thread streamer_thread;
    Thread reader_thread;
    
    synchronized static void write(OutputStream out, byte[] data) throws IOException {
    	out.write(data);
    }
    
    synchronized public void send(byte[] data) {
    	try {
			write(out, data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(13);
		}
    }
    public boolean isHold() {
    	return is_paused;
    }
    boolean is_paused = false;
    
    public boolean isStreaming() {
    	return streamer != null;
    }
    public void pause() {
    	try {
			write(out, new byte[]{(byte) (is_paused ? '~' : '!')});
			is_paused = !is_paused;
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(4);
		}
    }

    void stopStreamer() {
    	if(streamer != null) {
    		streamer.exit = true;
    		try {
    			streamer_thread.join();
    		} catch (InterruptedException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    			System.exit(9);
    		}
    		streamer = null;
    		streamer_thread = null;
    		System.out.println("Streamer stopped.");
    	}
    }
    
    void createReader() {
        reader = new Reader(in);
        reader_thread = new Thread(reader);
        reader_thread.start();
    }
    void stopReader() {
    	if(reader != null) {
    		reader.exit = true;
    		try {
    			reader_thread.join();
    		} catch (InterruptedException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    			System.exit(9);
    		}
    		reader = null;
    		reader_thread = null;
    		System.out.println("Reader stopped.");
    	}
    	
    }
    public void stream(GCodeFile file) {
    	Reader r = reader;
    	stopReader();
    	
        streamer = new Streamer(in, out, file);
        streamer.buffer = r.buffer;
        streamer_thread = new Thread(streamer);
        streamer_thread.start();
    }
    public void stopStream() {
    	if(streamer != null) {
    		stopStreamer();
    		createReader();
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
            
            serialPort = (SerialPort) commPort;
            serialPort.setSerialPortParams(9600,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
                
            in = serialPort.getInputStream();
            out = serialPort.getOutputStream();

            updater_thread = new Thread(updater = new Updater(out));
            updater_thread.start();
        }
    }

    
    public class Reader implements Runnable 
    {
        private InputStream in;
        private byte[] buffer = new byte[1024];
        public boolean exit = false;
        
        public Reader ( InputStream in )
        {
            this.in = in;
        }
        
        public void run ()
        {
            try
            {
        		int data;
        		int len = 0;

        		while(!exit) {
        		while(in.available() > 0 && (!exit || len != 0)) {
                    if (   ( data = in.read()) > -1)
                    {
                        if ( (data == '\n' || data == '\r')) {
                        	if(len > 0) {
                            String output = new String(buffer,0,len);
                        	len = 0;
                    		System.out.println("GrblReader Received: " + output);
                        	if(output.equals("ok")) {
                        	}
                        	else if(output.startsWith("MPos:")) {
                            	String[] s = output.split("[\\]\\[xyz,\\s]");
                            	toolPosition.x = Float.parseFloat(s[1]);
                            	toolPosition.y = Float.parseFloat(s[2]);
                            	toolPosition.z = Float.parseFloat(s[3]);
                        	}
                        	else if(output.startsWith("Grbl ")) {
                        	}
                        	else if(output.startsWith("'$' ")) {
                        	}
                        	else {
                        		System.out.println("GrblReader Error: " + output);
                        		System.exit(2);
                        		return;
                        	}
                        	}
                        	if(exit)
                        		return;
                        }
                        else
                        	buffer[len++] = (byte) data;
                    }
        		}
        		if(exit && len == 0)
        			return;

        		try {
					Thread.sleep(20, 0);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
	                System.exit(-1);
					
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
    
    public class Streamer implements Runnable 
    {
        private InputStream in;
        OutputStream out;
        private byte[] buffer = new byte[1024];
        GCodeFile gcode;
        public int currentLine;
        public boolean exit = false;
        
        public Streamer ( InputStream in ,OutputStream out, GCodeFile gcode)
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
                
                System.out.println("GrblStream: Start streaming...");
                
                currentLine = 0;
            	for(GCodeLine line : gcode.gcode) {
            		System.out.println("GrblStream Write: " + line.getContent());
            		send( (line.getContent() + "\n").getBytes());
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
            		if(exit) {
                		System.out.println("GrblStream thread exit.");
            			return;
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
        		Thread.sleep(1000, 0);
            	
            	while(!exit) {
            		send( "?".getBytes());
            		Thread.sleep(1000/10, 0);
            	}
            }catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(10);
			}            
        }
    }
    
    public void dispose() {
    	stopStreamer();
    	stopReader();
    	
    	if(updater != null) {
    		updater.exit = true;
    		try {
    			updater_thread.join();
    		} catch (InterruptedException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    			System.exit(9);
    		}
    		updater = null;
    		updater_thread = null;
    		System.out.println("Updater stopped.");
    	}
    	serialPort.close();
    	serialPort = null;
    }

}