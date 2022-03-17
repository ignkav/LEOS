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
package eu.europa.ec.leos.services.support;

import eu.europa.ec.leos.services.compare.ContentComparatorContext;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuthorialNoteProcessor {

    Integer originalLength = 0;
    /**
     * Get the list of authorial notes in a string
     * @param content
     * @return list of authorial notes
     */
    public List<AuthorialNote> getAuthorialNotes(String content)  {
        if (StringUtils.isEmpty(content) || !content.contains("</authorialNote>")) {
            return null;
        }
        List<AuthorialNote> authorialNotes = new ArrayList<>();
        Pattern tagPattern = Pattern.compile("<(\\S+?)(.*?)>(.*?)</\\1>");
        Matcher tagMatcher = tagPattern.matcher(content);

        /*
         *   m.group(0) => tag only
         *   m.group(1) => tag name
         *   m.group(2) => tag attributes
         *   m.group(3) => tag content
         */
        while (tagMatcher.find()) {
            String tagName = tagMatcher.group(1);
            if(tagName.equals("authorialNote")) {
                String tagAttributes = tagMatcher.group(2);
                Pattern xmlIdPattern = Pattern.compile("[\\s\\S]*?xml:id=\"(?<gXmlId>[\\s\\S]*?)\"[\\s\\S]*?", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
                Matcher xmlIdMatcher = xmlIdPattern.matcher(tagAttributes);
                xmlIdMatcher.find();
                String xmlId = xmlIdMatcher.group("gXmlId");

                Pattern markerPattern = Pattern.compile("[\\s\\S]*?marker=\"(?<gMarker>[\\s\\S]*?)\"[\\s\\S]*?", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
                Matcher markerMatcher = markerPattern.matcher(tagAttributes);
                markerMatcher.find();
                Integer marker = Integer.valueOf(markerMatcher.group("gMarker"));

                AuthorialNote authorialNote = new AuthorialNote(xmlId, marker, tagMatcher.start(), tagMatcher.group());
                authorialNotes.add(authorialNote);
            }
        }

        return authorialNotes;
    }

    /**
     * Delete in content only authorial notes in parameters
     * @param content
     * @param authorialNotes
     * @return content without authorial notes
     */
    public String deleteExistingAuthorialNotes(String content, List<AuthorialNote> authorialNotes ,List<AuthorialNote> authorialNotesCompared, boolean currentContent)  {
        if (StringUtils.isEmpty(content) || !content.contains("</authorialNote>") || authorialNotes == null) {
            return content;
        }

        for(AuthorialNote authorialNote:authorialNotes) {
            Integer marker = authorialNote.getMarker();
            AuthorialNote note = getAuthorialNoteById(authorialNotesCompared,authorialNote.getXmlId(), authorialNote.getMarker());
            if(note != null && !note.getMarker().equals(marker)) {
                if(currentContent) {
                    String originalMarker = "(" + note.getMarker() + ")";
                    content = content.replace("marker=\"" + marker + "\"",
                            "marker=\"" + originalMarker + "" +
                                    marker + "\"");
                } else {
                    String originalMarker = "(" + marker + ")";
                    content = content.replace("marker=\"" + marker + "\"",
                            "marker=\"" + originalMarker + "" +
                                    note.getMarker() + "\"");
                }
            }
        }

        return content;
    }

    /**
     * Check if character after footnote is alphanumeric
     * If yes, add space between footnote and next word
     * @param content
     * @return content with additional space if needed
     */
    public String processCharacterAfterAuthorialNotes(String content)  {
        if (StringUtils.isEmpty(content) || !content.contains("</authorialNote>")) {
            return content;
        }
        Pattern tagPattern = Pattern.compile("<(\\S+?)(.*?)>(.*?)</\\1>");
        Matcher tagMatcher = tagPattern.matcher(content);

        /*
         *   m.group(0) => tag only
         *   m.group(1) => tag name
         *   m.group(2) => tag attributes
         *   m.group(3) => tag content
         */
        while (tagMatcher.find()) {
            String tagName = tagMatcher.group(1);
            if(tagName.equals("authorialNote")) {

                String tagOnly = tagMatcher.group(0);
                Integer tagPosition = content.lastIndexOf(tagOnly) + tagOnly.length();
                if(tagPosition < content.length()) {
                    String lastCharacter = content.substring(tagPosition, tagPosition + 1);

                    if (isAlphaNumeric(lastCharacter)) {
                        content = content.replace(tagOnly + lastCharacter, tagOnly + " " + lastCharacter);
                        //Need to one to length because of the space added
                        originalLength++;
                    }
                }
            }
        }

        return content;
    }

    /**
     * Delete the authorial notes from the content and store in a string
     * @param content
     * @return content without the authorial notes
     */
    public String deleteAllAuthorialNotes(String content) {
        return content.replaceAll("<authorialNote[\\s\\S]*?>[\\s\\S]*?</authorialNote>", "");
    }

    /**
     * Get Authorialnote by id in List
     * @param authorialNotes
     * @param id
     * @return authorialNote selected by id
     */
    public AuthorialNote getAuthorialNoteById(List<AuthorialNote> authorialNotes, String id, Integer marker) {
        return authorialNotes.stream().filter(authorialNote -> id.equals(authorialNote.getXmlId())).filter(authorialNote -> !marker.equals(authorialNote.getMarker())).findAny().orElse(null);
    }

    /**
     * Get Length of removed span added by the diffing library
     * Useful when content contains footnotes deleted in original
     * @param content
     * @param start
     * @param context
     * @return Length of removed span
     */
    public Integer getRemovedSpanDiffingLength(String content, Integer start, ContentComparatorContext context) {

        Pattern patternSpans = Pattern.compile("<span[^>]*>[\\s\\S]+<\\/span>");

        content = content.replaceAll("</span>", "</span><!DELIMITER>");
        String[] spans = content.split("<!DELIMITER>");

        Integer lengthRemovedSpan = 0;
        Integer lengthSpan = 0;
        for(String span:spans) {
            Matcher matcherSpans = patternSpans.matcher(span);
            while(matcherSpans.find()) {
                Integer currentLength = matcherSpans.group(0).length();
                if((lengthSpan + matcherSpans.start()) <= start && matcherSpans.group(0).contains(context.getRemovedValue())) {
                    start += currentLength;
                    lengthRemovedSpan += currentLength;
                    if(isEmptyCharacterBefore(span, matcherSpans.start())) {
                            lengthRemovedSpan++;
                    }
                } else {
                    start += currentLength;
                }
            }
            lengthSpan += span.length();
        }
        return lengthRemovedSpan;
    }

    /**
     * Get Length of removed span added by the diffing library
     * Useful when content contains footnotes added in current not present in original
     * @param content
     * @param start
     * @return Length of added span
     */
    public Integer getAddedSpanDiffingLength(String content, Integer start, ContentComparatorContext context)  {
        if (StringUtils.isEmpty(content) || !content.contains("</span>")) {
            return 0;
        }

        Pattern tagPattern = Pattern.compile("<(\\S+?)(.*?)>(.*?)</\\1>");
        Matcher tagMatcher = tagPattern.matcher(content);
        Integer lengthSpan = 0;
        /*
         *   m.group(0) => tag only
         *   m.group(1) => tag name
         *   m.group(2) => tag attributes
         *   m.group(3) => tag content
         */
        while (tagMatcher.find()) {
            String tagName = tagMatcher.group(1);
            if(tagName.equals("span")) {
                String tagOnly = tagMatcher.group(0);
                String tagContent =  tagMatcher.group(3);
                Integer regionStartTag = tagMatcher.start();
                if(regionStartTag < start && tagOnly.contains(context.getAddedValue())) {
                    lengthSpan += tagOnly.length() - tagContent.length();
                    if(tagContent.contains("</authorialNote>")) {
                        lengthSpan++;
                    }
                }
            }
        }

        return lengthSpan;
    }

    /**
     * Check if string is alphanumeric
     * @param content
     * @return true/false
     */
    public static boolean isAlphaNumeric(String content) {
        return content != null &&
                content.chars().allMatch(Character::isLetterOrDigit);
    }

    /**
     * Add substring in string at exact position
     * Useful when postprocess authorial notes
     * @param source
     * @param append
     * @param position
     * @return string with substring
     */
    public String addStringAtPosition(String source, String append, Integer position) {
        if(isEmptyCharacterBefore(source, position)) {
                position = position - 1;
        }
        return source.substring(0, position) + append + source.substring(position);
    }

    /**
     * Check if character before is empty
     * @param content
     * @param position
     * @return true/false
     */
    public boolean isEmptyCharacterBefore(String content, Integer position) {
        if (position > 0) {
            String before = content.substring(position - 1, position);
            if(before.trim().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * AuthorialNote object
     */
    public class AuthorialNote {

        public String xmlId;
        public Integer marker;
        public Integer regionStart;
        public String note;

        public AuthorialNote(String xmlId, Integer marker, Integer regionStart, String note) {
            this.xmlId = xmlId;
            this.marker = marker;
            this.regionStart = regionStart;
            this.note = note;
        }

        public String getXmlId() {
            return xmlId;
        }

        public void setXmlId(String xmlId) {
            this.xmlId = xmlId;
        }

        public Integer getMarker() {
            return marker;
        }

        public void setMarker(Integer marker) {
            this.marker = marker;
        }

        public Integer getRegionStart() {
            return regionStart;
        }

        public void setRegionStart(Integer regionStart) {
            this.regionStart = regionStart;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }

        @Override
        public boolean equals(Object rightNote) {
            if (this == rightNote) return true;
            if (rightNote == null || getClass() != rightNote.getClass()) return false;
            AuthorialNote leftNote = (AuthorialNote) rightNote;
            return marker.equals(leftNote.marker) &&
                    note.equals(leftNote.note);
        }
    }
}
