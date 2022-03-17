---------------------------------------------------------------------------------------------------------
-- SQL queries for running load tests with JMeter.
-- Make sure that the following entries exist in the database:
-- * the demo user with id 3
-- * the __world__ group with id 1
-- * the DIGIT group with id 44
-- You can change the number of annotations created by adapting the number of times the loop is executed.
---------------------------------------------------------------------------------------------------------

set DEFINE off;

declare
  createdDate DATE;
  updatedDate DATE;
begin
  insert into SYSTEM.DOCUMENTS (DOCUMENT_ID,URI) values (84,'uri://LEOS/dummy_load_test');

  commit;

  insert into SYSTEM.METADATA (ID,DOCUMENT_ID,GROUP_ID,SYSTEM_ID) values (42,84,1,'LEOS');

  for i in 1..10000 loop
    select to_date(trunc(dbms_random.value(to_char(DATE '2000-01-01','J'),to_char(DATE '2010-12-31','J'))),'J') into createdDate from DUAL;
    select to_date(trunc(dbms_random.value(to_char(DATE '2011-01-01','J'),to_char(DATE '2020-08-31','J'))),'J') into updatedDate from DUAL;
    insert into SYSTEM.ANNOTATIONS (ANNOTATION_ID,TEXT,CREATED,UPDATED,USER_ID,SHARED,TARGET_SELECTORS,METADATA_ID,STATUS,SENT_DELETED,RESP_VERSION_SENT_DELETED) values (concat('load-test-',to_char(i)),to_char(i),createdDate,updatedDate,3,1,'[{"selector":null,"source":"uri://LEOS/dummy_load_test"}]',42,0,0,0);
  end loop;

  commit;

  -- ISC - step 1/9

  insert into SYSTEM.METADATA (ID,DOCUMENT_ID,GROUP_ID,SYSTEM_ID,KEYVALUES) values (43,84,44,'LEOS','ISCReference:ISC/2019/4'||chr(10)||'responseId:SG'||chr(10)||'responseVersion:1');

  for i in 10001..20000 loop
    select to_date(trunc(dbms_random.value(to_char(DATE '2000-01-01','J'),to_char(DATE '2010-12-31','J'))),'J') into createdDate from DUAL;
    select to_date(trunc(dbms_random.value(to_char(DATE '2011-01-01','J'),to_char(DATE '2020-08-31','J'))),'J') into updatedDate from DUAL;
    insert into SYSTEM.ANNOTATIONS (ANNOTATION_ID,TEXT,CREATED,UPDATED,USER_ID,SHARED,TARGET_SELECTORS,METADATA_ID,STATUS,SENT_DELETED,RESP_VERSION_SENT_DELETED) values (concat('load-test-',to_char(i)),to_char(i),createdDate,updatedDate,3,1,'[{"selector":null,"source":"uri://LEOS/dummy_load_test"}]',43,0,0,0);
  end loop;

  commit;

  -- ISC - step 2/9

  insert into SYSTEM.METADATA (ID,DOCUMENT_ID,GROUP_ID,SYSTEM_ID,KEYVALUES,RESPONSE_STATUS) values (44,84,44,'LEOS','ISCReference:ISC/2019/4'||chr(10)||'responseId:SG'||chr(10)||'responseVersion:1',1);

  for i in 20001..30000 loop
    select to_date(trunc(dbms_random.value(to_char(DATE '2000-01-01','J'),to_char(DATE '2010-12-31','J'))),'J') into createdDate from DUAL;
    select to_date(trunc(dbms_random.value(to_char(DATE '2011-01-01','J'),to_char(DATE '2020-08-31','J'))),'J') into updatedDate from DUAL;
    insert into SYSTEM.ANNOTATIONS (ANNOTATION_ID,TEXT,CREATED,UPDATED,USER_ID,SHARED,TARGET_SELECTORS,METADATA_ID,STATUS,SENT_DELETED,RESP_VERSION_SENT_DELETED) values (concat('load-test-',to_char(i)),to_char(i),createdDate,updatedDate,3,1,'[{"selector":null,"source":"uri://LEOS/dummy_load_test"}]',44,0,0,0);
  end loop;

  commit;

  -- ISC - step 3/9

  insert into SYSTEM.METADATA (ID,DOCUMENT_ID,GROUP_ID,SYSTEM_ID,KEYVALUES) values (45,84,44,'LEOS','ISCReference:ISC/2019/4'||chr(10)||'responseId:SJ'||chr(10)||'responseVersion:1');

  for i in 30001..40000 loop
    select to_date(trunc(dbms_random.value(to_char(DATE '2000-01-01','J'),to_char(DATE '2010-12-31','J'))),'J') into createdDate from DUAL;
    select to_date(trunc(dbms_random.value(to_char(DATE '2011-01-01','J'),to_char(DATE '2020-08-31','J'))),'J') into updatedDate from DUAL;
    insert into SYSTEM.ANNOTATIONS (ANNOTATION_ID,TEXT,CREATED,UPDATED,USER_ID,SHARED,TARGET_SELECTORS,METADATA_ID,STATUS,SENT_DELETED,RESP_VERSION_SENT_DELETED) values (concat('load-test-',to_char(i)),to_char(i),createdDate,updatedDate,3,1,'[{"selector":null,"source":"uri://LEOS/dummy_load_test"}]',45,0,0,0);
  end loop;

  commit;

  -- ISC - step 4/9

  insert into SYSTEM.METADATA (ID,DOCUMENT_ID,GROUP_ID,SYSTEM_ID,KEYVALUES) values (46,84,44,'LEOS','ISCReference:ISC/2019/4'||chr(10)||'responseId:SG'||chr(10)||'responseVersion:2');

  for i in 40001..50000 loop
    select to_date(trunc(dbms_random.value(to_char(DATE '2000-01-01','J'),to_char(DATE '2010-12-31','J'))),'J') into createdDate from DUAL;
    select to_date(trunc(dbms_random.value(to_char(DATE '2011-01-01','J'),to_char(DATE '2020-08-31','J'))),'J') into updatedDate from DUAL;
    insert into SYSTEM.ANNOTATIONS (ANNOTATION_ID,TEXT,CREATED,UPDATED,USER_ID,SHARED,TARGET_SELECTORS,METADATA_ID,STATUS,SENT_DELETED,RESP_VERSION_SENT_DELETED) values (concat('load-test-',to_char(i)),to_char(i),createdDate,updatedDate,3,1,'[{"selector":null,"source":"uri://LEOS/dummy_load_test"}]',46,0,0,0);
  end loop;

  commit;

  -- ISC - step 5/9

  insert into SYSTEM.METADATA (ID,DOCUMENT_ID,GROUP_ID,SYSTEM_ID,KEYVALUES,RESPONSE_STATUS) values (47,84,44,'LEOS','ISCReference:ISC/2019/4'||chr(10)||'responseId:SG'||chr(10)||'responseVersion:2',1);

  for i in 50001..60000 loop
    select to_date(trunc(dbms_random.value(to_char(DATE '2000-01-01','J'),to_char(DATE '2010-12-31','J'))),'J') into createdDate from DUAL;
    select to_date(trunc(dbms_random.value(to_char(DATE '2011-01-01','J'),to_char(DATE '2020-08-31','J'))),'J') into updatedDate from DUAL;
    insert into SYSTEM.ANNOTATIONS (ANNOTATION_ID,TEXT,CREATED,UPDATED,USER_ID,SHARED,TARGET_SELECTORS,METADATA_ID,STATUS,SENT_DELETED,RESP_VERSION_SENT_DELETED) values (concat('load-test-',to_char(i)),to_char(i),createdDate,updatedDate,3,1,'[{"selector":null,"source":"uri://LEOS/dummy_load_test"}]',47,0,0,0);
  end loop;

  commit;

  -- ISC - step 6/9 (ignored as deletion only)

  -- ISC - step 7/9

  insert into SYSTEM.METADATA (ID,DOCUMENT_ID,GROUP_ID,SYSTEM_ID,KEYVALUES,RESPONSE_STATUS) values (48,84,44,'LEOS','ISCReference:ISC/2019/4'||chr(10)||'responseId:SG'||chr(10)||'responseVersion:3',1);

  for i in 60001..70000 loop
    select to_date(trunc(dbms_random.value(to_char(DATE '2000-01-01','J'),to_char(DATE '2010-12-31','J'))),'J') into createdDate from DUAL;
    select to_date(trunc(dbms_random.value(to_char(DATE '2011-01-01','J'),to_char(DATE '2020-08-31','J'))),'J') into updatedDate from DUAL;
    insert into SYSTEM.ANNOTATIONS (ANNOTATION_ID,TEXT,CREATED,UPDATED,USER_ID,SHARED,TARGET_SELECTORS,METADATA_ID,STATUS,SENT_DELETED,RESP_VERSION_SENT_DELETED) values (concat('load-test-',to_char(i)),to_char(i),createdDate,updatedDate,3,1,'[{"selector":null,"source":"uri://LEOS/dummy_load_test"}]',48,0,0,0);
  end loop;

  commit;

  -- ISC - step 8/9

  insert into SYSTEM.METADATA (ID,DOCUMENT_ID,GROUP_ID,SYSTEM_ID,KEYVALUES,RESPONSE_STATUS) values (49,84,44,'LEOS','ISCReference:ISC/2019/4'||chr(10)||'responseId:SJ'||chr(10)||'responseVersion:1',1);

  for i in 70001..80000 loop
    select to_date(trunc(dbms_random.value(to_char(DATE '2000-01-01','J'),to_char(DATE '2010-12-31','J'))),'J') into createdDate from DUAL;
    select to_date(trunc(dbms_random.value(to_char(DATE '2011-01-01','J'),to_char(DATE '2020-08-31','J'))),'J') into updatedDate from DUAL;
    insert into SYSTEM.ANNOTATIONS (ANNOTATION_ID,TEXT,CREATED,UPDATED,USER_ID,SHARED,TARGET_SELECTORS,METADATA_ID,STATUS,SENT_DELETED,RESP_VERSION_SENT_DELETED) values (concat('load-test-',to_char(i)),to_char(i),createdDate,updatedDate,3,1,'[{"selector":null,"source":"uri://LEOS/dummy_load_test"}]',49,0,0,0);
  end loop;

  commit;

  -- ISC - step 9/9 (ignored as deletion only)
end;
