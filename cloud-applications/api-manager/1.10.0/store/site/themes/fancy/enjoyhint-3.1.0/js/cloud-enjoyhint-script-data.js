// Get the name of the API used in tutorial
function getApiName() {
    var apiName = localStorage.getItem("apiName");
    return apiName;
}

var login_apistore_script_data = [
    {
        'click #login-link': 'First of all lets login to API Cloud.'
    }
];

var api_info_script_data = [
    {
        'click #subscribe-button': 'Here is the new API that you published in the API Store. This is' +
        ' how your subscribers see it. Click Subscribe to start using the API.'
    }
];

var goto_mysubscription_message_script_data = [
    {
        'click #messageModal': 'You are subscribed now. Click "Go to My Subscriptions" to go to the ' +
        'OAuth key generation page.'
    }
];

var subscription_token_generate_script_data = [
    {
        'click #btn-generatekeys-prod': 'Click "Generate keys" to get your OAuth token, which, by ' +
        'default, is valid for 10 minutes.'
    },
    {
        selector: '#' + getApiName(),
        event: 'click',
        description: 'Now, click the ' + getApiName() + ' API to open the API\'s overview again.'
    },

];

var subscription_token_regenerate_script_data = [
    {
        'click #btn-regeneratekeys-prod': 'Click "Re-generate keys" to get your OAuth token.'
    },
    {
        selector: '#' + getApiName(),
        event: 'click',
        description: 'Now, click the ' + getApiName() + ' API to open the API\'s overview again.'
    }
];

var ui_tab_script_data = [
    {
        'click #2': 'Click the API Console tab to invoke the API. The API Console is an integrated Swagger UI.'
    }
];

var swagger_script_data = [
    {
        'click #default_get_countries_code': 'Click the GET /countries/{code} method that you created ' +
        'earlier to expand it.'
    },
    {
        'key .parameter required': 'Type a two-letter country code (e.g., "us") and press Tab',
        'keyCode': 9
    },
    {
        'click .submit': 'Now, click "Try it out!" button to invoke the API'
    },
    {
        'click #api_info': 'The API is successfully invoked. You can see the response, invocation ' +
        'URL, and sample Curl command in the API Console.This concludes our tutorial to publish and ' +
        'invoke your first API. To learn more, go back to the API Publisher and click Documentation ' +
        '/ API Cloud.'
    }
];
