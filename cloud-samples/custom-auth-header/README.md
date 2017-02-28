# Cloud Samples - Custom Auth Header Sample

This is a sample for : https://docs.wso2.com/display/APICloud/Pass+a+Custom+Authorization+Token+to+the+Backend

Instructions;

**NOTE:** You must have JDK 8 inorder to build the components

(1) Build custom-auth-header

(2) Run the micro service by 

`java -jar custom-auth-header-1.0.0-jar-with-dependencies.jar`

(3) You can see the output by 

`curl -H "Authorization: Bearer 1234" http://localhost:8080/custom-auth-header/validate-header -v`