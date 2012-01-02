Jam Test Front End Readme File
==============================

Git
---
git clone git://jam-daq.git.sourceforge.net/gitroot/jam-daq/testfrontend (read-only)

git clone ssh://dwvisser@jam-daq.git.sourceforge.net/gitroot/jam-daq/testfrontend (read/write) 

Building
--------
If you have cloned the source using git, you will be able to simply run Ant
in the 'testfrontend' folder. To learn more about Ant and download the
installer, go to http://ant.apache.org/

Launching
---------
On windows systems, running launch.bat will launch the Jam Test Front End. If
you have built from source using the above instructions, you need to be in the 
target 'release' folder.

Using
-----
The GUI shows the parameters to be set in Jam to communicate with it. When Jam
successfully communicates with the test front end, the GUI shows appropriate
verbose messages, and sends appropriate replies, depending on state.