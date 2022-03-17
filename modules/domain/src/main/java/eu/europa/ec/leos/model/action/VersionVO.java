/*
 * Copyright 2021 European Commission
 *
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *     https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */
package eu.europa.ec.leos.model.action;

import eu.europa.ec.leos.domain.cmis.common.VersionType;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VersionVO {
    
    private VersionType versionType;
    private String documentId;
    private String cmisVersionNumber;
    private VersionNumber versionNumber;
    private String updatedDate;
    private String username;
    private String versionedReference;
    private List<VersionVO> subVersions = new ArrayList<>();
    private CheckinCommentVO checkinCommentVO;
    private boolean mostRecentVersion;
    
    private final static SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public VersionType getVersionType() {
        return versionType;
    }
    
    public void setVersionType(VersionType versionType) {
        this.versionType = versionType;
    }
    
    public void setCmisVersionNumber(String cmisVersionNumber) {
        this.cmisVersionNumber = cmisVersionNumber;
    }
    
    public String getCmisVersionNumber() {
        return cmisVersionNumber;
    }
    
    public VersionNumber getVersionNumber() {
        return versionNumber;
    }
    
    public void setVersionNumber(VersionNumber versionNumber) {
        this.versionNumber = versionNumber;
    }
    
    public String getUpdatedDate() {
        return updatedDate;
    }
    
    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }
    
    public void setUpdatedDate(Instant updatedDate) {
        this.updatedDate = dateFormatter.format(Date.from(updatedDate));
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }

    public String getVersionedReference() {
        return versionedReference;
    }

    public void setVersionedReference(String versionedReference) {
        this.versionedReference = versionedReference;
    }

    public List<VersionVO> getSubVersions() {
        return subVersions;
    }
    
    public void setSubVersions(List<VersionVO> subVersions) {
        this.subVersions = subVersions;
    }
    
    public CheckinCommentVO getCheckinCommentVO() {
        return checkinCommentVO;
    }
    
    public void setCheckinCommentVO(CheckinCommentVO checkinCommentVO) {
        this.checkinCommentVO = checkinCommentVO;
    }
    
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
    
    public String getDocumentId() {
        return documentId;
    }
    
    public boolean isMostRecentVersion() {
        return mostRecentVersion;
    }

    public void setMostRecentVersion(boolean mostRecentVersion) {
        this.mostRecentVersion = mostRecentVersion;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(versionType, documentId, cmisVersionNumber, versionNumber, updatedDate, username);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        
        final VersionVO other = (VersionVO) obj;
        return Objects.equals(this.versionType, other.versionType) &&
                Objects.equals(this.documentId, other.documentId) &&
                Objects.equals(this.cmisVersionNumber, other.cmisVersionNumber) &&
                Objects.equals(this.versionNumber, other.versionNumber) &&
                Objects.equals(this.updatedDate, other.updatedDate) &&
                Objects.equals(this.username, other.username);
    }
    
    @Override
    public String toString() {
        return "[versionType: " + versionType
                + ", documentId: " + documentId
                + ", cmisVersionNumber: " + cmisVersionNumber
                + ", versionNumber: " + versionNumber
                + ", updatedDate: " + updatedDate
                + ", username: " + username
                + ", subVersions: " + subVersions
                + ", checkinCommentVO: " + checkinCommentVO
                + "]" + "\n";
    }

    public static class VersionNumber implements Comparable<VersionNumber> {
        private final int[] versions;
        public VersionNumber(String versionNumber) {
            versions = Stream.of(versionNumber.split("[.]")).mapToInt(Integer::parseInt).toArray();
        }

        @Override public String toString() {
            return Arrays.stream(versions).mapToObj(String::valueOf).collect(Collectors.joining("."));
        }

        private int getPosition(int position) {
            if (versions.length > position) {
                return versions[position];
            } else {
                return 0;
            }
        }

        public int getMajor() {
            return getPosition(0);
        }

        public int getIntermediate() {
            return getPosition(1);
        }

        public int getMinor() {
            return getPosition(2);
        }

        @Override public int compareTo(VersionNumber versionNumber) {
            for (int i = 0; i < Math.min(versions.length, versionNumber.versions.length); i++) {
                int res = Integer.compare(versions[i], versionNumber.versions[i]);
                if (res != 0) {
                    return Integer.signum(res);
                }
            }
            return Integer.signum(Integer.compare(versions.length, versionNumber.versions.length));
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            VersionNumber that = (VersionNumber) o;
            return Arrays.equals(versions, that.versions);
        }

        @Override public int hashCode() {
            return Arrays.hashCode(versions);
        }
    }
}
