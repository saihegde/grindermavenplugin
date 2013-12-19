//   Copyright 2012 Giuseppe Iacono, Felipe Munoz Castillo
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package com.fides;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.String;
import java.util.regex.Matcher;

import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide Maven repository utilities 
 * 
 * @author Giuseppe Iacono
 */
public class MavenUtilities 
{
	private final static Logger logger = LoggerFactory.getLogger(Report.class);	
	
	/**
	 * @return the path of Maven local repository
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	public static String getMavenLocalRepository () 
			throws FileNotFoundException, IOException, XmlPullParserException 
	{
		File m2Dir = new File( System.getProperty( "user.home" ), ".m2" );
        File settingsFile = new File( m2Dir, "settings.xml" );
        String localRepo = null;
        if ( settingsFile.exists() ) {
            Settings settings = new SettingsXpp3Reader().read( new FileReader( settingsFile ) );
            localRepo = settings.getLocalRepository();
        }
        if ( localRepo == null ){
            localRepo = System.getProperty( "user.home" ) + File.separator+".m2"+ File.separator+"repository";
        }
        
        logger.debug("-------getMavenLocalRepository------------");
		logger.debug("----------------------------------------");
		logger.debug(" localRepo: {}", localRepo);
		logger.debug("----------------------------------------");
		
        return localRepo;
	}
	
	/**
	 * @param groupId the groupId of the plugin
	 * @param artifactId the artifactId of the plugin
	 * @param version the version of the plugin
	 * 
	 * @return the relative path of plugin jar
	 */
	public static String getPluginRelativePath(String groupId, String artifactId, String version) 
	{
		logger.debug("-------getPluginRelativePath------------");
		logger.debug("----------------------------------------");
		
		logger.debug("-------------groupId: {}---------", groupId);
		
		String[] words = groupId.split("\\.");
		
		logger.debug("-------------words.length: {}------------", words.length);
		
		StringBuffer path = new StringBuffer();
		for (String word : words) {
			path.append(word).append(File.separator);
			logger.debug("-------------word: {}------------", word);
		}
		
		logger.debug("-------------path: {}------", path.toString());
		
		String artifactVersionPath = artifactId + File.separator +  version + File.separator;
		String jarPath = artifactId + "-"+  version + ".jar";		
		
		logger.debug("-------------artifactVersionPath: {}------", artifactVersionPath);
		logger.debug("-------------jarPath: {0}------", jarPath);
		
		logger.debug("----------------------------------------");
		
		return path.toString() + artifactVersionPath + jarPath;
	}

	/**
	 * @param groupId the groupId of the plugin
	 * @param artifactId the artifactId of the plugin
	 * @param version the version of the plugin
	 * 
	 * @return the absolute path of plugin jar
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	public static String getPluginAbsolutePath(String groupId, String artifactId, String version) 
			throws FileNotFoundException, IOException, XmlPullParserException
	{
		return getMavenLocalRepository() + File.separator + 
				getPluginRelativePath(groupId, artifactId, version);
	}
	
	/**
	 * @return the absolute path of Maven local repository
	 */
	public static String getCurrentDir() 
	{
		StringBuffer directory = new StringBuffer();
		directory.append(System.getProperty("java.io.tmpdir"))
				.append(File.separator)
				.append("jar_grinderplugin");
		return directory.toString();
	}	
	
	/**
	 * Replace special caracter '\' with the special caracter '/'.
	 * 
	 * @param path the path to normalize
	 * 
	 * @return the normalized representation of path
	 */
	public static String normalizePath(String path)
	{
		String normalized = null;
		normalized = path.replaceAll(Matcher.quoteReplacement("\\"), "/");
		return normalized;
	}
}
