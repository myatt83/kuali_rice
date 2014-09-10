INSERT INTO KRIM_PERM_T (PERM_ID, OBJ_ID, VER_NBR, PERM_TMPL_ID, NMSPC_CD, NM, DESC_TXT, ACTV_IND)
VALUES ('KRSAP100030', SYS_GUID(), '1', '46', 'KR-SAP', 'View Note / Attachment Travel Document', 'Applies to travel document', 'Y')
/
INSERT INTO KRIM_PERM_ATTR_DATA_T (ATTR_DATA_ID, OBJ_ID, VER_NBR, PERM_ID, KIM_TYP_ID, KIM_ATTR_DEFN_ID, ATTR_VAL)
VALUES ('KRSAP13300', SYS_GUID(), '1', 'KRSAP100030', '9', '13', 'TravelDocument')
/
INSERT INTO KRIM_ROLE_PERM_T (ROLE_PERM_ID, OBJ_ID, VER_NBR, ROLE_ID, PERM_ID, ACTV_IND)
VALUES ('KRSAP13002', SYS_GUID(), '1', 'KRSAP10004', 'KRSAP100030', 'Y')
/
INSERT INTO KRIM_PERM_T (PERM_ID, OBJ_ID, VER_NBR, PERM_TMPL_ID, NMSPC_CD, NM, DESC_TXT, ACTV_IND)
VALUES ('KRSAP100031', SYS_GUID(), '1', '46', 'KR-SAP', 'View Note / Attachment Travel Document Attachment', 'Applies to travel document attachments', 'Y')
/
INSERT INTO KRIM_PERM_ATTR_DATA_T (ATTR_DATA_ID, OBJ_ID, VER_NBR, PERM_ID, KIM_TYP_ID, KIM_ATTR_DEFN_ID, ATTR_VAL)
VALUES ('KRSAP13400', SYS_GUID(), '1', 'KRSAP100031', '9', '13', 'TravelDocument')
/
INSERT INTO KRIM_PERM_ATTR_DATA_T (ATTR_DATA_ID, OBJ_ID, VER_NBR, PERM_ID, KIM_TYP_ID, KIM_ATTR_DEFN_ID, ATTR_VAL)
VALUES ('KRSAP13401', SYS_GUID(), '1', 'KRSAP100031', '9', '9', 'OTH')
/
INSERT INTO KRIM_ROLE_PERM_T (ROLE_PERM_ID, OBJ_ID, VER_NBR, ROLE_ID, PERM_ID, ACTV_IND)
VALUES ('KRSAP13003', SYS_GUID(), '1', 'KRSAP10003', 'KRSAP100031', 'Y')
/
