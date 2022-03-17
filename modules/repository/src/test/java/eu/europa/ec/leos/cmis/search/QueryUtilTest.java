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

import eu.europa.ec.leos.domain.cmis.common.VersionType;
import eu.europa.ec.leos.model.filter.QueryFilter;
import eu.europa.ec.leos.model.filter.QueryFilter.Filter;
import eu.europa.ec.leos.model.filter.QueryFilter.FilterType;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class QueryUtilTest {

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void createQueryMultiTest() {
        String expected = "metadata:docType = 'REGULATION' AND leos:language IN ('FR', 'NL')";
        QueryFilter createFilter = createMultiFilter();
        Assert.assertEquals(expected, QueryUtil.formFilterClause(createFilter));
    }

    @Test
    public void createQuerySingleTest() {
        String expected = "metadata:docType = 'REGULATION'";

        QueryFilter filter = new QueryFilter();
        Filter f1 = new Filter("docType", "=", false, "REGULATION");
        filter.addFilter(f1);

        Assert.assertEquals(expected, QueryUtil.formFilterClause(filter));
    }

    @Test
    public void createQueryINTest() {
        String expected = "leos:language IN ('FR', 'NL')";
        QueryFilter filter = new QueryFilter();
        Filter f2 = new Filter("language", "IN", false, "FR", "NL");
        filter.addFilter(f2);

        Assert.assertEquals(expected, QueryUtil.formFilterClause(filter));
    }

    @Test
    public void createQueryAnyInTest() {
        String expected = "ANY leos:collaborators IN ('jane::AUTHOR', 'jane::REVIEWER')";
        QueryFilter filter = new QueryFilter();
        Filter f2 = new Filter("role", "IN", false, "jane::AUTHOR", "jane::REVIEWER");
        filter.addFilter(f2);

        Assert.assertEquals(expected, QueryUtil.formFilterClause(filter));
    }

    @Test
    public void createQueryWithThreeConditionsTest() {
        String expected = "metadata:docType = 'REGULATION' AND leos:language IN ('FR', 'NL', 'EN') AND leos:category = 'PROPOSAL'";
        QueryFilter createFilter = createFilterWith3Conditions();
        Assert.assertEquals(expected, QueryUtil.formFilterClause(createFilter));
    }

    @Test
    public void createQueryWithNullCheckAndThreeConditionsTest() {
        String expected = "(metadata:docType IS NULL OR metadata:docType = 'REGULATION') AND (leos:language IS NULL OR leos:language IN ('FR', 'NL', 'EN')) AND (leos:category IS NULL OR leos:category = 'PROPOSAL')";
        QueryFilter createFilter = createFilterWithNullCheckAnd3Conditions();
        Assert.assertEquals(expected, QueryUtil.formFilterClause(createFilter));
    }

    private QueryFilter createMultiFilter() {
        QueryFilter filter = new QueryFilter();
        Filter f1 = new Filter("docType", "=", false, "REGULATION");
        filter.addFilter(f1);
        Filter f2 = new Filter("language", "IN", false, "FR", "NL");
        filter.addFilter(f2);
        return filter;
    }

    @Test
    public void createQueryForMinorVersionTest() {
        String expected = "metadata:ref = 'bill_test' AND leos:versionLabel LIKE '10.1.%' AND leos:versionLabel <> '10.1.0'";
        QueryFilter filter = createMinorVersionQueryFilter();
        Assert.assertEquals(expected, QueryUtil.formFilterClause(filter));
    }

    @Test
    public void createQueryForMinorVersionTest_NotMajorVersion() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("CMIS Version number should be in the format of a major version x...0");
        QueryUtil.getMinorVersionsQueryFilter("bill_test", "1.2");
    }

    @Test
    public void createQueryForMinorVersionTest_MalformedVersion() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("CMIS Version number should be in the format x...0");
        QueryUtil.getMinorVersionsQueryFilter("bill_test", "10");
    }

    @Test
    public void createQueryForMinorVersionTest_NonNumericVersion() {
        exceptionRule.expect(IllegalArgumentException.class);
        exceptionRule.expectMessage("CMIS Version number should be in the format x...0");
        QueryUtil.getMinorVersionsQueryFilter("bill_test", "1a0.a1");
    }

    @Test
    public void createQueryForMinorVersionTest_NoPreviousVersions() {
        String expected = "";
        Assert.assertEquals(expected, QueryUtil.buildMinorVersionsLowerThanMajorRegularExp("0.0.0", true));
    }

    @Test
    public void createQueryForMajorVersionTest() {
        String expected = "metadata:ref = 'bill_test' AND leos:versionType IN ('1', '2')";
        QueryFilter filter = createMajorVersionQueryFilter();
        String query = QueryUtil.formFilterClause(filter);
        Assert.assertEquals(expected, query);
    }
    
    @Test
    public void createQueryForRecentVersionTest() {
        String expected = "metadata:ref = 'bill_test' AND leos:versionLabel LIKE '11.2.%' AND leos:versionLabel <> '11.2.0'";
        QueryFilter filter = createRecentVersionsQueryFilter();
        String query = QueryUtil.formFilterClause(filter);
        Assert.assertEquals(expected, query);
    }
    
    @Test
    public void createQueryForRecentVersionCmisVersionTest() {
        String expected = "metadata:ref = 'bill_test' AND cmis:versionLabel LIKE '21.%' AND cmis:versionLabel <> '21.0'";
        QueryFilter filter = createRecentVersionsCmisVersionQueryFilter();
        String query = QueryUtil.formFilterClause(filter);
        Assert.assertEquals(expected, query);
    }

    private QueryFilter createFilterWith3Conditions() {
        QueryFilter filter = new QueryFilter();
        Filter f1 = new Filter("docType", "=", false, "REGULATION");
        filter.addFilter(f1);
        Filter f2 = new Filter("language", "IN", false, "FR", "NL", "EN");
        filter.addFilter(f2);
        Filter f3 = new Filter("category", "=", false, "PROPOSAL");
        filter.addFilter(f3);
        return filter;
    }

    private QueryFilter createFilterWithNullCheckAnd3Conditions() {
        QueryFilter filter = new QueryFilter();
        Filter f1 = new Filter("docType", "=", true, "REGULATION");
        filter.addFilter(f1);
        Filter f2 = new Filter("language", "IN", true, "FR", "NL", "EN");
        filter.addFilter(f2);
        Filter f3 = new Filter("category", "=", true, "PROPOSAL");
        filter.addFilter(f3);
        return filter;
    }

    private QueryFilter createMinorVersionQueryFilter() {
        QueryFilter filter = new QueryFilter();
        Filter f1 = new Filter("ref", "=", false, "bill_test");
        filter.addFilter(f1);
        Filter f2 = new Filter("versionLabel", "LIKE", false, QueryUtil.buildMinorVersionsLowerThanMajorRegularExp("10.2.0", true));
        filter.addFilter(f2);
        Filter f3 = new Filter("versionLabel", "<>", false, QueryUtil.buildMinorVersionsLowerThanMajorRegularExp("10.2.0", false));
        filter.addFilter(f3);
        return filter;
    }

    private QueryFilter createMajorVersionQueryFilter() {
        QueryFilter filter = new QueryFilter();
        Filter f1 = new Filter(FilterType.ref.name(), "=", false, "bill_test");
        filter.addFilter(f1);
        Filter f2 = new Filter(FilterType.versionType.name(), "IN", false, Integer.toString(VersionType.MAJOR.value()), Integer.toString(VersionType.INTERMEDIATE.value()));
        filter.addFilter(f2);
        return filter;
    }
    
    private QueryFilter createRecentVersionsCmisVersionQueryFilter() {
        QueryFilter filter = new QueryFilter();
        Filter f1 = new Filter(FilterType.ref.name(), "=", false, "bill_test");
        filter.addFilter(f1);
        Filter f2 = new Filter(FilterType.cmisVersionLabel.name(), "LIKE", false, QueryUtil.buildMinorVersionsGreaterThanMajorRegularExp("21.0", true));
        filter.addFilter(f2);
        Filter f3 = new Filter(FilterType.cmisVersionLabel.name(), "<>", false, QueryUtil.buildMinorVersionsGreaterThanMajorRegularExp("21.0", false));
        filter.addFilter(f3);
        return filter;
    }
    
    private QueryFilter createRecentVersionsQueryFilter() {
        QueryFilter filter = new QueryFilter();
        Filter f1 = new Filter(FilterType.ref.name(), "=", false, "bill_test");
        filter.addFilter(f1);
        Filter f2 = new Filter(FilterType.versionLabel.name(), "LIKE", false, QueryUtil.buildMinorVersionsGreaterThanMajorRegularExp("11.2.0", true));
        filter.addFilter(f2);
        Filter f3 = new Filter(FilterType.versionLabel.name(), "<>", false, QueryUtil.buildMinorVersionsGreaterThanMajorRegularExp("11.2.0", false));
        filter.addFilter(f3);
        return filter;
    }
}