##Download
http://spacewar-arena.com/cnc/files/grblgui.zip

## Screenshots
![screenshot](https://github.com/cody82/grblgui/raw/master/grblgui.png)
![screenshot](https://github.com/cody82/grblgui/raw/master/grblgui2.png)

## Requirements
* Java 1.6
* OpenGL 2.0

## Usage
Start the Program: `java -jar grblgui.jar [<your g-code file or directory> [<arduino COM-port>]]`

Example: `java -jar grblgui.jar /home/cody/gcode/ /dev/ttyACM0`

If you start the program without parameters, it will look for g-code files in `~/grblgui-gcode` 
and in `grblgui-gcode` next to `grblgui.jar`.
