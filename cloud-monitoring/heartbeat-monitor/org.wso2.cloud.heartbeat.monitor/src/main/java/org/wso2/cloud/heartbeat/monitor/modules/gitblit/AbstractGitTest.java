/**
 *  Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.cloud.heartbeat.monitor.modules.gitblit;

import com.gitblit.models.RepositoryModel;
import com.gitblit.utils.RpcUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.*;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.wso2.cloud.heartbeat.monitor.core.notification.Mailer;
import org.wso2.cloud.heartbeat.monitor.core.notification.SMSSender;
import org.wso2.cloud.heartbeat.monitor.utils.*;
import org.wso2.cloud.heartbeat.monitor.utils.fileutils.CaseConverter;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.Map;

/*
 * Abstract class for Git tests
 * */
public abstract class AbstractGitTest implements Job {

	private static final Log log = LogFactory.getLog(AbstractGitTest.class);

	private String testName = "Default Test Name";
	protected String localPath;
	protected String remotePath;
	protected String applicationKey;
	protected Repository localRepo;
	protected Git git;
	protected String hostName;
	protected String serviceName;
	protected String gitUser;
	protected String gitPassword;
	protected TestStateHandler testStateHandler;
	private TestInfo testInfo;

	/*
	 * This method initializes TestStateHandler instance
	 * 
	 * @throws java.io.IOException
	 */
	public void init() throws IOException {
		PlatformUtils.setTrustStoreParams();
		testStateHandler = TestStateHandler.getInstance();
		testInfo = new TestInfo(serviceName, testName, remotePath);
	}

	/*
	 * this method initializes a git repository
	 * 
	 * @throws java.io.IOException
	 */
	protected void initRepo() throws IOException {
		if (git != null) {
			return;
		}
        if(log.isDebugEnabled()){
            log.debug("initializing local repo at "+ localPath);
        }
		try {
			localRepo = new FileRepository(localPath + File.separator + ".git");
		} catch (IOException e) {
			String msg = "IOException occurred while creating local repository "
					+ localPath;
			log.error(msg);
			throw new IOException(msg);
		}
		git = new Git(localRepo);
	}

	/*
	 * Clone a git repository
	 * 
	 * @throws org.eclipse.jgit.errors.GitAPIException
	 */
	protected void gitClone() throws  Exception {
		if (log.isDebugEnabled()) {
			log.debug("Executing Git Clone " + remotePath + " to " + localRepo);
		}
        String repo = remotePath + File.separator + Constants.GIT + File.separator
                + applicationKey + Constants.REPO_TYPE;
		try {
			UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(
					gitUser, gitPassword);
			Git.cloneRepository().setCredentialsProvider(credentialsProvider)
					.setURI(repo).setDirectory(new File(localPath)).call();
			log.info("Executing Git Clone done " + remotePath + " to "
					+ localPath);

		} catch (GitAPIException e) {
			throw new GitAPIException(
					"GitAPIException while executing git clone from repository : " + repo + " to : "+ localPath, e) { // for
																		// repo
			};
		} catch (JGitInternalException e) {
			log.error(
                    "org.eclipse.jgit.api.errors.JGitInternalException: Unable to create directories for "
                            + localPath,e);
            throw new Exception("org.eclipse.jgit.api.errors.JGitInternalException: Unable to create directories for "
                    + localPath,e);
		}
	}

	/*
	 * Add file to the git repo
	 * 
	 * @throws org.eclipse.jgit.errors.GitAPIException
	 */
	protected void gitAdd() throws GitAPIException, IOException {
		initRepo();
		if (log.isDebugEnabled()) {
			log.debug("Executing Git Add " + remotePath + " to " + localRepo);
		}
		try {
			git.add().addFilepattern(".").call();
		} catch (GitAPIException e) {
			throw new GitAPIException(
					"GitAPIException while executing git add", e) {
			};
		}
	}

	/*
	 * Commit changes made in local repository to remote repo
	 * 
	 * @throws org.eclipse.jgit.errors.GitAPIException
	 */
	protected void gitCommit() throws GitAPIException, IOException {
		initRepo();
		if (log.isDebugEnabled()) {
			log.debug("Executing Git Commit " + localRepo);
		}
		try {
			git.commit().setMessage("test commit").call();
		} catch (GitAPIException e) {
			throw new GitAPIException(
					"GitAPIException while executing git commit", e) {
			};
		}
	}

	/*
	 * Push changes to remote repo
	 * 
	 * @throws org.eclipse.jgit.errors.GitAPIException
	 */
	protected void gitPush() throws GitAPIException, IOException {
		initRepo();
		if (log.isDebugEnabled()) {
			log.debug("Executing Git Push " + localRepo);
		}

		UsernamePasswordCredentialsProvider credentialsProvider = new UsernamePasswordCredentialsProvider(
				gitUser, gitPassword);
		try {
			git.push().setCredentialsProvider(credentialsProvider).call();
		} catch (GitAPIException e) {
			throw new GitAPIException(
					"GitAPIException while executing git push", e) {
			};
		}
	}

	protected boolean repoExists(String applicationKey) throws IOException {
		RepositoryModel retrievedRepo = null;
		String repoName = applicationKey + Constants.REPO_TYPE;
		String repoUrl = remotePath ;
		String adminUsername = gitUser;
		String adminPassword = gitPassword;
		// Create the gitblit repository model
		RepositoryModel model = new RepositoryModel();
		model.name = repoName;
		// authenticated users can clone, push and view the repository
		model.accessRestriction = com.gitblit.Constants.AccessRestrictionType.VIEW;
		model.isBare = true; // TODO: temporaryly added for demo purpose, need
		// to fixed with new gitblit
		try {
			retrievedRepo = findRepository(model.name, repoUrl, adminUsername,
					adminPassword);
		} catch (IOException e) {
			String msg = "Repository is not deleted : " + repoName
					+ " due to " + e.getLocalizedMessage();
			log.error(msg, e);
			throw new IOException(msg, e);
		}
		return retrievedRepo != null;
	}

	private RepositoryModel findRepository(String name, String url,
			String account, String password) throws IOException {
		Map<String, RepositoryModel> repositories = RpcUtils.getRepositories(
				url, account, password.toCharArray());
		RepositoryModel retrievedRepository = null;
		for (RepositoryModel model : repositories.values()) {
			if (model.name.equalsIgnoreCase(name)) {
				retrievedRepository = model;
				break;
			}
		}
		return retrievedRepository;
	}

	/*
	 * Function to be tested will be implemented in this method
	 * 
	 * @throws org.quartz.JobExecutionException
	 */
	@Override
	public void execute(JobExecutionContext jobExecutionContext)
			throws JobExecutionException {

	}

	/**
	 * Sets service host
	 * 
	 * @param hostName
	 *            Service host
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public void setTestName(String testName) {
		this.testName = testName;
	}

	/**
	 * Sets Service name
	 * 
	 * @param serviceName
	 *            Service name
	 */
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	/**
	 * On test failure, alerts via email and sms
	 * 
	 * @param msg
	 *            error message
	 */
	protected void onFailure(String msg) {

		boolean isSuccess = false;
		DbConnectionManager dbConnectionManager = DbConnectionManager
				.getInstance();
		Connection connection = dbConnectionManager.getConnection();

		long timestamp = System.currentTimeMillis();
		DbConnectionManager.insertLiveStatus(connection, timestamp,
				serviceName, testName, isSuccess);
		DbConnectionManager.insertFailureDetail(connection, timestamp,
				serviceName, testName, msg);

		Mailer mailer = Mailer.getInstance();
		mailer.send(CaseConverter.splitCamelCase(serviceName) + " :FAILURE",
				CaseConverter.splitCamelCase(testName) + ": " + msg, "");
		SMSSender smsSender = SMSSender.getInstance();
		smsSender.send(CaseConverter.splitCamelCase(serviceName) + ": "
				+ CaseConverter.splitCamelCase(testName) + ": Failure");
	}

	/*
	 * Clean the temp directory
	 * 
	 * @param path : path to file
	 */
	protected void cleanDirectory(String path) {
		File directory = new File(path);
		if (directory.exists()) {
			try {
				for (File file : directory.listFiles()) {
					if (file.isDirectory()) {
						cleanDirectory(file.getPath());
					}
					file.delete();
				}
			} catch (Exception e) {
				log.error(
						"Exception occurred while deleting git temp directory ",
						e);
			}
			directory.delete();
		}

        if(log.isDebugEnabled()){
            log.debug("Temp local repository is deleted!");
        }
	}

	/*
	 * Handles exceptions as occurred
	 * 
	 * @param type : type of the error
	 * 
	 * @param obj : exception
	 */

	protected void handleError(String type, Object obj) {

		if (type.equals("GitAPIException")) {
			GitAPIException gitAPIException = (GitAPIException) obj;
			log.error(CaseConverter.splitCamelCase(serviceName) + " - "
					+ hostName, gitAPIException);
			testStateHandler.onFailure(testInfo, gitAPIException.getMessage(),
					gitAPIException);
		} else if (type.equals("IOException")) {
			IOException ioException = (IOException) obj;
			log.error(CaseConverter.splitCamelCase(serviceName) + hostName,
					ioException);
			testStateHandler.onFailure(testInfo, ioException.getMessage(),
					ioException);
		} else if (type.equals("TransportException")) {
			TransportException tException = (TransportException) obj;
			log.error(CaseConverter.splitCamelCase(serviceName) + hostName,
					tException);
			testStateHandler.onFailure(testInfo, tException.getMessage(),
					tException);
		} else if (type.equals("Exception")) {
			Exception exception = (Exception) obj;
			log.error(CaseConverter.splitCamelCase(serviceName) + hostName,
					exception);
			testStateHandler.onFailure(testInfo, exception.getMessage(),
					exception);
		}
	}

	public TestInfo getTestInfo() {
		return testInfo;
	}

	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}

	public void setRemotePath(String remotePath) {
		this.remotePath = remotePath;
	}

	public void setLocalRepo(Repository localRepo) {
		this.localRepo = localRepo;
	}

	public void setGitUser(String gitUser) {
		this.gitUser = gitUser;
	}

	public void setGitPassword(String gitPassword) {
		this.gitPassword = gitPassword;
	}

	public void setApplicationKey(String applicationKey) {
		this.applicationKey = applicationKey;
	}

}
