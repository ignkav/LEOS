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
package eu.europa.ec.leos.annotate.model.search;

import eu.europa.ec.leos.annotate.Generated;
import eu.europa.ec.leos.annotate.model.AnnotationComparator;
import eu.europa.ec.leos.annotate.model.SimpleMetadataWithStatuses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * class representing the options available for searching for annotations
 */
public class AnnotationSearchOptions extends AbstractAnnotationSearchOptions {

    private static final Logger LOG = LoggerFactory.getLogger(AnnotationSearchOptions.class);

    // -------------------------------------
    // Available properties
    // -------------------------------------

    /**
     * should replies be merged into the top-level annotations, or be listed separately
     */
    private boolean separateReplies;

    /**
     * number of items and start index to be returned
     */
    private int itemLimit = Consts.DEFAULT_SEARCH_LIMIT, itemOffset = Consts.DEFAULT_SEARCH_OFFSET;

    /**
     * sorting order
     */
    private Direction order = Direction.DESC;
    /**
     * column to be sorted
     */
    private String sortColumn;

    /**
     * list of sets of metadata with statuses requested
     */
    @SuppressWarnings("PMD.LongVariable")
    private List<SimpleMetadataWithStatuses> metadataMapsWithStatusesList;

    /**
     * information which type of user executes the search
     */
    private Consts.SearchUserType searchUser;
    
    // -------------------------------------
    // Constructors
    // -------------------------------------

    /**
     * constructor with mandatory search parameters
     */
    public AnnotationSearchOptions(final String uri, final String group, final boolean separateReplies, final int limit, final int offset, final String order,
            final String sort) {

        super(group, uri);

        this.separateReplies = separateReplies;

        setItemLimitAndOffset(limit, offset);

        setSortColumnIntern(sort);
        this.order = Direction.valueOf(order.toUpperCase(Locale.ENGLISH));
        this.searchUser = Consts.SearchUserType.Unknown;
    }

    // -------------------------------------
    // Getters & setters
    // -------------------------------------

    @Generated
    public boolean isSeparateReplies() {
        return separateReplies;
    }

    @Generated
    public void setSeparateReplies(final boolean separateReplies) {
        this.separateReplies = separateReplies;
    }

    @Generated
    public int getItemLimit() {
        return itemLimit;
    }

    @Generated
    public void setItemLimit(final int itemLimit) {
        this.itemLimit = itemLimit;
    }

    /**
     * Set limit and offset at once, as they are related (offset depends on limit)
     */
    public final void setItemLimitAndOffset(final int limit, final int offset) {

        if (limit < 0) {
            // negative value: we want to return all items - do this by specifying biggest possible value...
            this.itemLimit = Integer.MAX_VALUE;

            // ... and setting offset to 0 (thus avoid receiving only a page)
            this.itemOffset = 0;

        } else if (limit == 0) {

            // zero: use default values
            this.itemLimit = Consts.DEFAULT_SEARCH_LIMIT;
            this.itemOffset = (offset < 0 ? Consts.DEFAULT_SEARCH_OFFSET : offset);

        } else {
            // any other positive value is accepted - no upper limit!
            this.itemLimit = limit;

            // take over the given offset; set it positive, if need be
            this.itemOffset = (offset < 0 ? Consts.DEFAULT_SEARCH_OFFSET : offset);
        }
    }

    @Generated
    public int getItemOffset() {
        return itemOffset;
    }

    @Generated
    public void setItemOffset(final int itemOffset) {
        this.itemOffset = itemOffset;
    }

    @Generated
    public Direction getOrder() {
        return order;
    }

    @Generated
    public void setOrder(final Direction order) {
        this.order = order;
    }

    @Generated
    public String getSortColumn() {
        return sortColumn;
    }

    public void setSortColumn(final String sortColumn) {

        setSortColumnIntern(sortColumn);
    }

    // moved to separate function to avoid being overridden (PMD notification)
    private void setSortColumnIntern(final String sortColumn) {

        // check that given sort criteria is valid; ignore if invalid
        if (!StringUtils.isEmpty(sortColumn) && !AnnotationComparator.SORTABLE_COLUMN_NAMES.contains(sortColumn)) {
            LOG.error("Given sorting column '" + sortColumn + "' cannot be used for sorting; no sorting will be applied");
            this.sortColumn = "";
            return;
        }
        this.sortColumn = sortColumn;
    }

    @Generated
    public List<SimpleMetadataWithStatuses> getMetadataMapsWithStatusesList() {
        return metadataMapsWithStatusesList;
    }

    @Generated
    public void setMetadataMapsWithStatusesList(final List<SimpleMetadataWithStatuses> metaList) {
        this.metadataMapsWithStatusesList = metaList;
        
        // TEMPORARY WORKAROUNG FOR ANOT-134: discard the "version" parameter
        if (this.metadataMapsWithStatusesList != null) {
            for(final SimpleMetadataWithStatuses smws : metaList) {
                if(smws.getMetadata() != null && smws.getMetadata().containsKey("version")) {
                    smws.getMetadata().remove("version");
                }
            }
        }
    }

    @Generated
    public Consts.SearchUserType getSearchUser() {
        return searchUser;
    }

    @Generated
    public void setSearchUser(final Consts.SearchUserType sUser) {
        this.searchUser = sUser;
    }

    // -------------------------------------
    // equals and hashCode
    // -------------------------------------

    @Generated
    @Override
    public int hashCode() {
        return super.hashCode() ^
                Objects.hash(separateReplies, itemLimit, itemOffset, order, sortColumn, searchUser, metadataMapsWithStatusesList);
    }

    @Generated
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final AnnotationSearchOptions other = (AnnotationSearchOptions) obj;
        return super.equals(obj) &&
                Objects.equals(this.separateReplies, other.separateReplies) &&
                Objects.equals(this.itemLimit, other.itemLimit) &&
                Objects.equals(this.itemOffset, other.itemOffset) &&
                Objects.equals(this.order, other.order) &&
                Objects.equals(this.sortColumn, other.sortColumn) &&
                Objects.equals(this.searchUser, other.searchUser) &&
                Objects.equals(this.metadataMapsWithStatusesList, other.metadataMapsWithStatusesList);
    }
}
