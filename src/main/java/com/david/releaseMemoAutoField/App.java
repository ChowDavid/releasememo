package com.david.releaseMemoAutoField;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.io.FileUtils;

/**
 * Hello world!
 *
 */
public class App
{
    public static void main( String[] args ) throws IOException, ArchiveException {
    	Map<String,Boolean> checkMap=new HashMap<String,Boolean>();
    	
    	for (String s:ReleaseMemoField.KEYWORDS){
    		checkMap.put(s, false);
    	}

        if (args!=null && args.length==1){
        	//Pass
        	System.out.println("Release Memo create process start" );
            System.out.println("File Check...");
            File file=new File(args[0]);
            if (!file.exists() || file.isDirectory()){
            	System.out.println("File provided does not exists "+file.getAbsolutePath());
            } else {
            	//validate the file input
            	for (String line:FileUtils.readLines(file)){
            		for (String key:ReleaseMemoField.KEYWORDS){
            			if (line.matches(key)){
            				checkMap.put(key, true);
            				break;
            			}
            		}
            	}
        		boolean fieldValidation=true;
        		for (String key:ReleaseMemoField.KEYWORDS){
        			if (!checkMap.get(key)){
        				fieldValidation=false;
        				System.out.println("Field Error found "+key);
        			}
        		}
        		if (fieldValidation){
        			System.out.println("No error found. process start...");
        			ReleaseMemo memo=new ReleaseMemo(file,Boolean.getBoolean("skipsvn"));
        			memo.create();
        			memo.compress();
        			memo.tearDown();
        			System.out.println("Process done!");
        		} else {
        			System.out.println("Process stop please correct the error on file ["+file.getName()+"]");
        		}
            }
        } else {
        	//ERROR missing content
        	System.out.println("Release Memo create process not start" );
            System.out.println("Please provide your release memo text file version 1.0");
            System.out.println("java -jar releaseMemo.jar -Dskipsvn memo.txt");
        }
    }
}
