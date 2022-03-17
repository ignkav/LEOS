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
package eu.europa.ec.leos.cmis.search;

import eu.europa.ec.leos.cmis.mapping.CmisProperties;
import eu.europa.ec.leos.domain.cmis.LeosCategory;
import eu.europa.ec.leos.model.filter.QueryFilter;
import eu.europa.ec.leos.model.filter.QueryFilter.Filter;
import eu.europa.ec.leos.model.filter.QueryFilter.FilterType;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.stream.Collectors;

class QueryUtil {

    private static final Logger LOG = LoggerFactory.getLogger(QueryUtil.class);

    static String formFilterClause(QueryFilter workspaceFilter) {
        LOG.trace("Form where clause for filter...");
        StringBuilder whereClauseFilter = new StringBuilder();
        for (Filter filter : workspaceFilter.getFilters()) {
            if (whereClauseFilter.length() != 0) {
                whereClauseFilter.append(" AND ");
            }
            if(filter.nullCheck) {
                whereClauseFilter.append("(");
                whereClauseFilter.append(FilterType.getColumnName(filter.key) );
                whereClauseFilter.append(" IS NULL OR ");
            }
            StringBuilder value = new StringBuilder("'");
            value.append(StringUtils.join(filter.value, "', '"));
            value.append("'");

            String operation;
            if ("ANY".equals(FilterType.getColumnType(filter.key))) {
                if ("IN".equalsIgnoreCase(filter.operator)) {
                    operation = String.format("ANY %s IN (%s)",
                            FilterType.getColumnName(filter.key),
                            value);
                } else {
                    operation = String.format("%s %s ANY %s",
                            value,
                            filter.operator,
                            FilterType.getColumnName(filter.key)
                    );
                }
            } else {
                if ("IN".equalsIgnoreCase(filter.operator)) {
                    operation = String.format("%s IN (%s)",
                            FilterType.getColumnName(filter.key),
                            value);
                } else {
                    operation = String.format("%s %s %s",
                            FilterType.getColumnName(filter.key),
                            filter.operator,
                            value);
                }
            }
            whereClauseFilter.append(operation);
            if(filter.nullCheck) {
                whereClauseFilter.append(")");
            }
        }
        return whereClauseFilter.toString();
    }

    static String formSortClause(QueryFilter workspaceFilter) {
        return workspaceFilter.getSortOrders().stream()
                .map(sortOrder -> FilterType.getColumnName(sortOrder.key) + " " + sortOrder.direction)
                .collect(Collectors.joining(" ,"));
    }

    static String getQuery(Folder folder, Set<LeosCategory> categories, boolean descendants, QueryFilter workspaceFilter) {
        String categoryStr = categories.stream()
                .map(a -> "'" + a.name()+"'")
                .collect(Collectors.joining(","));

        StringBuilder whereClause = new StringBuilder(
                String.format("leos:category IN (%s) AND %s('%s')",
                        categoryStr,
                        descendants? "IN_TREE": "IN_FOLDER",
                        folder.getId()));

        String filterClause = QueryUtil.formFilterClause(workspaceFilter);
        if(!filterClause.isEmpty()){
            whereClause.append(" AND ").append(filterClause);
        }
        return whereClause.toString();
    }
    
    static String getMajorVersionQueryString(String docRef) {
       StringBuilder queryBuilder =  new StringBuilder(CmisProperties.METADATA_REF.getId()).append(" = '").append(docRef)
               .append("' ")
               .append(" AND cmis:isMajorVersion = true ")
               .append(" order by cmis:creationDate DESC");
       return queryBuilder.toString();
    }
    
    static String getVersionsWithoutVersionLabelQueryString(String docRef) {
        StringBuilder queryBuilder =  new StringBuilder(CmisProperties.METADATA_REF.getId()).append(" = '").append(docRef)
                .append("' ")
                .append(" AND ").append(CmisProperties.VERSION_LABEL.getId()).append(" IS NULL")
                .append(" order by cmis:creationDate DESC");
        return queryBuilder.toString();
    }

    private static List<String> parseMajorVersion(String majorVersionLabel) {
        // Split
        List<String> str = new LinkedList<>(Arrays.asList(majorVersionLabel.split("[.]")));

        // Check versionLabel format
        if (str.size() < 2) {
            throw new IllegalArgumentException("CMIS Version number should be in the format x...0");
        }
        if (!str.stream().allMatch(StringUtils::isNumeric)) {
            throw new IllegalArgumentException("CMIS Version number should be in the format x...0");
        }

        // Check if it is a major version (Should finish with ".0")
        if (!("0".equals(str.remove(str.size()-1)))) {
            throw new IllegalArgumentException("CMIS Version number should be in the format of a major version x...0");
        }
        return str;
    }

    private static String buildSearchVersionRegularExp(List<String> str, boolean allIntermediateVersions) {
        StringBuilder versionRegularExp = new StringBuilder();
        // Build regular expression x.% for all intermediate versions or x.0 for the first one
        versionRegularExp.append(String.join(".", str));
        if (allIntermediateVersions) {
            versionRegularExp.append(".%");
        }
        else {
            versionRegularExp.append(".0");
        }

        return versionRegularExp.toString();
    }

    // Build regular expression to search intermediate versions above one major version
    // Major version should respect format: x.y...z.0
    static String buildMinorVersionsGreaterThanMajorRegularExp(String majorVersionLabel, boolean allIntermediateVersions) {
        List<String> str = parseMajorVersion(majorVersionLabel);

        return buildSearchVersionRegularExp(str, allIntermediateVersions);
    }

    // Build regular expression to search intermediate versions below one major version
    // Major version should respect format: x.y...z.0
    static String buildMinorVersionsLowerThanMajorRegularExp(String majorVersionLabel, boolean allIntermediateVersions) {
        List<String> str = parseMajorVersion(majorVersionLabel);

        ListIterator<String> listIterator = str.listIterator(str.size());

        while (listIterator.hasPrevious()) {
            String lastDigit = listIterator.previous();
            if (!"0".equals(lastDigit)) {
                listIterator.set(String.valueOf(Integer.parseInt(lastDigit) - 1));
                return buildSearchVersionRegularExp(str, allIntermediateVersions);
            }
        }
        return "";
    }

    static QueryFilter getRecentVersionsQuery(String docRef, String versionLabel) {
        QueryFilter filter = new QueryFilter();
        Filter f1 = new Filter(FilterType.ref.name(), "=", false, docRef);
        filter.addFilter(f1);
        Filter f2 = new Filter(FilterType.cmisVersionLabel.name(), "LIKE", false, buildMinorVersionsGreaterThanMajorRegularExp(versionLabel, true));
        filter.addFilter(f2);
        Filter f3 = new Filter(FilterType.cmisVersionLabel.name(), "<>", false, buildMinorVersionsGreaterThanMajorRegularExp(versionLabel, false));
        filter.addFilter(f3);
        filter.addSortOrder(new QueryFilter.SortOrder(FilterType.creationDate.name(), QueryFilter.SORT_DESCENDING));
        return filter;
    }

    public static QueryFilter getMinorVersionsQueryFilter(String docRef, String currIntVersion) {
        QueryFilter filter = new QueryFilter();
        Filter f1 = new Filter(FilterType.ref.name(), "=", false, docRef);
        filter.addFilter(f1);
        Filter f2 = new Filter(FilterType.cmisVersionLabel.name(), "LIKE", false, buildMinorVersionsLowerThanMajorRegularExp(currIntVersion, true));
        filter.addFilter(f2);
        Filter f3 = new Filter(FilterType.cmisVersionLabel.name(), "<>", false, buildMinorVersionsLowerThanMajorRegularExp(currIntVersion, false));
        filter.addFilter(f3);
        filter.removeSortOrder(FilterType.creationDate.name());
        filter.addSortOrder(new QueryFilter.SortOrder(FilterType.creationDate.name(), QueryFilter.SORT_DESCENDING));
        return filter;
    }

    static QueryFilter getVersionsGreaterThanQuery(String docRef, String versionLabel, boolean sortDescending) {
        QueryFilter filter = new QueryFilter();
        Filter f1 = new QueryFilter.Filter(FilterType.ref.name(), "=", false, docRef);
        filter.addFilter(f1);
        Filter f2 = new QueryFilter.Filter(FilterType.cmisVersionLabel.name(), ">", false, versionLabel);
        filter.addFilter(f2);
        filter.addSortOrder(new QueryFilter.SortOrder(QueryFilter.FilterType.creationDate.name(), sortDescending ? QueryFilter.SORT_DESCENDING : QueryFilter.SORT_ASCENDING));
        return filter;
    }
}
