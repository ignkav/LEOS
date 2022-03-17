--
-- Copyright 2021 European Commission
--
-- Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
-- You may not use this work except in compliance with the Licence.
-- You may obtain a copy of the Licence at:
--
--     https://joinup.ec.europa.eu/software/page/eupl
--
-- Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the Licence for the specific language governing permissions and limitations under the Licence.
--

------------------------------------
-- Changes to initial Oracle
-- database creation scripts
--
-- adds new column CONTEXT on table USERS for user context
-- creates new unique constraint USERS_UK_LOGIN_CONTEXT on columns LOGIN and CONTEXT 
-- drops existing unique constraint USERS_UK_LOGIN
--
-- change initiated by ANOT-136
------------------------------------

ALTER TABLE "USERS" ADD ("CONTEXT" VARCHAR2(50));
COMMENT ON COLUMN "USERS"."CONTEXT" IS 'User context';

ALTER TABLE "USERS" ADD CONSTRAINT "USERS_UK_LOGIN_CONTEXT" UNIQUE ("LOGIN", "CONTEXT") USING INDEX ENABLE;

ALTER TABLE "USERS" DROP CONSTRAINT "USERS_UK_LOGIN";
