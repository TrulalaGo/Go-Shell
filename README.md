# GoShell

This code is used to copy files and folders from DocumentFile (access storage with SAF) to File (local storage). Explanation: 

1. When a file is selected (pickedFile is not null) → The copyDocumentFile function is called to copy the file to the current directory (currentDir).
   
2. The copyDocumentTree function: If the object is a folder, create a new folder in the destination and then recursively copy its contents. If the object is a file, copy using copyDocumentFile.
      
3. The copyDocumentFile function: Opens InputStream from sourceFile.uri. Creates a new file in destinationDir. Copies data from input to output. Shows a Toast on success or failure.
   
4. After the copy is complete → refreshGrid updates the GridLayout view to reflect the changes. This code is suitable for SAF-based file manager applications, because it uses DocumentFile for secure file access on Android.
