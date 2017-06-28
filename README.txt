Files project ----------------------

This app shows 2 different ways to get files to an Android app.
1. Push the file from a PC to the app. This approach makes use
   of nanohttpd. You must start the server before doing this.
   See the LuaScripts project for a python client to roll up
   scripts and send to the app.

        a. tap 'Start Server'
        b. from LuaScripts: python client.py <ip of device> 8080
        c. tap 'Stop Server'
        d. tap 'Refresh' to see the files downloaded.
        e. tap 'Execute' to run 'init.lua' (which requires other files)
        f. tap 'Light' to mimic controling a channel

2. Pull the file from a server. Enter the URL for the file and
   tap 'Get File'. See the LuaScripts project for a simple
   method to serve up files for this purpose.

        a. from LuaScripts, start the file server (http-server)
        b. enter a file name (teedious). This can be done to update a file.
        c. tap 'Refresh' to see the files
        d. tap 'Execute' to run 'init.lua'
        e. tap 'Light' to mimic controlling a channel


Notes a Luaj (from LuaAndroid) -----------------------------------------

Got ClassNotFoundException when trying to acess a non-system class.

One fix (that works) is to change the luaj source code.

Edit src/jse/org/luaj/vm2/lib/jse/LuajavaLib.java
At line 178 do:

    //return Class.forName(name, true, ClassLoader.getSystemClassLoader());
    return Class.forName(name, true, Thread.currentThread().getContextClassLoader());

In other words, replace the commented out line with the uncommented line.
At this point, I'm not sure about the side effects for doing this, but the script linked
and my test worked.

--------------------------------------------------------------

To build luaj:
    In the luaj directory type:

        ant

    The thing will build. Copy the jse jar file to the project.


---------------------------------------------------------------

clone nanohttpd

to build it: mvn package


