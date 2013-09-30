package cody.grbl.android;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector3;
import com.hoho.android.usbserial.driver.UsbSerialDriver;

import cody.gcode.GCodeCommand;
import cody.gcode.GCodeFile;
import cody.gcode.GCodeLine;
import cody.grbl.GrblStreamInterface;
import cody.grbl.GrblStreamListener;

public class AndroidGrblStream implements GrblStreamInterface {
	UsbSerialDriver driver;
	FileHandle logfile;
	GrblStreamListener listener;
	
	public AndroidGrblStream(UsbSerialDriver _driver) throws Exception {
		driver = _driver;
		logfile = Gdx.files.external("grblgui-log.txt");
		log("serial constructor");
        connect("");
        createReader();
	}

	public void setListener(GrblStreamListener listener) {
		this.listener = listener;
	}
	
	void log(String s) {
		logfile.writeString(s + "\r\n", true);
	}
    GCodeFile gcode;
    
    private Vector3 toolPosition = new Vector3();
    private Vector3 machinePosition = new Vector3();
    private Streamer streamer;
    Updater updater;
    Reader reader;
    Thread updater_thread;
    Thread streamer_thread;
    Thread reader_thread;
    
    public int getCurrentLine() {
    	if(streamer != null) {
    		return streamer.currentLine;
    	}
    	else {
    		return 0;
    	}
    }
    synchronized void write(byte[] data) throws IOException {
    	driver.write(data, data.length);
    }
    
    synchronized public void send(byte[] data) {
    	try {
			write(data);
		} catch (IOException e) {
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
			write(new byte[]{(byte) (is_paused ? '~' : '!')});
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
    			e.printStackTrace();
    			System.exit(9);
    		}
    		streamer = null;
    		streamer_thread = null;
    		System.out.println("Streamer stopped.");
    	}
    }
    
    void createReader() {
    	log("serial: create reader");
        reader = new Reader(driver);
        reader_thread = new Thread(reader);
        reader_thread.start();
    }
    void stopReader() {
    	if(reader != null) {
    		reader.exit = true;
    		try {
    			reader_thread.join();
    		} catch (InterruptedException e) {
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
    	
        streamer = new Streamer(driver, file);
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
    	driver.open();
    	driver.setBaudRate(9600);

    	log("serial port open");

        updater_thread = new Thread(updater = new Updater(driver));
        updater_thread.start();
    }

    
    public class Reader implements Runnable 
    {
        private UsbSerialDriver in;
        private byte[] buffer = new byte[1024];
        public boolean exit = false;
        
        public Reader ( UsbSerialDriver in )
        {
            this.in = in;
        }
        
        public void run ()
        {
            try
            {
        		byte data;
        		int len = 0;

				while (!exit) {
					//System.out.println("1");
					byte[] buffer2 = new byte[1024];
					int bytesread;
					while ((bytesread=in.read(buffer2, 1024)) > 0 && (!exit || len != 0)) {
						//System.out.println("2");
						for(int i=0;i<bytesread;++i) {
							data = buffer2[i];
					    	//log("serial: " + new String(new byte[]{data},0,1));
							//System.out.println("3");
							//System.out.println(data);
							if ((data == '\n' || data == '\r')) {
								//System.out.println("4");
								if (len > 0) {
									String output = new String(buffer, 0, len);
									len = 0;
									log("GrblReader Received: "+ output);
									if(listener != null)
										listener.received(output);
									
									if (output.equals("ok")) {
									} else if (output.contains("MPos:[")) {
										String mpos = output.substring(output.indexOf("MPos:[") + 6);
										String[] s = mpos.split("[\\]\\[xyz,\\s]");
										getMachinePosition().x = Float.parseFloat(s[0]);
										getMachinePosition().y = Float.parseFloat(s[1]);
										getMachinePosition().z = Float.parseFloat(s[2]);
										String wpos = output.substring(output.indexOf("WPos:[") + 6);
										String[] s2 = wpos.split("[\\]\\[xyz,\\s]");
										getToolPosition().x = Float.parseFloat(s2[0]);
										getToolPosition().y = Float.parseFloat(s2[1]);
										getToolPosition().z = Float.parseFloat(s2[2]);
									} else if (output.startsWith("<")) {
										String mpos = output.substring(output.indexOf("MPos:") + 5);
										String[] s = mpos.split(",");
										getMachinePosition().x = Float.parseFloat(s[0]);
										getMachinePosition().y = Float.parseFloat(s[1]);
										getMachinePosition().z = Float.parseFloat(s[2]);
										String wpos = output.substring(output.indexOf("WPos:") + 5);
										String[] s2 = wpos.split("[,>]");
										getToolPosition().x = Float.parseFloat(s2[0]);
										getToolPosition().y = Float.parseFloat(s2[1]);
										getToolPosition().z = Float.parseFloat(s2[2]);
									} else if (output.startsWith("Grbl ")) {
									} else if (output.startsWith("'$' ")) {
									} else if (output.startsWith("['$H'|'$X' to unlock]")) {
										//send("$X\n".getBytes());
									} else if (output.startsWith("[Caution: Unlocked]")) {
										System.out.println("Unlocked!");
									} else {
										log("GrblReader Error: "+ output);
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
					e.printStackTrace();
			    	log("serial: reader exception 12234554");
	                System.exit(-1);
					
				}
        		}
            }
            catch ( Exception e )
            {
                e.printStackTrace();
		    	log("serial: reader exception 4545");
		    	log(e.toString());
		    	for(int i=0;i<e.getStackTrace().length;++i)
		    		log(e.getStackTrace()[i].toString());
                System.exit(-1);
            }    
        }
    }
    
    public class Streamer implements Runnable 
    {
        private byte[] buffer = new byte[1024];
        GCodeFile gcode;
        public int currentLine;
        public boolean exit = false;
        UsbSerialDriver port;
        
        public Streamer ( UsbSerialDriver port, GCodeFile gcode)
        {
            this.port = port;
            this.gcode = gcode;
        }
        
        public void run ()
        {
            try
            {
        		byte data;
                
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

    					byte[] buffer2 = new byte[1];
                    while ( port.read(buffer2, 1) > 0 ) {
                    	data = buffer2[0];
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
									getMachinePosition().x = Float.parseFloat(s[0]);
									getMachinePosition().y = Float.parseFloat(s[1]);
									getMachinePosition().z = Float.parseFloat(s[2]);
									String wpos = output.substring(output.indexOf("WPos:[") + 6);
									String[] s2 = wpos.split("[\\]\\[xyz,\\s]");
									getToolPosition().x = Float.parseFloat(s2[0]);
									getToolPosition().y = Float.parseFloat(s2[1]);
									getToolPosition().z = Float.parseFloat(s2[2]);
								} else if (output.startsWith("<")) {
									String mpos = output.substring(output.indexOf("MPos:") + 5);
									String[] s = mpos.split(",");
									getMachinePosition().x = Float.parseFloat(s[0]);
									getMachinePosition().y = Float.parseFloat(s[1]);
									getMachinePosition().z = Float.parseFloat(s[2]);
									String wpos = output.substring(output.indexOf("WPos:") + 5);
									String[] s2 = wpos.split("[,>]");
									getToolPosition().x = Float.parseFloat(s2[0]);
									getToolPosition().y = Float.parseFloat(s2[1]);
									getToolPosition().z = Float.parseFloat(s2[2]);
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
            catch (  Exception e )
            {
                e.printStackTrace();
                System.exit(-1);
            }        
        }
    }

    public class Updater implements Runnable 
    {
        public boolean exit = false;
        UsbSerialDriver port;
        public Updater ( UsbSerialDriver out )
        {
            this.port = out;
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
				e.printStackTrace();
		    	log("serial: updater exception 4545");
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
    			e.printStackTrace();
    			System.exit(9);
    		}
    		updater = null;
    		updater_thread = null;
    		System.out.println("Updater stopped.");
    	}
    	try {
			driver.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
    	driver = null;
    }
	public Vector3 getToolPosition() {
		return toolPosition;
	}
	public void setToolPosition(Vector3 toolPosition) {
		this.toolPosition = toolPosition;
	}
	public Vector3 getMachinePosition() {
		return machinePosition;
	}
	public void setMachinePosition(Vector3 machinePosition) {
		this.machinePosition = machinePosition;
	}

}
