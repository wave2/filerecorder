package org.wave2.recorder;

/**
 * Copyright (c) 2016 Wave2 Limited
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of Wave2 Limited.
 */

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;


public class FileRecorder {

    @Option(name = "-f",aliases = { "--folder" }, usage = "Folder to monitor", metaVar = "Path", required = true)
    private String monitorPath;

    @Option(name = "-r", aliases = { "--repo" }, usage = "Repository folder (holds changes detected)", metaVar = "Path", required = true)
    private String repositoryPath;

    @Option(name = "-d", aliases = { "--debug" }, usage = "Enable debug messages", required = false)
    private boolean debugEnabled;

    @Option(name = "-k", aliases = { "--keep" }, usage = "Add .keep to empty folders", required = false)
    private boolean keepFolders;

    // receives other command line parameters than options
    @Argument
    private List<String> arguments = new ArrayList<String>();

    //Logger
    private static final Logger logger = LoggerFactory.getLogger(FileRecorder.class.getName());

    //Application properties
    private final Properties properties = new Properties();
    private String commitMessage = "";

    /**
     * Load application properties
     */
    private void loadProperties() {
        try {
            properties.load(getClass().getResourceAsStream("/application.properties"));
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Default constructor for File Recorder
     */
    private FileRecorder() {
        loadProperties();

    }

    public static void main(String[] args) throws IOException {
        new FileRecorder().parseArgs(args);
    }

    private void parseArgs(String[] args) throws IOException {
        CmdLineParser parser = new CmdLineParser(this);

        // if you have a wider console, you could increase the value;
        // here 80 is also the default
        parser.setUsageWidth(80);

        try {
            // parse the arguments.
            parser.parseArgument(args);

            // you can parse additional arguments if you want.
            // parser.parseArgument("more","args");

        } catch (CmdLineException e) {
            // if there's a problem in the command line,
            // you'll get this exception. this will report
            // an error message.
            System.err.println(e.getMessage() + "\n");
            System.err.println("FileRecorder - lazy version control for your stuff! Version " + properties.getProperty("application.version"));
            System.err.println("Usage: java -jar fileRecorder.jar -f PATH -r PATH [record]");
            // print the list of available options
            parser.printUsage(System.err);
            System.err.println();
            return;
        }

        if( debugEnabled ) {
            LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
            ch.qos.logback.classic.Logger rootLogger = lc.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
            rootLogger.setLevel(Level.DEBUG);
        }

        // access non-option arguments
        for (String s : arguments) {
            //Record changes
            if (s.equalsIgnoreCase("record")) {
                record();
            }
        }

    }

    private void record() {
        //Check the repository
        logger.info("Looking for changes in " + monitorPath);
        try {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            Repository repository = builder.setWorkTree(new File(monitorPath)).setGitDir(new File(repositoryPath, ".git")).build();
            Git git = new Git(repository);
            StoredConfig config = repository.getConfig();

            //Is this a fileRecorder repository?
            if (!config.getBoolean("fileRecorder", "enabled", false)) {
                //There is no config here, lets try to create a new repo
                try {
                    repository.create();
                } catch (IllegalStateException e) {
                    System.err.println("Repository - " + repositoryPath + " is not a fileRecorder enabled repository.\n\nPlease provide a valid path.");
                    System.exit(1);
                }
                config.setBoolean("fileRecorder", null, "enabled", true);
                config.save();
            }

            Status status = git.status().call();

            //Grab the list of changes
            Set<String> untrackedFiles = status.getUntracked();
            Set<String> untrackedFolders = status.getUntrackedFolders();
            Set<String> modifiedFiles = status.getModified();
            Set<String> missingFiles = status.getMissing();
            ArrayList newFolders = new ArrayList();

            //Files modified
            for (String modified : modifiedFiles) {
                logger.info("Modified file: " + monitorPath + File.separator + modified);
                AddCommand add = git.add();
                add.addFilepattern(modified).call();
                commitMessage += "Modified file: " + monitorPath + File.separator + modified + "\n";
            }

            //Files missing
            for (String missing : missingFiles) {
                logger.info("Missing file: " + monitorPath + File.separator + missing);
                RmCommand remove = git.rm();
                remove.addFilepattern(missing).call();
                commitMessage += "Removing file: " + monitorPath + File.separator + missing + "\n";
            }

            //New files detected
            for (String untrackedFile : untrackedFiles) {
                //Is this an untracked folder?
                for (String untrackedFolder : untrackedFolders) {
                    if (untrackedFile.contains(untrackedFolder)){
                        logger.info("New folder found: " + monitorPath + File.separator + untrackedFolder);
                        newFolders.add(untrackedFolder);
                    }
                }
                logger.info("New file found: " + monitorPath + File.separator + untrackedFile);
                AddCommand add = git.add();
                add.addFilepattern(untrackedFile).call();
                commitMessage += "New file found: " + monitorPath + File.separator + untrackedFile + "\n";
            }

            //Emtpy folders detected
            for (String untrackedFolder : status.getUntrackedFolders()) {
                //Did this folder contain a new file?
                if (!newFolders.contains(untrackedFolder)) {
                    logger.info("Empty folder found: " + monitorPath + File.separator + untrackedFolder);
                    //Add .keep file to folder if option passed
                    if(keepFolders){
                        logger.info("Creating .keep file in folder: " + monitorPath + File.separator + untrackedFolder);
                        Files.createFile(Paths.get(monitorPath + File.separator + untrackedFolder + File.separator + ".keep"));
                        AddCommand add = git.add();
                        add.addFilepattern(untrackedFolder).call();
                        commitMessage += "New folder found: " + monitorPath + File.separator + untrackedFolder + "\n";
                    }
                }
            }

            //Commit all changes
            if (!commitMessage.equals("")) {
                CommitCommand commit = git.commit();
                commit.setMessage(commitMessage).call();
            }
            git.close();
            logger.info("FileRecorder Finished");
        } catch (Exception e) {
            logger.error(e.getClass().toString() + " - " + e.getMessage());
        }
    }
}
