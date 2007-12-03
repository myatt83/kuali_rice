CREATE TABLE NOTIFICATION_MSG_DELIVS
(
ID NUMBER(8) NOT NULL,
NOTIFICATION_ID NUMBER(8) NOT NULL,
USER_RECIPIENT_ID VARCHAR2(300) NOT NULL,
MESSAGE_DELIVERY_TYPE_NAME VARCHAR2(500) NOT NULL,
MESSAGE_DELIVERY_STATUS VARCHAR2(15) NOT NULL, 
DELIVERY_SYSTEM_ID VARCHAR2(300),
LOCKED_DATE TIMESTAMP,
DB_LOCK_VER_NBR INTEGER DEFAULT 0 NOT NULL,
CONSTRAINT NOTIFICATION_MSG_DELIVS_PK PRIMARY KEY (ID)
)
/