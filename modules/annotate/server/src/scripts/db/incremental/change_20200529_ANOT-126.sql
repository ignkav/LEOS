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
-- add a new column on ANNOTATIONS table: CONNECTED_ENTITY (foreign key to GROPS table)
-- 
-- change initiated by ANOT-126
------------------------------------
ALTER TABLE "ANNOTATIONS" ADD "CONNECTED_ENTITY_ID" NUMBER NULL;
ALTER TABLE "ANNOTATIONS" ADD CONSTRAINT "ANNOTATIONS_FK_GROUPS" FOREIGN KEY ("CONNECTED_ENTITY_ID") REFERENCES "GROUPS" ("GROUP_ID") ON DELETE CASCADE ENABLE;
COMMENT ON COLUMN "ANNOTATIONS"."CONNECTED_ENTITY_ID" IS 'Group ID of entity used for creating annotation';