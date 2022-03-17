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
package eu.europa.ec.leos.services.support.xml.domain;

import java.util.List;

import static eu.europa.ec.leos.util.LeosDomainUtil.addFieldIfNotNull;
import static eu.europa.ec.leos.util.LeosDomainUtil.calculateLeftPadd;

public final class Element {

    private final Object navigationIndex;
    private final int nodeIndex;
    private final boolean hasTextChild;
    private String tagIdentifier;
    private String tagContent;
    private String tagName;
    private String fullContent;//only for text matching purpose
    private Element parent;
    private List<Element> children;

    public Element(Object navigationIndex, String tagId, String tagContent, int nodeIndex, boolean hasTextChild, List<Element> children) {
        this.navigationIndex = navigationIndex;
        this.nodeIndex = nodeIndex;
        this.hasTextChild = hasTextChild;
        this.tagContent = tagContent;
        this.tagIdentifier = tagId != null ? tagId.replaceAll(" ", "") : null;
        this.children = children;
        this.children.forEach(child -> child.parent = this);
//		if(hasTextChild) {
//			this.tagIdentifier += "_" + nodeIndex;
//		}
    }

    public Element(Object navigationIndex, String tagId, String tagName, String tagContent, int nodeIndex, boolean hasTextChild, String fullContent, List<Element> children) {
        this(navigationIndex, tagId, tagContent, nodeIndex, hasTextChild, children);
        this.tagName = tagName;
        this.fullContent = fullContent.replaceAll("<.*?>|\\r\\n", "");
    }

    public String getTagName() {
        return tagName;
    }

    public int getApproxLength() {
        return fullContent.length();
    }

    public String getTagId() {
        return tagIdentifier;
    }

    public Object getNavigationIndex() {
        return navigationIndex;
    }

    public int getNodeIndex() {
        return nodeIndex;
    }

    public String getTagContent() {
        return tagContent;
    }

    public boolean hasTextChild() {
        return hasTextChild;
    }

    public Element getParent() {
        return parent;
    }

    public List<Element> getChildren() {
        return children;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Element element = (Element) o;

        if (tagIdentifier != null && element.tagIdentifier != null
                && tagIdentifier.equals(element.tagIdentifier))
            return true;

        return false;
    }

    @Override
    public int hashCode() {
        return tagIdentifier != null ? tagIdentifier.hashCode() : tagName.hashCode();
    }

    @Override
    public String toString() {
        return printTocAsTree(this, 0);
    }

    public String printTocAsTree(Element item, int deep) {
        // change RIGHT_CHAR=", " and LEFT_CHAR="", in case you want all in one line. Or overload the method.
        final String RIGHT_CHAR = "\n";
        final String LEFT_CHAR = "\t";

        String LEFT_PAD_CLASSNAME = calculateLeftPadd(deep, LEFT_CHAR);
        String LEFT_PAD = calculateLeftPadd(deep + 1, LEFT_CHAR);

        final StringBuilder sb = new StringBuilder(RIGHT_CHAR);
        sb.append(LEFT_PAD_CLASSNAME).append("Element[").append(RIGHT_CHAR);
        addFieldIfNotNull("tagName", item.tagName, LEFT_PAD, RIGHT_CHAR, sb);
        addFieldIfNotNull("tagIdentifier", item.tagIdentifier, LEFT_PAD, RIGHT_CHAR, sb);
        addFieldIfNotNull("navigationIndex", item.navigationIndex, LEFT_PAD, RIGHT_CHAR, sb);
        addFieldIfNotNull("nodeIndex", item.nodeIndex, LEFT_PAD, RIGHT_CHAR, sb);
        addFieldIfNotNull("hasTextChild", item.hasTextChild, LEFT_PAD, RIGHT_CHAR, sb);
//        addFieldIfNotNull("tagContent", item.tagContent, LEFT_PAD, RIGHT_CHAR, sb);
//        addFieldIfNotNull("fullContent", item.fullContent, LEFT_PAD, RIGHT_CHAR, sb);
        sb.append(LEFT_PAD + "parentItem=").append(getParentString(item.parent)).append(RIGHT_CHAR);
//        addFieldIfNotNull("children", item.children, LEFT_PAD, RIGHT_CHAR, sb);

        if (item.children.size() > 0) {
            final StringBuilder sbChildren = new StringBuilder();
            for (Element child : item.children) {
                sbChildren.append(printTocAsTree(child, deep + 2));
            }
            sb.append(LEFT_PAD).append("children=[").append(sbChildren).append(RIGHT_CHAR);
        }

        sb.append(LEFT_PAD_CLASSNAME).append("]");
        return sb.toString();
    }

    private String getParentString(Element parent) {
        StringBuilder sb = new StringBuilder();
        if (parent != null) {
            sb.append("Element[");
            sb.append("tagName=").append(parent.tagName);
            sb.append(", tagIdentifier=").append(parent.tagIdentifier);
            sb.append("]");
        }
        return sb.toString();
    }

    //used from http://stackoverflow.com/questions/955110/similarity-string-comparison-in-java
    public double contentSimilarity(Element element) {
        String s1 = this.fullContent;
        String s2 = element.fullContent;

        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2;
            shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) {
            return 1.0; /* both strings are zero length */
        }
	    /* // If you have StringUtils, you can use it to calculate the edit distance:
	      return (longerLength - StringUtils.getLevenshteinDistance(longer, shorter)) /
	                                 (double) longerLength; */
        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;

    }

    // Example implementation of the Levenshtein Edit Distance
    // See http://rosettacode.org/wiki/Levenshtein_distance#Java
    private int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }
}
