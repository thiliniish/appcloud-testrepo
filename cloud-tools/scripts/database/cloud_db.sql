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
  tenantDomain VARCHAR(255) NOT NULL,
  email        VARCHAR(255) NOT NULL,
  uuid         VARCHAR(500) NOT NULL,
  roles        VARCHAR(500) NOT NULL
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

CREATE TABLE RIGHTWAVE_CLOUD_SUBSCRIPTION (
  TENANT_DOMAIN      VARCHAR(100) NOT NULL,
  EMAIL       VARCHAR(100) NOT NULL,
  APP_CLOUD        TINYINT(1) NOT NULL DEFAULT 0,
  API_CLOUD TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (TENANT_DOMAIN, EMAIL)
);

