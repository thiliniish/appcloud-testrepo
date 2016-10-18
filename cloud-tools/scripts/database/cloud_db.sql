CREATE TABLE SUBSCRIPTIONS (
  tenantDomain      VARCHAR(120) NOT NULL,
  app_cloud         TINYINT(1),
  integration_cloud TINYINT(1),
  api_cloud         TINYINT(1)
);

CREATE TABLE TEMP_REGISTRATION (
  email     VARCHAR(255) NOT NULL,
  uuid      VARCHAR(500) NOT NULL,
  isInvitee TINYINT(1),
  dateTime DATETIME NOT NULL,
  PRIMARY KEY (email)
);

CREATE TABLE TENANT_USER_MAPPING (
  userName     VARCHAR(255) NOT NULL,
  tenantDomain VARCHAR(500) NOT NULL
);

CREATE TABLE ORGANIZATIONS (
  tenantDomain VARCHAR(255) NOT NULL,
  displayName  VARCHAR(500) NOT NULL,
  PRIMARY KEY(tenantDomain)
);

CREATE TABLE TEMP_INVITEE (
  tenantDomain varchar(255) NOT NULL,
  email varchar(255) NOT NULL,
  uuid varchar(500) NOT NULL,
  roles varchar(500) NOT NULL,
  dateTime datetime NOT NULL,
  isSelfSigned TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY(tenantDomain, email)
);

CREATE TABLE BILLING_ACCOUNT (
 ACCOUNT_NUMBER varchar(50),
 TENANT_DOMAIN varchar(100) NOT NULL,
 PRIMARY KEY(TENANT_DOMAIN)
);

CREATE TABLE BILLING_STATUS (
 TENANT_DOMAIN varchar(100) NOT NULL,
 SUBSCRIPTION varchar(20) NOT NULL,
 TYPE ENUM('PAID', 'TRIAL', 'FREE') NOT NULL,
 STATUS ENUM('INACTIVE', 'ACTIVE', 'EXTENDED', 'PENDING_DISABLE','DISABLED') NOT NULL,
 START_DATE DATETIME NOT NULL,
 END_DATE DATETIME NOT NULL,
 PRIMARY KEY (TENANT_DOMAIN, SUBSCRIPTION, TYPE)
);

CREATE TABLE BILLING_ACCOUNT_AMENDMENTS (
  ACCOUNT_NUMBER varchar(50) NOT NULL,
  PRODUCT_RATE_PLAN_ID varchar(300) NOT NULL,
  START_DATE DATETIME NOT NULL,
  END_DATE DATETIME NOT NULL,
  SUBSCRIPTION varchar(20) NOT NULL,
  PRIMARY KEY (ACCOUNT_NUMBER, PRODUCT_RATE_PLAN_ID, START_DATE, SUBSCRIPTION)
);

CREATE TABLE BILLING_STATUS_HISTORY (
 TENANT_DOMAIN varchar(100) NOT NULL,
 SUBSCRIPTION varchar(20) NOT NULL,
 ACCOUNT_NUMBER varchar(50),
 TYPE ENUM('PAID','TRIAL','FREE') NOT NULL,
 STATUS ENUM('EXPIRED','DISABLED') NOT NULL,
 START_DATE DATETIME NOT NULL,
 END_DATE DATETIME NOT NULL,
 TENANT_ID int(11) NOT NULL
);

CREATE TABLE BILLING_SUBSCRIPTION_MAPPING (
 CUSTOM_SUBSCRIPTION_ID varchar(300) NOT NULL,
 PRODUCT_RATE_PLAN_ID varchar(300) NOT NULL,
 PRIMARY KEY (CUSTOM_SUBSCRIPTION_ID)
);

CREATE TABLE RIGHTWAVE_CLOUD_SUBSCRIPTION (
  TENANT_DOMAIN      VARCHAR(100) NOT NULL,
  EMAIL       VARCHAR(100) NOT NULL,
  APP_CLOUD        TINYINT(1) NOT NULL DEFAULT 0,
  API_CLOUD TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (TENANT_DOMAIN, EMAIL)
);

CREATE TABLE IF NOT EXISTS MONETIZATION_STATUS (
  TENANT_DOMAIN VARCHAR(255) NOT NULL,
  CLOUD_APPLICATION VARCHAR(20) NOT NULL,
  PRIMARY KEY(TENANT_DOMAIN, CLOUD_APPLICATION)
);

CREATE TABLE IF NOT EXISTS MONETIZATION_PRODUCT_PLANS (
  TENANT_DOMAIN VARCHAR(255) NOT NULL,
  ZUORA_PRODUCT_NAME VARCHAR(255) NOT NULL,
  RATE_PLAN_NAME VARCHAR(255) NOT NULL,
  RATE_PLAN_ID VARCHAR(32) NOT NULL UNIQUE,
  PRIMARY KEY(TENANT_DOMAIN, ZUORA_PRODUCT_NAME, RATE_PLAN_NAME)
);

CREATE TABLE IF NOT EXISTS MONETIZATION_API_CLOUD_PLANS_INFO (
  RATE_PLAN_ID VARCHAR(32) NOT NULL,
  MAX_DAILY_USAGE INT(20) NOT NULL,
  MONTHLY_RENTAL DOUBLE(20,2) NOT NULL,
  UOM_UNIT INT(20) NOT NULL,
  UOM_PRICE DOUBLE(20,2) NOT NULL,
  PRIMARY KEY(RATE_PLAN_ID),
  FOREIGN KEY (RATE_PLAN_ID) REFERENCES MONETIZATION_PRODUCT_PLANS(RATE_PLAN_ID)
);

CREATE TABLE IF NOT EXISTS MONETIZATION_API_CLOUD_SUBSCRIBERS (
  USER_NAME VARCHAR(255) NOT NULL,
  TENANT_DOMAIN VARCHAR(255) NOT NULL,
  TEST_ACCOUNT TINYINT(1) NOT NULL,
  ACCOUNT_NUMBER VARCHAR(50) UNIQUE,
  PRIMARY KEY(USER_NAME, TENANT_DOMAIN)
);

CREATE TABLE IF NOT EXISTS MONETIZATION_API_CLOUD_SUBSCRIPTIONS (
  ACCOUNT_NUMBER VARCHAR(50) NOT NULL,
  AM_APP_NAME VARCHAR(255) NOT NULL,
  AM_API_NAME VARCHAR(255) NOT NULL,
  AM_API_VERSION VARCHAR(30) NOT NULL,
  AM_API_PROVIDER VARCHAR(255) NOT NULL,
  RATE_PLAN_ID VARCHAR(32) NOT NULL,
  SUBSCRIPTION_NUMBER VARCHAR(100) NOT NULL UNIQUE,
  START_DATE DATETIME NOT NULL,
  PRIMARY KEY(ACCOUNT_NUMBER, AM_APP_NAME, AM_API_NAME, AM_API_VERSION),
  FOREIGN KEY (ACCOUNT_NUMBER) REFERENCES MONETIZATION_API_CLOUD_SUBSCRIBERS (ACCOUNT_NUMBER)
);

CREATE TABLE IF NOT EXISTS MONETIZATION_API_CLOUD_SUBSCRIPTIONS_HISTORY (
  ACCOUNT_NUMBER VARCHAR(50) NOT NULL,
  AM_APP_NAME VARCHAR(255) NOT NULL,
  AM_API_NAME VARCHAR(255) NOT NULL,
  AM_API_VERSION VARCHAR(30) NOT NULL,
  AM_API_PROVIDER VARCHAR(255) NOT NULL,
  RATE_PLAN_ID VARCHAR(32) NOT NULL,
  SUBSCRIPTION_NUMBER VARCHAR(100) NOT NULL UNIQUE,
  START_DATE DATETIME NOT NULL,
  END_DATE DATETIME NOT NULL,
  PRIMARY KEY(ACCOUNT_NUMBER, AM_APP_NAME, AM_API_NAME, AM_API_VERSION, START_DATE)
);

CREATE TABLE IF NOT EXISTS USER_LOGIN (
  TENANT_ID int(255) NOT NULL,
  TENANT_DOMAIN varchar(255) NOT NULL,
  LAST_LOGIN_DATE varchar(255) NOT NULL,
  PRIMARY KEY(TENANT_ID)
);

CREATE TABLE IF NOT EXISTS DELETION_TENANTS (
  TENANT_ID INT(11) NOT NULL,
  TENANT_DOMAIN varchar(255) NOT NULL,
  APP TINYINT(1) NOT NULL DEFAULT 0,
  API TINYINT(1) NOT NULL DEFAULT 0,
  CONFIG_PUBSTORE TINYINT(1) NOT NULL DEFAULT 0,
  GOVERNANCE TINYINT(1) NOT NULL DEFAULT 0,
  USER_MGT TINYINT(1) NOT NULL DEFAULT 0,
  CLOUD_MGT TINYINT(1) NOT NULL DEFAULT 0,
  LDAP TINYINT(1) NOT NULL DEFAULT 0,
  CONFIG_BPS TINYINT(1) NOT NULL DEFAULT 0,
  CONFIG_CLOUD_MGT TINYINT(1) NOT NULL DEFAULT 0,
  CONFIG_IS TINYINT(1) NOT NULL DEFAULT 0,
  CONFIG_SS TINYINT(1) NOT NULL DEFAULT 0,
  CONFIG_DAS TINYINT(1) NOT NULL DEFAULT 0,
  CONFIG_AF TINYINT(1) NOT NULL DEFAULT 0,
  CONFIG_AS TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (TENANT_ID)
);

CREATE TABLE IF NOT EXISTS DELETION_STATUS (
  TYPE VARCHAR(255) NOT NULL,
  STATUS SMALLINT(2) DEFAULT NULL,
  PRIMARY KEY (TYPE)
);

CREATE TABLE IF NOT EXISTS DELETION_EXCLUSION_TENANTS (
  TENANT_ID int(255) NOT NULL,
  TENANT_DOMAIN varchar(255) NOT NULL,
  PRIMARY KEY(TENANT_ID)
);

CREATE TABLE IF NOT EXISTS MONETIZATION_API_CLOUD_ACCOUNT (
  ACCOUNT_NUMBER VARCHAR(50) NOT NULL,
  TOKEN_TYPE VARCHAR(25) NOT NULL,
  STRIPE_PUBLISHABLE_KEY VARCHAR(50) NOT NULL,
  SCOPE VARCHAR(20) NOT NULL,
  LIVE_MODE TINYINT(1),
  STRIPE_USER_ID VARCHAR(50) NOT NULL,
  REFRESH_TOKEN VARCHAR(100) NOT NULL,
  ACCESS_TOKEN VARCHAR(100) NOT NULL,
  ACCOUNT_CREATION_DATE DATETIME NOT NULL,
  PRIMARY KEY(ACCOUNT_NUMBER, STRIPE_USER_ID)
);


