# releasememo
## build 
mvn clean install
## JDK requirement
JDK 1.6 or above
## operation
java -jar releaseMemoAutoFieldXXX.jar <path to source file> [-Dskipsvn=true]
if svn does not exists. please set skipsvn=true otherwise it will check the svn content form the workspace
## required field from source file
```
DEVELOPER=
ISSUE_DATE=YYYY-MM-DD
PROJECT_NAME=
SOW_NUMBER=
ENHANCEMENT_NUMBER=
INC_NUMBER=
WORKSPACE=<abs path>
DEV_BRANCH=<must be svn>
HEAD_BRANCH=<must be svn>
USERNAME=<svn username>
PASSWORD=<svn password>
TARGET_MERGE_DATE=YYYY-MM-DD
SOURCE_FOLDER=src/main/java
BINARY_FOLDER=target/classes
RESOURCES_FOLDER=src/main/resources
SOURCE_LIST
com/xxx/yyy/abc.java
...
...
...
RESOURCES_LIST
com/xxx/yyy/hello.properties
...
...
...
EOF
```


