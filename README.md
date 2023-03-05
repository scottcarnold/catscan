# CatScan

CatScan is a file search utility with several search types and capable of recursively searching within zip archives.

## Common Search Parameters

Directory: Enter or select the directory you want to search.

File Name(s):  Enter a comma separated list of file names to be included in the search.  File names can include asterisks as wildcards.  File names will be treated as case insensitive.

ZIP File Name(s):  Enter a comma separated list of ZIP archive file names to search within.  Files names can include asterisks as wildcards.  If you do not wish to search within ZIP archives, leave the field blank.

## File Name Search

This search type is for finding files by their file names.  

One present limitation of this search type is that you cannot find ZIP archives that match the ZIP file names of archives to also search within.  For example, if you wanted to search for any zip archives within any other zip archives, you might try to search for file names \*.zip with zip file names also set as \*.zip.  However, this will always return no results, as files matching the ZIP file names filter will not themselves be included in the file name search.

## Text File Search

This search type is for finding files that contain a given character sequence.

Search String: The character sequence to search for.  No wildcards are supported, and only one character sequence can be provided.

Case Sensitive:  If checked, search will be case sensitive.  Otherwise, search will be case insensitive.

## Java Class/Package Search

This search type is for finding Java classes or packages.  This search is a variation on the File Name Search.

Class or Package Name:  Enter a single class or package name to search for.  For example, you could enter "org.apache.logging.log4j" to search for that package.  This will be treated as case insensitive.

## File Statistics

This search type is for finding files or directories that match various extremes.

Stat Type:  Drop down selection for the type of extreme to look for, such as largest directories, or oldest files.

Max Results:  The maximum number of results to return.

## Search Results

Search results are provided within a tree structure.  If you double click a file or directory within the search results, an attempt will be made to open that file or directory.  