/*
 * Copyright 2005-2013 WSO2, Inc. http://www.wso2.org
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.cloud.heartbeat.monitor.utils.fileutils;

import java.io.*;

/**
 * File management utilities implemented in this class
 */
public class FileManager {

	/**
	 * Reads a file a returns string content
	 * 
	 * @param filePath
	 *            Relative file path
	 * @return String content of the file
	 * @throws java.io.IOException
	 */
	public static String readFile(String filePath) throws IOException {

		String line;
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
		StringBuilder stringBuilder = new StringBuilder();
        String lineSeparator = System.getProperty("line.separator");

		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line);
			stringBuilder.append(lineSeparator);
		}
		reader.close();
		return stringBuilder.toString();
	}

	/*
	 * writes data to a file
	 * 
	 * @param filePath path to file
	 * 
	 * @param data : data to be written to file
	 * 
	 * @throws java.io.IOException
	 */
	public static void writeToFile(String filePath, String data)
			throws IOException {

		Writer writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(new File(filePath), true), "UTF-8"));
		writer.write(data);
		writer.flush();
		writer.close();

	}
}
