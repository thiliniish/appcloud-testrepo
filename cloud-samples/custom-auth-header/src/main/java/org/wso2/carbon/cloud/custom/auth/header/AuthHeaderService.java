package org.wso2.carbon.cloud.custom.auth.header;

import com.google.gson.JsonObject;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * This is a sample MSF4J service which checks the Auth header of a requse
 */
@Path("/custom-auth-header")
@Produces({ "application/json" })
public class AuthHeaderService {

    private static final Logger logger = LoggerFactory.getLogger(AuthHeaderService.class);

    private static final String CODE = "code";
    private static final String MESSAGE = "message";

    @GET
    @Path("/validate-header")
    public Response validateHeader(@HeaderParam("Authorization") String authHeader) {

        logger.info("[validate-header] : Authorization Header Value : " + authHeader);

        JsonObject responseObj = new JsonObject();
        if (authHeader != null) {
            if ("Bearer 1234".equals(authHeader)) {
                responseObj.addProperty(CODE, HttpStatus.SC_OK);
                responseObj.addProperty(MESSAGE, "Authorization header validation successful");
            } else {
                responseObj.addProperty(CODE, HttpStatus.SC_UNAUTHORIZED);
                responseObj.addProperty(MESSAGE, "Authorization header validation failed");
            }
        } else {
            responseObj.addProperty(CODE, HttpStatus.SC_BAD_REQUEST);
            responseObj.addProperty(MESSAGE, "Authorization header is missing");
        }
        return Response.status(responseObj.get(CODE).getAsInt()).entity(responseObj).build();
    }

}
