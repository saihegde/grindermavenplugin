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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import com.jcabi.aether.Aether;


/**
 * Provide methods to configure the plug-in
 * @requiresDependencyResolution runtime
 * @author Giuseppe Iacono
 */
public abstract class GrinderPropertiesConfigure extends AbstractMojo
{
	// Jython version for The Grinder
	public static final String GRINDER_JYTHON_VERSION = "2.2.1";

	// Jython version for The Grinder Analyzer
	public static final String GRINDER_ANALYZER_JYTHON_VERSION = "2.5.2";	
	
	// default agent
	private static final boolean DEFAULT_DAEMON_OPTION = false;						
	
	// default agent sleep time in milliseconds
	private static final long DEFAULT_DAEMON_PERIOD = 60000;							
	
	// default local path test directory
	private static final String PATH_TEST_DIR = "src/test/jython";  		
	
	// local configuration directory
	private static final String CONFIG = "target/test/config";				
	
	// local grinder properties directory
	private static final String PATH_PROPERTIES_DIR = "src/test/config"; 	
	
	// local log directory 
	private static final String LOG_DIRECTORY = "target/test/log_files"; 			
	
	// local tcpproxy directory
	private static final String TCP_PROXY_DIRECTORY = "target/test/tcpproxy";	
	
	// grinder properties
	private Properties propertiesPlugin = new Properties();					

	// configuration file
	private File fileProperties = null;										
	
	// grinder properties file path
	private String pathProperties = null;									

	// jython test path 
	private String test = null;												

	// value of agent daemon option
	private boolean daemonOption = DEFAULT_DAEMON_OPTION;							
	
	// value of agent sleep time
	private long daemonPeriod = DEFAULT_DAEMON_PERIOD;								 
	
	// GrinderPropertiesConfigure logger
	private final Logger logger = LoggerFactory.getLogger("GrinderPropertiesConfigure");
	
	/**
	 * List of properties defined in the pom.xml file of Maven project.
	 * 
	 * @parameter
	 */
	private Map<String, String> properties;

	/**
	 * The grinder properties file path defined in the pom.xml file of Maven project.
	 * 
	 * @parameter
	 */
	private String path;

	/**
	 * The absolute path of test script directory defined in the pom.xml file of Maven project.
	 * 
	 * @parameter
	 */
	private String pathTest;

	/**
	 * Agent daemon option defined in the pom.xml file of Maven project.
	 * 
	 * @parameter
	 */
	private boolean daemon_option;

	/**
	 * Agent sleep time in milliseconds defined in the pom.xml file of Maven project.
	 * 
	 * @parameter
	 */
	private long daemon_period;
	
	/**
	 * List of Plugin dependencies
	 * 
     * @parameter expression="${plugin.artifacts}"
     */
    private List<Artifact> pluginArtifacts; 
    
    /**
     * The Maven Project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;
    
   	public List<Artifact> getPluginArtifacts() {
		return pluginArtifacts;
	}

	public void setPluginArtifacts(List<Artifact> pluginArtifacts) {
		this.pluginArtifacts = pluginArtifacts;
	}
	
	public static boolean getDEFAULT_DAEMON_OPTION() {
		return DEFAULT_DAEMON_OPTION;
	}
	
	public static long getDEFAULT_DAEMON_PERIOD() {
		return DEFAULT_DAEMON_PERIOD;
	}
	
	public static String getPATH_TEST_DIR() {
		return PATH_TEST_DIR;
	}
	
	public static String getCONFIG() {
		return CONFIG;
	}

	public static String getPATH_PROPERTIES_DIR() {
		return PATH_PROPERTIES_DIR;
	}

	public static String getLOG_DIRECTORY() {
		return LOG_DIRECTORY;
	}

	public static String getTCP_PROXY_DIRECTORY() {
		return TCP_PROXY_DIRECTORY;
	}	
	
	public Properties getPropertiesPlugin() {
		return propertiesPlugin;
	}

	public void setPropertiesPlugin(Properties propertiesPlugin) {
		this.propertiesPlugin = propertiesPlugin;
	}

	public File getFileProperties() {
		return fileProperties;
	}

	public void setFileProperties(File fileProperties) {
		this.fileProperties = fileProperties;
	}

	public String getPathProperties() {
		return pathProperties;
	}
	
	public void setPathProperties(String pathProperties) {
		this.pathProperties = pathProperties;
	}
	
	public String getTest() {
		return test;
	}

	public void setTest(String test) {
		this.test = test;
	}
	
	public boolean isDaemonOption() {
		return daemonOption;
	}

	public boolean getdaemonOption() {
		return daemonOption;
	}
	
	public void setDaemonOption(boolean daemonOption) {
		this.daemonOption = daemonOption;
	}

	public long getDaemonPeriod() {
		return daemonPeriod;
	}
	
	public void setDaemonPeriod(long daemonPeriod) {
		this.daemonPeriod = daemonPeriod;
	}
	
	public Logger getLogger() {
		return logger;
	}
	
	protected abstract String getJythonVersion();

	private void setClassPath() {
		// Print the list of plugin dependencies
		logger.debug("------------------------------------------------------------");
		logger.debug("------------------PROJECT DEPENDENCIES----------------------");

		Artifact a = null;
		List<Artifact> artifacts = pluginArtifacts;
		StringBuffer pluginDependencies = new StringBuffer();
		String grinderJar = null;
				
		for (Iterator<Artifact> i = artifacts.iterator();  i.hasNext();) {
			a = (Artifact) i.next();
			logger.debug("------------------------------------------------------------");
			if (!isGrinderArtifact(a)
					&& ( !isJythonArtifact(a) || (a.getArtifactId().contains("jython") && a.getVersion().equals(getJythonVersion())))){
				
				System.out.println("GroupId: " + a.getGroupId() 
								+ "\nArtifactId: " + a.getArtifactId() 
								+ "\nVersino: " + a.getVersion());
				try {
					grinderJar = MavenUtilities.getPluginAbsolutePath(
														a.getGroupId(), 
														a.getArtifactId(), 
														a.getVersion());
					grinderJar = MavenUtilities.normalizePath(grinderJar);
					pluginDependencies.append(grinderJar);
					if (i.hasNext()) {
						pluginDependencies.append(File.pathSeparator);
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (XmlPullParserException e) {
					e.printStackTrace();
				}				
			}
		}
		
		propertiesPlugin.setProperty("grinder.jvm.classpath", pluginDependencies.toString() + File.pathSeparator + getClasspathForProjectDependencies());
		
		logger.debug("--- Classpath configured");
	}
	
	/**
	 * 
	 */
	protected String getClasspathForProjectDependencies() {
		StringBuffer projectDependencies = new StringBuffer();
		@SuppressWarnings("unchecked")
		List<Dependency> dependencies = project.getDependencies();
		for (Dependency dependency: dependencies) {
			try {
				File local = new File(MavenUtilities.getMavenLocalRepository());
				Collection<org.sonatype.aether.artifact.Artifact> deps = new Aether(project, local).resolve(
					      new DefaultArtifact(dependency.getGroupId(), dependency.getArtifactId(), dependency.getType(), dependency.getVersion()),
					      "runtime"
					    );
				for (Iterator<org.sonatype.aether.artifact.Artifact> iterator = deps.iterator(); iterator.hasNext();) {
					org.sonatype.aether.artifact.Artifact artifact = (org.sonatype.aether.artifact.Artifact) iterator.next();
					System.out.println("Group Id -----> " + artifact.getGroupId());
					System.out.println("Artifact Id -----> " + artifact.getArtifactId());
					System.out.println("Resolved Version -----> " + artifact.getVersion());
					System.out.println("Is Snapshot:::" + artifact.isSnapshot());
					String projectDependencyJar = MavenUtilities.getPluginAbsolutePath(artifact.getGroupId(),artifact.getArtifactId(),artifact.getVersion());
					projectDependencyJar = MavenUtilities.normalizePath(projectDependencyJar);
					if(projectDependencies.length() > 0){
						projectDependencies.append(File.pathSeparator);
					}
					projectDependencies.append(projectDependencyJar);
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DependencyResolutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return projectDependencies.toString();
	}

	boolean isGrinderArtifact(Artifact artifact){
		return artifact.getArtifactId().equals("grinder");
	}
	
	boolean isJythonArtifact(Artifact artifact){
		return (artifact.getArtifactId().contains("jython"));
	}

	/**
	 * Set grinder properties
	 */
	private void initPropertiesFile() 
	{		
		if (path == null) {		// try to find grinder properties file in the PATH_PROPERTIES_DIR
			
			File[] config = new File(PATH_PROPERTIES_DIR).listFiles();
			
			if (config == null) {
				if(logger.isDebugEnabled()){
					logger.error("");
					logger.error(" ----------------------------");
					logger.error("|   Configuration ERROR!!!   |");
					logger.error(" ----------------------------");
					logger.error("");
					logger.error(" Configuration directory " + PATH_PROPERTIES_DIR + " do not exists!        ");
					logger.error("");
					logger.error(" Create this directory to configure grinder properties file. ");
					System.exit(0);
				}
			}
			
			if (config.length == 0) {
				if(logger.isDebugEnabled()){
					logger.error("");
					logger.error(" ----------------------------");
					logger.error("|   Configuration ERROR!!!   |");
					logger.error(" ----------------------------");
					logger.error("");
					logger.error(" " + PATH_PROPERTIES_DIR + " is empty! ");
					logger.error("");
					logger.error(" Copy grinder properties file in this directory  ");
					logger.error(" or set <path> from POM file. ");
					System.exit(0);
				}
			}
			

			String properties=null;
			int i=0;
			while(i<config.length) {
				if (config[i].getName().endsWith(".properties")) {
					if (properties==null)  {
						properties=config[i].getName();
					} else {
						if(logger.isDebugEnabled()){
							logger.error("");
							logger.error(" ----------------------------");
							logger.error("|   Configuration ERROR!!!   |");
							logger.error(" ----------------------------");
							logger.error("");
							logger.error(" " + PATH_PROPERTIES_DIR + " contain other files ");
							logger.error(" beyond the grinder properties file! ");
							logger.error("");
							logger.error(" found files "+properties + " and " +config[i].getName());
							System.exit(0);
						}
					}
				}
				i ++;
			}

			if (properties == null) {
				if(logger.isDebugEnabled()){
					logger.error("");
					logger.error(" ----------------------------");
					logger.error("|   Configuration ERROR!!!   |");
					logger.error(" ----------------------------");
					logger.error("");
					logger.error(" " + PATH_PROPERTIES_DIR + " don't contain a grinder properties file! ");
					System.exit(0);
				}
			}
			String pathProp = PATH_PROPERTIES_DIR + File.separator + properties;
			setPathProperties(pathProp);
		}
		else {
			setPathProperties(path);	
		}
		
		// load grinder properties from the grinder properties file
		FileInputStream is = null;
		try {
			is = new FileInputStream(pathProperties);
			propertiesPlugin.load(is); 					
		} catch (FileNotFoundException e) {
			if(logger.isDebugEnabled()){
				logger.error("");
				logger.error(" ----------------------------");
				logger.error("|   Configuration ERROR!!!   |");
				logger.error(" ----------------------------");
				logger.error("");
				logger.error(" The grinder properties file path ");
				logger.error(" " + pathProperties);
				logger.error(" set into your POM file do not exists! ");
				System.exit(0);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {}
			}
		}		
		
		// load grinder properties from pom.xml file of Maven project
		/*if (properties != null) {
			Iterator <Map.Entry <String, String>> iterator = properties.entrySet().iterator();
			Map.Entry<String, String> pair;
			while (iterator.hasNext()) {						
				pair = iterator.next();
				if (pair.getValue() != null && pair.getKey().startsWith("grinder.")) {
					propertiesPlugin.setProperty(pair.getKey(), pair.getValue());
				}
			}
			extractGrinderProperty(properties);
		} */

		extractGrinderProperty(System.getProperties());
		extractGrinderProperty(properties);
		setClassPath();
		logger.debug("--- Grinder properties file:  " + pathProperties);
	}

	@SuppressWarnings("rawtypes") 
	private void extractGrinderProperty(Map propertiesSource) {
		if(propertiesSource!=null){
			for (Object pair : propertiesSource.entrySet()){
				if (pair instanceof Map.Entry) {
					Map.Entry entry = (Map.Entry) pair;
					if (entry.getKey() instanceof String) {
						if (entry.getValue() != null && entry.getKey().toString().startsWith("grinder.")) {
							propertiesPlugin.setProperty(entry.getKey().toString(), entry.getValue().toString());
							logger.debug("--Current property: {}", pair);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Set test file path to execute
	 */
	private void initTestFile() 
	{
		File testScript = null;		// test script configured
		String nameTest = null;		// test script path
		String nameScript = propertiesPlugin.getProperty("grinder.script");
		
		if (pathTest == null) {		//  try to find grinder test file in the PATH_TEST_DIR 
			
			File[] jython = new File(PATH_TEST_DIR).listFiles();
			
			if (jython == null) {
				if(logger.isDebugEnabled()){
					logger.error("");
					logger.error(" ----------------------------");
					logger.error("|   Configuration ERROR!!!   |");
					logger.error(" ----------------------------");
					logger.error("");
					logger.error(" " + PATH_TEST_DIR + " do not exists! ");
					logger.error("");
					logger.error(" Create this directory to configure test file. ");
					System.exit(0);
				}
			}
			
			if (jython.length == 0) {
				if(logger.isDebugEnabled()){
					logger.error("");
					logger.error(" ----------------------------");
					logger.error("|   Configuration ERROR!!!   |");
					logger.error(" ----------------------------");
					logger.error("");
					logger.error(" " + PATH_TEST_DIR + " is empty!  	      ");
					logger.error("");
					logger.error(" Copy test file in this directory  ");
					logger.error(" or set <pathTest> from POM file. ");
					System.exit(0);
				}
			}
			
			/*String jython_file=null;
			int i=0;
			while(i<jython.length) {
				if (jython[i].getName().endsWith(".py")) {
					if ( jython_file==null)  {
						 jython_file=jython[i].getName();
					} else {
						if (logger.isDebugEnabled()) {
							logger.error("");
							logger.error(" ----------------------------");
							logger.error("|   Configuration ERROR!!!   |");
							logger.error(" ----------------------------");
							logger.error("");					
							logger.error(" " + PATH_TEST_DIR + " contain other files ");
							logger.error(" beyond the test script! ");
							System.exit(0);
						}
					}
				}
				i ++;
			}


			String grinderScript = propertiesPlugin.getProperty("grinder.script");
			if (grinderScript == null) {
				propertiesPlugin.setProperty("grinder.script","grinder.py");
				grinderScript = propertiesPlugin.getProperty("grinder.script");
			}
			if (!grinderScript.equals(jython_file)) {
				if (logger.isDebugEnabled()) {
					logger.error("");
					logger.error(" ----------------------------");
					logger.error("|   Configuration ERROR!!!   |");
					logger.error(" ----------------------------");
					logger.error("");					
					logger.error(" The grinder.script property do not match");
					logger.error(" with the test script into " + PATH_TEST_DIR);
					System.exit(0);
				}
			}*/
			
			if (nameScript == null) {
				nameTest = PATH_TEST_DIR + File.separator + 
						   "grinder.py";
			}
			else {
				nameTest = PATH_TEST_DIR + File.separator + 
						   nameScript;
			}
		}
		else {
			if (nameScript == null) {
				nameTest = pathTest + File.separator + 
						   "grinder.py";
			}
			else {
				nameTest = pathTest + File.separator + 	
						nameScript;						
			}
		}	
		
		testScript = new File(nameTest);
		
		/*if (!testScript.exists()) {
			if (logger.isDebugEnabled()) {
				logger.error("");
				logger.error(" ----------------------------");
				logger.error("|   Configuration ERROR!!!   |");
				logger.error(" ----------------------------");
				logger.error("");					
				logger.error(" The test file path ");
				logger.error(" " + nameTest);
				logger.error(" set into your POM file do not exists!");
				System.exit(0);
			}
		}*/
		setTest(nameTest);
		
		logger.debug("--- Jython test file:  " + test);
	}
	
	/**
	 * Set log directory
	 */
	private void initLogDirectory() 
	{
		// make sure the logDirectory exists
		File logDirectory = new File(LOG_DIRECTORY);
		if (logDirectory != null && !logDirectory.exists()) {
			logDirectory.mkdirs();
		}

		// set logDirectory
		propertiesPlugin.setProperty("grinder.logDirectory", LOG_DIRECTORY);
		
		logger.debug("--- Log directory:  " + LOG_DIRECTORY);
	}
	
	/**
	 * Set agent daemon option and sleep time
	 */
	private void initAgentOption()
	{
		if (daemon_option == true) {
			daemonOption = true;
			if (daemon_period > 0) {
				daemonPeriod = daemon_period;
			} 
			else {
				daemonPeriod = DEFAULT_DAEMON_PERIOD;
			}
		}
		else {
			daemonOption = DEFAULT_DAEMON_OPTION;
			daemonPeriod = DEFAULT_DAEMON_PERIOD;
		}
		
		logger.debug("--- Agent -daemon option:  " + daemonOption);
		
		if (daemonOption == true) {
			logger.debug("--- Agent sleep time:  " + daemonPeriod);
		}
	}
	
	/**
	 * Initialize configuration directory
	 */
	private void initConfigurationDirectory()
	{
		// make sure the configDirectory exists
		File configDirectory = new File(CONFIG);	
		if (configDirectory != null && !configDirectory.exists()) {
			configDirectory.mkdirs();								
		}
		
		// copy grinder properties to configuration directory
		String pathProperties = CONFIG + File.separator +
								"grinder_agent.properties";
		BufferedWriter out;

		try {
			out = new BufferedWriter(new FileWriter(pathProperties));
			propertiesPlugin.store(out, "Grinder Agent Properties");
		} catch (FileNotFoundException e) {
			System.out.println("File " + pathProperties + " does not exists!!!");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// create configuration file
		fileProperties = new File(pathProperties); 

		// copy test file to configuration directory		
		if (test != null) {
			String line = null;
			String dest_test = CONFIG + File.separator +
							   propertiesPlugin.getProperty("grinder.script");
			BufferedReader source = null;
			BufferedWriter dest = null;
	
			try {
				source = new BufferedReader(new FileReader(test));
				dest = new BufferedWriter(new FileWriter(dest_test));
				line = source.readLine();
				while (line != null) {
					dest.write(line + "\n");
					line = source.readLine();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (source != null || dest != null) {
					try {
						source.close();
						dest.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		logger.debug("--- Grinderplugin to be configured ---");
	}
	
	
	public void execute() throws MojoExecutionException, MojoFailureException{
		
		initPropertiesFile();
		
		initTestFile();
		
		initLogDirectory();
		
		initAgentOption();
				
		initConfigurationDirectory();		
	}
	
}
