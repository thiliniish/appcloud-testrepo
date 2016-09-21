// Get the name of the API used in tutorial
function getApiName() {
    var apiName = localStorage.getItem("apiName");
    return apiName;
}

var login_apistore_script_data = [
    {
        'click .need-login': 'First of all lets login to API Cloud.',
        'showSkip' : false
    }
];

var api_info_script_data = [
    {
        'click #subscribe-button': 'Here is the new API that you published in the API Store. This is' +
        ' how your subscribers see it. Click Subscribe to start using the API.',
        'showSkip' : false
    }
];

var goto_mysubscription_message_script_data = [
    {
        'click .modal-content': 'You are subscribed now. Click "View Subscriptions" to go to the ' +
        'OAuth key generation page.',
        'showSkip' : false
    }
];

var generate_production_keys_data = [
    {
        'click #production-keys-tab': 'Click "Production keys" to go to the ' +
        'Production OAuth key generation tab.',
        'showSkip' : false
    },
    {
        'click .generatekeys': 'Click "Generate keys" to get your OAuth token, which, by ' +
        'default, is valid for 60 minutes.',
        'showSkip' : false
    },
    {
        'click #subscriptions-tab': 'Now you have generated an OAuth access token for accessing the API you created. ' +
        'Click "Subscriptions" to see your subscription information',
        'showSkip' : false
    },
    {
        'click #subscription-table': 'Now, click the ' + getApiName() + ' API to open the API\'s overview again.',
        'showSkip' : false
    }

];

var re_generate_production_keys_data = [
    {
        'click #production-keys-tab': 'Click "Production keys" to go to the ' +
        'Production OAuth key generation tab.',
        'showSkip' : false
    },
    {
        'click #production': 'Since you have already generated a token, lets Regenerate a new token. ' +
        'Click the highlighted area to start.',
        'showSkip' : false
    },
    {
        'click .regenerate': 'Click "Re-generate keys" to generate the new OAuth token.',
        'showSkip' : false
    },
    {
        'click #subscriptions-tab': 'Now you have generated an OAuth access token for accessing the API you created. ' +
        'Click "Subscriptions" to see your subscription information',
        'showSkip' : false
    },
    {
        'click #subscription-table': 'Now, click the ' + getApiName() + ' API to open the API\'s overview again.',
        'showSkip' : false
    }

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
        'click #1': 'Click the API Console tab to invoke the API. The API Console is an integrated Swagger UI.',
        'showSkip' : false
    }
];

var swagger_script_data = [
    {
        'click #default_get_countries_code': 'Click the GET /countries/{code} method that you created ' +
        'earlier to expand it.',
        'showSkip' : false
    },
    {
        'key .parameter required': 'Type a two-letter country code (e.g., "us") and press Tab',
        'keyCode': 9,
        'showSkip' : false
    },
    {
        'click .submit': 'Now, click "Try it out!" button to invoke the API',
        'showSkip' : false
    },
    {
        'click #default_get_countries_code': 'The API is successfully invoked. You can see the response, invocation ' +
        'URL, and sample Curl command in the API Console.This concludes our tutorial to publish and ' +
        'invoke your first API. To learn more, go back to the API Publisher and click Documentation ' +
        '/ API Cloud.',
        'showSkip' : false
    }
];
