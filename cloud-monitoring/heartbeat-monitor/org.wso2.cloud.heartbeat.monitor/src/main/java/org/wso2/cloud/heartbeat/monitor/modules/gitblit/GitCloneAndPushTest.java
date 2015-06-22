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
import org.eclipse.jgit.api.errors.*;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.PersistJobDataAfterExecution;
import org.wso2.cloud.heartbeat.monitor.utils.Constants;
import org.wso2.cloud.heartbeat.monitor.utils.fileutils.FileManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class implements Git clone and push test scenario for a Cloud setup.
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class GitCloneAndPushTest extends AbstractGitTest {

	private static final Log log = LogFactory.getLog(GitCloneAndPushTest.class);

	/*
	 * Function to be tested will be implemented in this method
	 * 
	 * @throws org.quartz.JobExecutionException
	 */
	@Override
	public void execute(JobExecutionContext jobExecutionContext)
			throws JobExecutionException {
		try {
			setTestName("Git clone and push test");
			init();
			createRepository();

			gitClone();
			updateAFile();
			gitAdd();
			gitCommit();

			gitPush();
			testStateHandler.onSuccess(getTestInfo());
		} catch (TransportException e) {
			handleError("TransportException", e);

		} catch (InvalidRemoteException e) {
			handleError("InvalidRemoteException", e);

		} catch (GitAPIException e) {
			handleError("GitAPIException", e);

		} catch (IOException e) {
			handleError("IOException", e);

		} catch (Exception e) {
			handleError("Exception", e);
		}
		try {
			deleteRepository();
		} catch (Exception e) {
			handleError("Exception", e);

		}
		cleanDirectory(localPath);
	}

	/*
	 * This method will create an empty repository :applicationKey at
	 * :remotePath
	 * 
	 * @throws java.io.IOException
	 */
	public void createRepository() throws IOException {

		String repoName = applicationKey + Constants.REPO_TYPE;
		String repoCreateUrl = remotePath + Constants.REPO_CREATE_PARAM
				+ applicationKey;
		String adminUsername = gitUser;
		String adminPassword = gitPassword;
		RepositoryModel model = new RepositoryModel();
		model.name = repoName;
		model.accessRestriction = com.gitblit.Constants.AccessRestrictionType.VIEW;
		model.isBare = true;
		try {
			boolean isCreated = RpcUtils.createRepository(model, repoCreateUrl,
					adminUsername, adminPassword.toCharArray());
			if (isCreated) {
				log.info("Git repository \'" + applicationKey
						+ "\' is created @ " + remotePath);
			} else if (repoExists(applicationKey)) {
				log.error(applicationKey + " : Repository already exists @ "
						+ remotePath);
			} else {
				String msg = "Repository is not created for " + applicationKey
						+ " @ " + remotePath + " due to remote server error";
				log.error(msg);
				throw new IOException(msg);
			}
		} catch (IOException e) {
			String msg = "Repository is not created \'" + applicationKey
					+ "\' @ " + remotePath + " due to "
					+ e.getLocalizedMessage();
			log.error(msg, e);
			throw new IOException(msg, e);
		}

	}

	/*
	 * This method will delete the repository :applicationKey at :remotePath
	 * 
	 * @throws java.lang.Exception
	 */

	public void deleteRepository() throws Exception {

		String repoName = applicationKey + Constants.REPO_TYPE;
		String repoCreateUrl = remotePath + Constants.REPO_DELETE_PARAM
				+ applicationKey;
		String adminUsername = gitUser;
		String adminPassword = gitPassword;
		// Create the gitblit repository model
		RepositoryModel model = new RepositoryModel();
		model.name = repoName;
		// authenticated users can clone, push and view the repository
		model.accessRestriction = com.gitblit.Constants.AccessRestrictionType.VIEW;
		model.isBare = true;
		try {
			boolean isDeleted = RpcUtils.deleteRepository(model, repoCreateUrl,
					adminUsername, adminPassword.toCharArray());
			if (isDeleted) {
				log.info("Git repository is deleted for \'" + applicationKey
						+ "\' @ " + remotePath);
			} else {
				String msg = "Repository is not deleted \'" + applicationKey
						+ "\' @ " + remotePath + " due to remote server error";
				log.error(msg);
				throw new Exception(msg);
			}
		} catch (IOException e) {
			String msg = "Repository is not deleted \'" + applicationKey
					+ "\' @ " + remotePath + " due to "
					+ e.getLocalizedMessage();
			log.error(msg, e);
			throw new IOException(msg, e);
		}
	}

	/*
	 * Add new file to the repo
	 * 
	 * @throws java.io.IOException
	 */
	private void updateAFile() throws IOException {

		String filePath = localPath + File.separator
				+ Constants.SAMPLE_APP_FILE_PATH;
		File testFile = new File(filePath);
		if (!testFile.exists()) {
			testFile.createNewFile();
		}
		if (log.isDebugEnabled()) {
			log.debug("Creating new  file : " + filePath);
		}

		SimpleDateFormat dateTimeFormat = new SimpleDateFormat(
				Constants.SIMPLE_DATE_TIME_FORMAT);
		String dataToWrite = "/* " + dateTimeFormat.format(new Date())
				+ " test comment */ \n";
		FileManager.writeToFile(filePath, dataToWrite);
	}

}
