package com.david.releaseMemoAutoField;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.model.WorkbookRecordList;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationProvider;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc2.SvnCheckout;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnTarget;

public class SvnClient {
	
	private  final String TEMP_FOLDER=String.valueOf(System.currentTimeMillis());
	private  File  tempFile;
	
	public SvnClient(String uri,String username,String password){
		tempFile=new File(TEMP_FOLDER);
		if (tempFile.exists()){
			tempFile.delete();
		}
		final SvnOperationFactory svnOperationFactory = new SvnOperationFactory();
		ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(username, password);
		svnOperationFactory.setAuthenticationManager(authManager);
		try {
		    final SvnCheckout checkout = svnOperationFactory.createCheckout();
		    checkout.setSingleTarget(SvnTarget.fromFile(tempFile));
		    checkout.setSource(SvnTarget.fromURL(SVNURL.parseURIDecoded(uri)));
		    checkout.run();
		} catch (SVNException e){
			e.printStackTrace();
			tempFile=null;
		} finally {
		    svnOperationFactory.dispose();   
		}
	}
	public  boolean validate(String workspace, String workspaceSourceFolder, String workspaceFileName) {
		File workspaceFile=new File(workspace+File.separatorChar+workspaceSourceFolder+File.separatorChar+workspaceFileName);
		File scmTempFile=new File(TEMP_FOLDER+File.separatorChar+workspaceSourceFolder+File.separatorChar+workspaceFileName);
		if (!scmTempFile.exists()){
			System.err.println("Error scm file not exists "+scmTempFile.getAbsolutePath());
			return false;
		}
		if (!workspaceFile.exists()){
			System.err.println("Error workspace file not exists "+workspaceFile.getAbsolutePath());
			return false;
		}
		try {
			return FileUtils.contentEqualsIgnoreEOL(workspaceFile, scmTempFile, null);
		} catch (IOException e){
			e.printStackTrace();
			return false;
		}
	}
	
	public void cleanup() throws IOException{
		if (tempFile.exists()) FileUtils.deleteDirectory(tempFile);
	}
	
	/**
	 * The following code for testing only
	 * @param args
	 * @throws SVNException
	 */
	public static void main(String[] args) throws SVNException{
		String destPath="/Users/david/Documents/workspaces/workspace_source_health_check/releaseMemoAutoField/repos";
		String username="spiedwc";
		String password="spiedwc123";
		String url="svn://clklxcvsp01/mlc/head/Addon";
		//long revision = 154;
		//boolean isRecursive = true;

		final SvnOperationFactory svnOperationFactory = new SvnOperationFactory();
		ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(username, password);
		svnOperationFactory.setAuthenticationManager(authManager);
		try {
		    final SvnCheckout checkout = svnOperationFactory.createCheckout();
		    checkout.setSingleTarget(SvnTarget.fromFile(new File(destPath)));
		    checkout.setSource(SvnTarget.fromURL(SVNURL.parseURIDecoded(url)));
		    checkout.run();
		} finally {
		    svnOperationFactory.dispose();
		}
		
		
	}

}
