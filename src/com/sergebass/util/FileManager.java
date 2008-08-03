/*
 * FileManager.java (C) Serge Perinsky
 */

package com.sergebass.util;

/**
 * FileManager.
 *
 * @author Serge Perinsky
 */
public class FileManager {

/*
 JSR 75: Accessing the PIM database and File system
Part one: The FileConnection API
	

[Back]

PDA optional packages (JSR 75) gives developers access to two major areas that were previously unavailable. These are the PIM API which gives access to the Personal Information Management (PIM) database including to-do lists, calendars and contact data. The second optional package is the FileConnection API that gives access to the mobile phone's file system including removable storage media like Memory Sticks.

Currently Java™ Platforms 5 and 6 (JP-5 and JP-6) support JSR 75. Sony Ericsson mobile phones that support JP-5 include the K600, V600, Z520 and W800 and the W600 and W500 Walkman™ phones support JP-6.

This article is divided into two sections: the first part (this article) focuses on the FileConnection API and includes an example to download, and the second part (to follow) describes the PIM API.

Download FileConnection example>>

Dealing with restricted APIs
Both these packages are restricted APIs and will prompt you for permission when used. It can be a bit annoying to be prompted every time the MIDlet accesses the file system or the PIM database. However if a MIDlet is signed, you have an extra option when prompted that allows the action as long as the MIDlet is installed on the system. This is the so-called blanket option and can be set in the MIDlets permission settings.

When distributing software that uses these kind of restricted APIs and the MIDlet is signed, it is crucial for usability to instruct the user how to set the blanket option so there is no need to click through the permission dialogs every time the MIDlet is accessing a restricted API. This can also be set in the permissions menu.

Navigate to the permissions menu from marking your MIDlet and choose the "more" option (left soft button). From the more menu, choose "permissions", from that menu different permissions can be configured and if your MIDlet is signed you can set the blanket option for the desired functionality.
 
The FileConnection API

Permissions

Two permissions are defined for the Fileconnection API:

    javax.microedition.io.Connector.file.read
    javax.microedition.io.Connector.file.write

The read permission is used when a file is open in read mode or if an InputStream is opened from a FileConnection object.

The write permission is used to open files in write mode or if an OutputStream is opened from a FileConnection. It is also used when invoking other write operations like delete or rename.

If you are not granted the permission to access the restricted API, a SecurityException is thrown and it is important to manage these situations in your MIDlet.

Like all kinds of I/O operations, it is highly recommended that you execute your file system I/O operations in a thread of their own to prevent any deadlocks.

Sony Ericsson specifics:
Sony Ericsson's implementation of JSR 75 has some restrictions including:

    * The folders Games and Themes can not be accessed
    * Files and Directories are case sensitive
    * The length of the file path is limited by the native file system which is 120 characters

You can read about all the specifics in the Sony Ericsson Java ME Platform Developers' Guidelines, page 42>> 

System properties:
To check if the the FileConnection API is implemented use the:
System.getProperty("microedition.io.file.FileConnection.version");
This should return "1.0" if it is implemented.

Classes and interfaces in the FileConnection API
The FileConnection API gives you the opportunity to access the device file system including Memory Sticks. It gives the functionality to create, remove directories and files, list directory content, set permissions, get file information and perform I/O operations on files. Below are the important classes and interfaces:

    * ConnectionClosedException is thrown when a method is invoked on a FileConnection while the connection is closed.
    * FileSystemRegistry class is a central registry and has the ability to list the mounted roots with the method listRoots(). It also provides methods for registering listeners that are notified if file systems are added and removed during run time.
    * The FileSystemListener interface is used for receiving status notification when adding or removing a file system root.
    * The FileConnection interface is used to access directories and files on the device.
    * It extends the Connection interface and holds a number of useful methods to create, remove directories and files, list directory content, set permissions, get file information and perform I/O operations on files.

Using the FileConnection API
Since the FileConnection API uses the Generic Connection Framework for file system connectivity, the procedure to create a FileConnection is performed in the same way as create any GCF connection, with the only difference being the URL.

To create a FileConnection, use the Connector factory's open method which returns a Connection:

    Connector.open(string URL);

Valid URLs:

    For internal memory "file://localhost/c:/" or "file:///c:/".
    For memory card "file://localhost/c:/" or "file:///e:/

So when accessing the other directory in the internal memory, create a FileConnection object using the URL below:
FileConnection fc = (FileConnection)Connector.open("file:///c:/");

Since the mode parameter in Connector.open() method is not supported, both the read and write permissions are set when creating a FileConnection object.

When creating a FileConnection, the actual file or directory that the URL is pointing to doesn't have to exist, so opening a file/directory and creating a new file/directory is very similar. This is necessary when creating files and directories.

Remember to close the FileConnection object when you are done using it:
fc.close();

A FileConnection that is pointing to a non-existent file will not be able to perform any file or directory specific operations. For example, if you are trying to open an InputStream or an OutPutStream, a java.io.IOException will be thrown.

The only difference is when the FileConnection is open the method create() or mkdir() is invoked to create the file or directory on the file system.

You can always check if the file/directory exists:
fc.exists();

If it doesn't exist, create a new file:
fc.create();

or create a directory:
fc.mkdir();

To delete a file or a directory:
fc.delete();

To list the content of a directory that your FileConnection is pointing to:
Enumeration e = fc.list();
while (e.hasMoreElements()) {
 System.out.println(((String)e.nextElement()));
}

This method returns an enumeration of all directories and files that are present in the directory. Directories are denoted with a trailing slash "/" in their returned name. There is a second version of the list method that enables you to sort the content with a filter:
list(String filter, boolean includeHidden)

This methods return the content that is passing the filter, the filter could be "*.mp3" for only listing files that ends with .mp3. Use the attribute includeHidden to indicate whether files marked as hidden should be included or not in the list of files and directories returned. These methods return folders and files in a random order so you need to sort them manually.

The FileSystemRegistry has the static method listRoots() to list the available and supported mounted roots on the device. The list is returned as an Enumeration :
Enumeration e =  FileSystemRegistry.listRoots();
while (e.hasMoreElements()) {
            String rootName = (String)e.nextElement();
 System.out.println("mounted root:"+rootName);
 }  

File I/O
If you are used to the GCF connection framework, you will be very familiar with reading from files and writing to files. To write to a file you need to get an OutputStream from the FileConnection object that points to the existing file:
OutputStream os = fc.openOutputStream()
os.write(new String("hello").getBytes());
os.close();

Or you can use the DataOutputStream to write primitive Java datatypes to a file:
int i = 1234;
DataOutputStream ds = fc. openDataOutputStream();
ds.writeInt(i);
ds.close();

To read from a file you get the InputStream or a DataInputStream from a FileConnection object that is pointing to the existing file:
byte[] b = new byte[1024];
InputStream is = fc.openInputStream();
is.read(b);
is.close();

File and directory information
There are a number of methods in the FileConnection class that are used for retrieving information about the specific directory or file. Here are some of them:

boolean canRead() - Is the file/directory readable?

boolean canWrite()  - Is the file/directory writable?

long directorySize(boolean includeSubDirs)  - Returns the size of all the files in bytes in the directory. If the includeSubDirs is true the size of all the sub-directories are included.

long fileSize()  -  Returns the size of the file in bytes

long lastModified()  - Returns the date when the file/directory was last modified

Play media files instantly using progressive download on the W600
The implementation of the FileConnection API on the Sony Ericsson W600 mobile phone uses progressive download to give instant access to media files to the MMAPI.

Progressive download is a technique used to play media while the media is still being downloaded to the player (you may have noticed this when looking at video clips on the internet). The video is being played while the player is still downloading and buffering the content. Before the implementation of the progressive download, the entire media file was loaded into the memory before it was played, resulting in long waiting times from the media being loaded to it actually was played (more obvious when the media file was large).

The benefit of this is obvious when playing large media files and especially when using play list functionality.

To use progressive download you need to create a player using the file scheme like this:
Manager.createPlayer("file:///c:/");

 

More information:

    * Download the JSR 75 API specification at www.jcp.org
    * Sony Ericsson specific information about JSR 75 in the Sony Ericsson Java ME Platform Developers' Guidelines>>
    * Useful article at Javasoft>> 
 */
}
