# CatScan
CatScan is a simple file search utility.  It can search for:
+ files by name
+ text within files
+ java classes
+ statistics on files and directories

Each search type is also capable of searching within zip archives.

# CatScan 1.1 Release Notes
Planned Release 3/31/2023

## Build
+ Updated to use Maven build system
+ Updated Java minimum version from 1.5 to 1.8

## CatScan 1.1
+ Changed tab structure from tabs on left to tabs on top
+ Search criteria will now appear as the first node in the search results tree
+ Default action for double click on batch and shell script files in search results changed from Open to Edit
+ Added information button that launches About Dialog to display release notes
+ Added Longest Path Names as a new File Statistics type
+ Refactoring and removal of old code
+ Updated from Log4J to Log4J2
+ Updated to newer Zenput input validation framework
+ Fixed resource leak on zip file processing
+ Fixed bug related to search results tab selection

# CatScan 1.0 Release Notes
Released 6/24/2010

## CatScan 1.0
Initial released version.
