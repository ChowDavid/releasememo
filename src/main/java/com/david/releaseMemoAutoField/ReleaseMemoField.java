package com.david.releaseMemoAutoField;

public interface ReleaseMemoField {

	String DEVELOPER="DEVELOPER";
	String ISSUE_DATE="ISSUE_DATE";
	String PROJECT_NAME="PROJECT_NAME";
	String SOW_NUMBER="SOW_NUMBER";
	String ENHANCEMENT_NUMBER="ENHANCEMENT_NUMBER";
	String INC_NUMBER="INC_NUMBER";
	String WORKSPACE="WORKSPACE";
	String DEV_BRANCH="DEV_BRANCH";
	String HEAD_BRANCH="HEAD_BRANCH";
	String TARGET_MERGE_DATE="TARGET_MERGE_DATE";
	String SOURCE_FOLDER="SOURCE_FOLDER";
	String BINARY_FOLDER="BINARY_FOLDER";
	String RESOURCES_FOLDER="RESOURCES_FOLDER";
	String SOURCE_LIST="SOURCE_LIST";
	String RESOURCES_LIST="RESOURCES_LIST";
	String USERNAME="USERNAME";
	String PASSWORD="PASSWORD";
	String EOF="EOF";
	
	String[] KEYWORDS={
			"^DEVELOPER=.+$",
			"^ISSUE_DATE=\\d\\d\\d\\d-\\d\\d-\\d\\d$",
			"^PROJECT_NAME=.+$",
			"^SOW_NUMBER=.*$",
			"^ENHANCEMENT_NUMBER=.*$",
			"^INC_NUMBER=.*$",
			"^WORKSPACE=.+$",
			"^DEV_BRANCH=svn.+$",
			"^HEAD_BRANCH=svn.+$",
			"^TARGET_MERGE_DATE=\\d\\d\\d\\d-\\d\\d-\\d\\d$",
			"^SOURCE_FOLDER=.+$",
			"^BINARY_FOLDER=.+$",
			"^SOURCE_LIST$",
			"^RESOURCES_FOLDER=.*$",
			"^RESOURCES_LIST$",
			"^USERNAME=.+$",
			"^PASSWORD=.+$",
			"^EOF$"};

	
	
	
}
