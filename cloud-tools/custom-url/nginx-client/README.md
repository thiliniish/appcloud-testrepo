# cloud

# To install nginx client

(1.) Run the nginxScript.sh to checkout the client and build it
     The domain-mapper will be added to "/mnt/domain-mapper"

(2.) Make sure to update aes-keystore.jck, wso2carbon.jks and 
     client-truststore.jks according to the environment
     
(3.) Update config.properties file according to the environment.
     You'll have to update the following properties.
        remoteregistry.username
        remoteregistry.password
        remoteregistry.url
        messageBrokerUrl
        region
        keystore.alias
        keystore.password
        keystore.key.password
        wso2.keystore.password
     
