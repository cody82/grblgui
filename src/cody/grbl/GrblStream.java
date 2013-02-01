package cody.grbl;

import j.extensions.comm.SerialComm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cody.gcode.GCodeCommand;
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
    
    SerialComm serialPort;
    public OutputStream out;
    public InputStream in;
    public Vector3 toolPosition = new Vector3();
    public Vector3 machinePosition = new Vector3();
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

    public void stream() {
    	stream(gcode);
    }
    public void stream(GCodeFile file) {
    	gcode = file;
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
    	SerialComm[] ports = SerialComm.getCommPorts();
    	for(SerialComm c : ports) {
    		String name = c.getSystemPortName();
    		System.err.println(name);
    		if(name.equals(portName)) {
    			serialPort = c;
    			serialPort.setComPortParameters(9600, 8, SerialComm.ONE_STOP_BIT, SerialComm.NO_PARITY);
    			if(!serialPort.openPort())
    				throw new Exception("cant open port " + portName);
                in = serialPort.getInputStream();
                out = serialPort.getOutputStream();
                updater_thread = new Thread(updater = new Updater(out));
                updater_thread.start();
    			return;
    		}
    	}
    	throw new Exception("port does not exist: " + portName);
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

				while (!exit) {
					while (in.available() > 0 && (!exit || len != 0)) {
						if ((data = in.read()) > -1) {
							if ((data == '\n' || data == '\r')) {
								if (len > 0) {
									String output = new String(buffer, 0, len);
									len = 0;
									System.out.println("GrblReader Received: "
											+ output);
									if (output.equals("ok")) {
									} else if (output.contains("MPos:[")) {
										String mpos = output.substring(output.indexOf("MPos:[") + 6);
										String[] s = mpos.split("[\\]\\[xyz,\\s]");
										machinePosition.x = Float.parseFloat(s[0]);
										machinePosition.y = Float.parseFloat(s[1]);
										machinePosition.z = Float.parseFloat(s[2]);
										String wpos = output.substring(output.indexOf("WPos:[") + 6);
										String[] s2 = wpos.split("[\\]\\[xyz,\\s]");
										toolPosition.x = Float.parseFloat(s2[0]);
										toolPosition.y = Float.parseFloat(s2[1]);
										toolPosition.z = Float.parseFloat(s2[2]);
									} else if (output.startsWith("<")) {
										System.out.println("Wrong grbl-version?(TODO)");
										System.exit(2);
										return;
									} else if (output.startsWith("Grbl ")) {
									} else if (output.startsWith("'$' ")) {
									} else {
										System.out.println("GrblReader Error: "+ output);
										System.exit(2);
										return;
									}
								}
								if (exit)
									return;
							} else
								buffer[len++] = (byte) data;
						}
					}
					if (exit && len == 0)
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
                int errors = 0;
            	lines:for(GCodeLine line : gcode.gcode) {
            		for(GCodeCommand cmd : line.commands) {
            			if(cmd.cmd == 'T' || cmd.cmd == 'M') {
                    		System.out.println("GrblStream ignored line: " + line.getContent());
            				currentLine++;
            				continue lines;
            			}
            		}
            		
            		System.out.println("GrblStream Write: " + line.getContent());
            		send( (line.getContent() + "\n").getBytes());
                    currentLine ++;
            		
                    boolean ok = false;
            		int len = 0;
            		while(!ok) {
            			
                    while ( ( data = in.read()) > -1 )
                    {
                        if ( (data == '\n' || data == '\r')) {
                        	if(len > 0) {
                            String output = new String(buffer,0,len);
                        	len = 0;
                    		System.out.println("GrblStream Received: " + output);
                        	if(output.equals("ok")) {
                        		ok = true;
                        		break;
                        	} else if (output.contains("MPos:[")) {
								String mpos = output.substring(output.indexOf("MPos:[") + 6);
								String[] s = mpos.split("[\\]\\[xyz,\\s]");
								machinePosition.x = Float.parseFloat(s[0]);
								machinePosition.y = Float.parseFloat(s[1]);
								machinePosition.z = Float.parseFloat(s[2]);
								String wpos = output.substring(output.indexOf("WPos:[") + 6);
								String[] s2 = wpos.split("[\\]\\[xyz,\\s]");
								toolPosition.x = Float.parseFloat(s2[0]);
								toolPosition.y = Float.parseFloat(s2[1]);
								toolPosition.z = Float.parseFloat(s2[2]);
							} else if (output.startsWith("<")) {
								System.out.println("Wrong grbl-version?(TODO)");
								System.exit(2);
								return;
							} else {
                        		System.out.println("GrblStream Error: " + output);
                        		errors++;
                        		if(errors != 1) {
	                        		System.exit(2);
	                        		return;
                        		}
                        		else
                            		System.out.println("GrblStream Error ignored.");
                        	}
                        	}
                        }
                        else
                        	buffer[len++] = (byte) data;
                    }
                    try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            		}
                    if(!ok) {
                    	System.out.println("GrblStream fail.");
                    	System.out.println("GrblStream thread exit.");
                		System.exit(6);
                		return;
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
    	serialPort.closePort();
    	serialPort = null;
    }

}