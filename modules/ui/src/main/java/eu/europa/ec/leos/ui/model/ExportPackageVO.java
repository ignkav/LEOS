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
package eu.europa.ec.leos.ui.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ExportPackageVO {

    private final String id;
    private final String versionId;
    private final String versionLabel;
    private List<String> comments;
    private final Date date;
    private final String status;

    private static final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public ExportPackageVO(String id, String versionId, String versionLabel, List<String> comments, Date date, String status) {
        this.id = id;
        this.versionId = versionId;
        this.versionLabel = versionLabel;
        this.comments = comments;
        this.date = date;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getVersionId() {
        return versionId;
    }

    public String getVersionLabel() {
        return versionLabel;
    }

    public List<String> getComments() {
        return comments;
    }

    public String getMainComment() {
        return comments.get(0);
    }

    public void setMainComment(String mainComment) {
        comments.set(0, mainComment);
    }

    public String getComment(int numLine) {
        return comments.size() > numLine ? comments.get(numLine) : "";
    }

    public Date getDate() {
        return date;
    }

    public String getDateFormatted() {
        return dateFormat.format(date);
    }

    public String getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ExportPackageVO that = (ExportPackageVO) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(versionId, that.versionId) &&
                Objects.equals(versionLabel, that.versionLabel) &&
                Objects.equals(comments, that.comments) &&
                Objects.equals(date, that.date) &&
                Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, versionId, versionLabel, comments, date, status);
    }

    @Override
    public String toString() {
        return "ExportPackageVO{" +
                "id='" + id + '\'' +
                ", versionId='" + versionId + '\'' +
                ", versionLabel='" + versionLabel + '\'' +
                ", comments='" + comments + '\'' +
                ", date='" + date + '\'' +
                ", status='" + status + '\'' +
                '}';
    }

    public static int compareVersions(String version1, String version2) {
        String[] levels1 = version1.split("\\.");
        String[] levels2 = version2.split("\\.");        
        int length = Math.max(levels1.length, levels2.length);
        for (int i = 0; i < length; i++) {
            Integer v1 = i < levels1.length ? Integer.parseInt(levels1[i]) : 0;
            Integer v2 = i < levels2.length ? Integer.parseInt(levels2[i]) : 0;
            int compare = v1.compareTo(v2);
            if (compare != 0) {
                return compare;
            }
        }
        return 0;
    }
}