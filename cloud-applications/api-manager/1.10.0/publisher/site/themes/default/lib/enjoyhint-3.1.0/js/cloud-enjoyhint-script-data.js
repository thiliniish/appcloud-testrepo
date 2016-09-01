var item_listing_with_worldbank_script_data = [
    {
        'click #listing-title': 'Welcome to WSO2 API Cloud. In this tutorial, we will lead you through' +
        ' publishing and invoking an API based on World Banks country statistics data. As you already ' +
        'have an API named WorldBank, let\'s use a different name and a context when creating the API.' +
        ' Click the highlighted area to continue the interactive tutorial.'        
    },
    {
        'click #menu-api-add': 'Click "Add" to get started.'
    }
];

var item_listing_with_apis_script_data = [
    {
        'click #menu-api-add': 'Welcome to WSO2 API Cloud. In this tutorial, we will lead you through' +
        ' publishing and invoking an API based on World Bank\'s country statistics data. Click "Add" ' +
        'to get started'
    }
];

var item_listing_script_data = [
    {
        'click #listing': 'Welcome to WSO2 API Cloud. In this tutorial, we will lead you through ' +
        'publishing and invoking an API based on World Banks country statistics data. Click the ' +
        'highlighted area to continue the interactive tutorial.'
    },
    {
        'click #btnAddNewApi': 'Click New API to get started.'
    }
];

var trial_expired_script_data = [
    {
        'click #messageModal': 'Your trial unfortunately expired. Please upgrade to a paid plan or request extension.'
    }
];

var item_add_script_data = [
    {
        selector: '#create-new-api',
        event: 'click',
        description: 'Let\'s create an API from scratch. Select the Design new API option.',
        shape: 'circle'
    },
    {
        selector: '#designNewAPI',
        event: 'click',
        description: 'Start creating a new API'
    }

];

var item_design_script_data = [
    {
        'key #name': 'Lets type WorldBank as the API display name and press Tab',
        'keyCode': 9
    },
    {
        'key #context': 'The API\'s context is included in its URL path. Type "wb" as the context ' +
        'and press Tab.',
        'keyCode': 9
    },
    {
        'key #version': 'Type 1.0.0 as the API version and press Tab',
        'keyCode': 9
    },
    {
        'key #resource_url_pattern': 'Let\'s create a REST resource called "countries" with the ' +
        'country\'s code as a parameter. Type "countries/{code}" as the URL Pattern and press Tab.',
        'keyCode': 9
    },
    {
        selector: '#get',
        event: 'click',
        description: 'Click the GET option to allow the GET HTTP method for this URL pattern.',
        shape: 'circle'
    },
    {
        'click #add_resource': 'Click Add to add this pattern to the list of allowed REST resources'
    },
    {
        'click #go_to_implement': 'Next, click Implement to proceed to the implementation phase of' +
        ' the API creation.'
    }
];

var item_design_with_worldbank_api_script_data = [
    {
        'key #name': 'Lets type API display name. API name is unique. As you already have an ' +
        'API named "WorldBank" let\'s type a different name. Ex: WorldBank2, WorldBankTest etc.. ' +
        'and press Tab',
        'keyCode': 9
    },
    {
        'key #context': 'Context is what API will have in the URL path. API context also cannot be ' +
        'duplicate. So let\'s type a different context than "wb". Ex: wb1, wbtest etc... and press Tab.',
        'keyCode': 9
    },
    {
        'key #version': 'Type 1.0.0 as the API version and press Tab',
        'keyCode': 9
    },
    {
        'key #resource_url_pattern': 'Lets define REST resource "countries" that then takes country ' +
        'code as the parameter. To do this, type "countries/{code}" as the URL Pattern and press Tab.',
        'keyCode': 9
    },
    {
        selector: '#get',
        event: 'click',
        description: 'Select the GET checkbox to allow GET method for this URL pattern',
        shape: 'circle'
    },
    {
        'click #add_resource': 'Click Add to add this pattern to the list of allowed REST resources'
    },
    {
        'click #go_to_implement': 'Now click Implement to proceed to the implementation phase of the' +
        ' API creation'
    }
];

var item_implement_script_data = [

    {
        'click #select-managed-api': 'Let\'s implement the actual API rather than a prototype. Click' +
        ' the Managed API option to proceed.'
    },
    {
        'key #jsonform-0-elt-production_endpoints': 'Provide an existing backend service URL for World ' +
        'Bank data in our example, type: http://api.worldbank.org and press Tab',
        'keyCode': 9
    },
    {
        'click #go_to_manage': 'Next, click the "Next: Manage" button to proceed to the last phase of' +
        ' the API creation.'
    }

];

var item_info_script_data = [
    {
        'click #goToStore': 'Click the Go to API Store link to open the API in your default API Store.'
    }

];

var item_manage_script_data = [
    {
        'click .btn-group': 'Let\'s select usage tiers or subscription plans that you offer to subscribers.'
    },
    {
        'click .checkbox:eq(0)': 'Select Gold from the Tier Availability dropdown list.'
    },
    {
        'click #publish_api': 'Click Save & Publish to publish the API in the API Store.'
    }
];

var item_manage_success_message_script_data = [
    {
        'click #goToStore-btn': 'Your API is now published. Click "Go to API Store" to see the API ' +
        'in the API Store and subscribe to it.'
    }
];
