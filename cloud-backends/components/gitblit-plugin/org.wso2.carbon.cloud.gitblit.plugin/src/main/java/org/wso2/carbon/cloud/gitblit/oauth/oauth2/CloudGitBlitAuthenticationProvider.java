package org.wso2.carbon.cloud.gitblit.oauth.oauth2;

import com.gitblit.models.UserModel;
import org.wso2.carbon.appfactory.git.AppFactoryGitBlitUserModel;
import org.wso2.carbon.appfactory.gitblit.oauth.oauth2.AppFactoryGitBlitAuthenticationProvider;
import org.wso2.carbon.cloud.git.CloudRepositoryAuthorizationClient;

public class CloudGitBlitAuthenticationProvider extends AppFactoryGitBlitAuthenticationProvider{

    public CloudGitBlitAuthenticationProvider() {
        super();
    }

    @Override
    public UserModel authenticate(String username, char[] password) {
        username = doConvert(username);
        logger.info("Cloud Plugin: Authenticating User : " + username);
        AppFactoryGitBlitUserModel userModel = (AppFactoryGitBlitUserModel) super.authenticate(username,password);

        if(userModel != null){
            if(!(userModel.getAppFactoryRepositoryAuthorizationClient() instanceof CloudRepositoryAuthorizationClient)){
                CloudRepositoryAuthorizationClient authorizationClient
                        = new CloudRepositoryAuthorizationClient(userModel.username, password, getConfiguration(), getAppFactoryAuthenticationClient());
                userModel.setAppFactoryRepositoryAuthorizationClient(authorizationClient);
            }
        }
        return userModel;
    }

    private  String doConvert(String username) {
        StringBuilder convertedUser = new StringBuilder(username);
        if ("".equals(username)) {
            logger.error("Cloud Plugin: User name can not be empty.");
        }
        else if (username.contains("@")) {
            int index = username.indexOf("@");
            convertedUser.setCharAt(index, '.');
        }
        return convertedUser.toString();
    }

}
