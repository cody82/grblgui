##Download
https://github.com/cody82/grblgui/releases/download/v0.2.2/grblgui.zip

## Screenshots
![screenshot](https://github.com/cody82/grblgui/raw/master/grblgui.jpg)

## Requirements
* Java 1.7
* OpenGL 2.0

## Usage
Start the Program: `java -jar grblgui.jar [<your g-code file or directory> [<arduino COM-port>]]`

Example: `java -jar grblgui.jar /home/cody/gcode/ /dev/ttyACM0`

If you start the program without parameters, it will look for g-code files in `~/grblgui-gcode` 
and in `grblgui-gcode` next to `grblgui.jar`.

## Development
For compiling you need:
* Eclipse 3.6(?)
* Eclipse Scala plugin with Scala 2.10
