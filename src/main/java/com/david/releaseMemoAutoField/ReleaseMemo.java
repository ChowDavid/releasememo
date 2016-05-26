package com.david.releaseMemoAutoField;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * @author david
 *
 */
public class ReleaseMemo {
	
	private final SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
	File file;
	File targetFile;
	File targetFolder;
	File zipFile;
	List<String> lines;
	List<String> errorMessage;
	SvnClient client;
	List<String> sources;
	boolean skipsvn;
	
	public ReleaseMemo(File file, boolean skipsvn) throws IOException{
		this.file=file;
		this.skipsvn=skipsvn;
		lines=FileUtils.readLines(file);
		errorMessage=new ArrayList<String>();
		targetFolder=new File(get(ReleaseMemoField.PROJECT_NAME).toUpperCase()+"_"+sdf.format(new Date()));
		zipFile=new File(get(ReleaseMemoField.PROJECT_NAME).toUpperCase()+"_"+sdf.format(new Date())+".zip");
		targetFile=new File(targetFolder.getAbsolutePath()+File.separatorChar+"release memo v2_"+get(ReleaseMemoField.PROJECT_NAME)+"_"+sdf.format(new Date())+".xls");
		String devScmUri=get(ReleaseMemoField.DEV_BRANCH);
		String username=get(ReleaseMemoField.USERNAME);
		String password=get(ReleaseMemoField.PASSWORD);
		if (!skipsvn) client=new SvnClient(devScmUri, username, password);
		sources=getSources();
	}
	
	public void tearDown() throws IOException{
		if (!skipsvn) client.cleanup();
	}

	private String get(String keyword) {
		for (String l:lines){
			for (String key:ReleaseMemoField.KEYWORDS){
				if (l.matches(key) && key.startsWith("^"+keyword)){
					return l.split("=")[1];
				}
			}
			
		}
		return null;
	}

	/**
	 * create the package from those source, and target
	 * It create three folder
	 * 	|_memo
	 * 		|_inputFile
	 * 		|_release memo v2.xls
	 * 		|_source
	 * 			|_/abc/def/xyz.java
	 * 			|_/foo/....java
	 * 		|_binary
	 * 			|_/abc/def/xyz.class
	 * 			|_/foo/....class
	 * 			|_someother.sql
	 * 			|_someother.properties
	 * @throws IOException 
	 */
	public void create() throws IOException {
		targetFolder.mkdirs();
		FileUtils.copyFileToDirectory(file, targetFolder);
		//Copy Source
		copySources(targetFolder.getAbsolutePath()+File.separatorChar+"source");
		copyBinaries(targetFolder.getAbsolutePath()+File.separatorChar+"binary");
		copyResources(targetFolder.getAbsolutePath()+File.separatorChar+"resources");
		createExcel(targetFile);
	}

	private void createExcel(File targetFile) throws IOException {
		Workbook wb =  new HSSFWorkbook();
		Sheet noteSheet = wb.createSheet("NOTE");
		int r=0;
		fillRow(noteSheet,r++,"DEVELOPER",get(ReleaseMemoField.DEVELOPER));
		fillRow(noteSheet,r++,"ISSUE_DATE",get(ReleaseMemoField.ISSUE_DATE));
		fillRow(noteSheet,r++,"PROJECT_NAME",get(ReleaseMemoField.PROJECT_NAME));
		fillRow(noteSheet,r++,"SOW_NUMBER",get(ReleaseMemoField.SOW_NUMBER));
		fillRow(noteSheet,r++,"ENHANCEMENT_NUMBER",get(ReleaseMemoField.ENHANCEMENT_NUMBER));
		fillRow(noteSheet,r++,"INC_NUMBER",get(ReleaseMemoField.INC_NUMBER));
		fillRow(noteSheet,r++,"WORKSPACE",get(ReleaseMemoField.WORKSPACE));
		fillRow(noteSheet,r++,"DEV_BRANCH",get(ReleaseMemoField.DEV_BRANCH));
		fillRow(noteSheet,r++,"HEAD_BRANCH",get(ReleaseMemoField.HEAD_BRANCH));
		fillRow(noteSheet,r++,"TARGET_MERGE_DATE",get(ReleaseMemoField.TARGET_MERGE_DATE));
		r++;
		r++;
		for (String message:errorMessage){
			fillRow(noteSheet,r++,"ERROR Message",message);
		}
		

		Sheet sourceSheet = wb.createSheet("FILE");
		r=0;
		fillRow(sourceSheet,r++,"Developer","File","Date (YYYYMMDD)","Add\\Delete\\Update","Change Description","QC ID(if any)");
		for (String line:sources){
			fillRow(sourceSheet,r++,"",line);
		}
		for (String line:getResources()){
			fillRow(sourceSheet,r++,"",line);
		}
        
        FileOutputStream out = new FileOutputStream(targetFile.getAbsolutePath());
        wb.write(out);
        out.close();
		
	}

	private void fillRow(Sheet noteSheet, int i, String...strings ) {
		Row r = noteSheet.createRow(i);
		int c=0;
		for (String value:strings){
			r.createCell(c++).setCellValue(value);
		}
		
	}

	private void copyResources(String targetFolderName) throws IOException {
		String workspace=get(ReleaseMemoField.WORKSPACE);
		String resourceFolder=get(ReleaseMemoField.RESOURCES_FOLDER);
		for (String line:getResources()){
			File sourceFile=new File(workspace+File.separatorChar+resourceFolder+File.separatorChar+line);
			FileUtils.copyFile(sourceFile, new File(targetFolderName+File.separatorChar+line));
		}
	}

	private void copyBinaries(String targetFolderName) throws IOException {
		String workspace=get(ReleaseMemoField.WORKSPACE);
		String sourceFolder=get(ReleaseMemoField.BINARY_FOLDER);
		for (String line:sources){
			if (line.endsWith("java")){
				line=line.substring(0,line.length()-4)+"class";
				File sourceFile=new File(workspace+File.separatorChar+sourceFolder+File.separatorChar+line);
				if (sourceFile.exists()){
					FileUtils.copyFile(sourceFile, new File(targetFolderName+File.separatorChar+line));			
				} else {
					System.err.println("Error found binary file does not exists ["+sourceFile.getAbsolutePath()+"]");
					errorMessage.add("Error found binary file does not exists ["+sourceFile.getAbsolutePath()+"]");
				}
			} 	
		}
	}

	private void copySources(String targetFolderName) throws IOException {
		String workspace=get(ReleaseMemoField.WORKSPACE);
		String sourceFolder=get(ReleaseMemoField.SOURCE_FOLDER);
		for (String line:sources){
			File sourceFile=new File(workspace+File.separatorChar+sourceFolder+File.separatorChar+line);
			FileUtils.copyFile(sourceFile, new File(targetFolderName+File.separatorChar+line));
		}
	}
	
	
	private List<String> getResources(){
		List<String> result=new ArrayList<String>();
		String workspace=get(ReleaseMemoField.WORKSPACE);
		String resourceFolder=get(ReleaseMemoField.RESOURCES_FOLDER);
		boolean start=false;
		for (String line:lines){
			for (String key:ReleaseMemoField.KEYWORDS){
				if (line.matches(key)){
					start=false;
				}
			}
			if (line.matches("^"+ReleaseMemoField.RESOURCES_LIST+"$")){
				start=true;
			}
			if (start){
				boolean keywordFound=false;
				for (String key:ReleaseMemoField.KEYWORDS){
					if (line.matches(key)){
						keywordFound=true;
						break;
					}
				}
				if (!keywordFound){
					//System.out.println(line);
					File sourceFile=new File(workspace+File.separatorChar+resourceFolder+File.separatorChar+line);
					if (sourceFile.exists()){
						if (!skipsvn){
							if (client.validate(workspace, resourceFolder, line)){
								result.add(line);
							} else {
								System.err.println("Error found resources svn not match ["+sourceFile.getAbsolutePath()+"]");
								errorMessage.add("Error found resources svn not match ["+sourceFile.getAbsolutePath()+"]");
							}
						} else {
							result.add(line);
						}
					} else {
						System.err.println("Error found resoruces file does not exists ["+sourceFile.getAbsolutePath()+"]");
						errorMessage.add("Error found resoruces file does not exists ["+sourceFile.getAbsolutePath()+"]");
					}
				}
					
			}
		}
		return result;
	}
	
	private List<String> getSources(){
		List<String> result=new ArrayList<String>();
		String workspace=get(ReleaseMemoField.WORKSPACE);
		String sourceFolder=get(ReleaseMemoField.SOURCE_FOLDER);
		boolean start=false;
		for (String line:lines){
			for (String key:ReleaseMemoField.KEYWORDS){
				if (line.matches(key)){
					start=false;
				}
			}
			if (line.matches("^"+ReleaseMemoField.SOURCE_LIST+"$")){
				start=true;
			}
			if (start){
				boolean keywordFound=false;
				for (String key:ReleaseMemoField.KEYWORDS){
					if (line.matches(key)){
						keywordFound=true;
						break;
					}
				}
				if (!keywordFound){
					File sourceFile=new File(workspace+File.separatorChar+sourceFolder+File.separatorChar+line);
					if (sourceFile.exists()){
						if (!skipsvn){
							if (client.validate(workspace, sourceFolder, line)){
								result.add(line);
							} else {
								System.err.println("Error found source svn not match ["+sourceFile.getAbsolutePath()+"]");
								errorMessage.add("Error found source svn not match ["+sourceFile.getAbsolutePath()+"]");
							}
						} else {
							result.add(line);
						}
					} else {
						System.err.println("Error found source file does not exists ["+sourceFile.getAbsolutePath()+"]");
						errorMessage.add("Error found source file does not exists ["+sourceFile.getAbsolutePath()+"]");
					}
				}
					
			}
		}
		
		return result;
	}
	
    /**
     * Add all files from the source directory to the destination zip file
     *
     * @param source      the directory with files to add
     * @param destination the zip file that should contain the files
     * @throws IOException      if the io fails
     * @throws ArchiveException if creating or adding to the archive fails
     */
    private void addFilesToZip(File source, File destination) throws IOException, ArchiveException {
        OutputStream archiveStream = new FileOutputStream(destination);
        ArchiveOutputStream archive = new ArchiveStreamFactory().createArchiveOutputStream(ArchiveStreamFactory.ZIP, archiveStream);

        Collection<File> fileList = FileUtils.listFiles(source, null, true);

        for (File file : fileList) {
            String entryName = getEntryName(source, file);
            ZipArchiveEntry entry = new ZipArchiveEntry(entryName);
            archive.putArchiveEntry(entry);

            BufferedInputStream input = new BufferedInputStream(new FileInputStream(file));

            IOUtils.copy(input, archive);
            input.close();
            archive.closeArchiveEntry();
        }

        archive.finish();
        archiveStream.close();
    }

    /**
     * Remove the leading part of each entry that contains the source directory name
     *
     * @param source the directory where the file entry is found
     * @param file   the file that is about to be added
     * @return the name of an archive entry
     * @throws IOException if the io fails
     */
    private String getEntryName(File source, File file) throws IOException {
        int index = source.getAbsolutePath().length() + 1;
        String path = file.getCanonicalPath();

        return path.substring(index);
    }

	public void compress() throws IOException, ArchiveException {
	     addFilesToZip(targetFolder, zipFile);
		
	}
	
	
	
}
