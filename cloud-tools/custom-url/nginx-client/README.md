# cloud

# Configurations needed for nginx client ( cloud-domain-mapper-1.0.0.jar)

(1.) Change config.properties file to include "nginx.api.config.path=<path_to_store_custom_vhost_configs>"

(2.) Add the following line in "nginx.conf"
        include <path_to_store_custom_vhost_configs>/*;
     
