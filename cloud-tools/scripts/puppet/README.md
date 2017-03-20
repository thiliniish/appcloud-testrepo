# cloud

# This is the script to add packs and jars from Cloud Git repository to puppet modules

(1.) Make sure to update the "INSTRUCTIONS_FILE" variable with the path to file with instructions to add changes to puppet.

(2.) Refer to "file.txt" for example structure of instructions.
     You can list the components to be added in each line.
     The format should be <base-module>,<path-to-git-component>,<path-to-puppet-directory>,<build-flag>,<current-file-name>,<new-file-name>
     
        - base-module : e.g. "as","wso2base" 
        - path-to-git-component : make sure to exclude git home from path
        - path-to-puppet-directory : e.g. DROPINS_PATH,CLOUDMGT_APP_PATH,CLOUDMGT_XML_PATH 
        - build-flag : true/false depending on whether a mvn build is required
        - current-file-name : the component name
        - new-file-name : this is equal to current-file-name unless there is a change of name

(3.) You can comment out any unnecessary components by adding a "#" in front of relevant line.

(4.) Make sure to run the script as a bash script.


