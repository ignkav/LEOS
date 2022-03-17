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
package integration.saveToc;

import eu.europa.ec.leos.model.action.SoftActionType;
import eu.europa.ec.leos.vo.toc.AknTag;
import eu.europa.ec.leos.vo.toc.NumberingType;
import eu.europa.ec.leos.vo.toc.OptionsType;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;
import eu.europa.ec.leos.vo.toc.TocItemBuilder;
import eu.europa.ec.leos.vo.toc.TocItemVOBuilder;
import org.w3c.dom.Node;

import java.util.List;
import java.util.stream.Collectors;

import static eu.europa.ec.leos.services.support.xml.XmlHelper.EC;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.SOFT_MOVE_PLACEHOLDER_ID_PREFIX;

public class TocVOCreateUtils {

    public static TableOfContentItemVO createPart(String id, int itemDepth, String instance) {
        TocItemVOBuilder builder = TocItemVOBuilder.getBuilder()
                .withId(id)
                .withTocItem(TocItemBuilder.getBuilder()
                        .withAknTag(AknTag.PART)
                        .withTemplate("<part xml:id=\"${id}\"><num>${num}</num><heading>${heading}</heading></part>")
                        .withItemNumber(OptionsType.MANDATORY)
                        .withItemHeading(OptionsType.MANDATORY)
                        .build()
                )
                .withOriginAttr(instance)
                .withNumber("#")
                .withOriginNumAttr(instance)
                .withHeading("Part heading...")
                .withSoftActionAttr(SoftActionType.ADD)
                .withIsSoftActionRoot(true)
                .withItemDepth(itemDepth);
        return builder.build();
    }

    public static TableOfContentItemVO createTitle(String id, int itemDepth, String instance) {
        TocItemVOBuilder builder = TocItemVOBuilder.getBuilder()
                .withId(id)
                .withTocItem(TocItemBuilder.getBuilder()
                        .withAknTag(AknTag.TITLE)
                        .withTemplate("<title xml:id=\"${id}\"><num>${num}</num><heading>${heading}</heading></title>")
                        .withItemNumber(OptionsType.MANDATORY)
                        .withItemHeading(OptionsType.MANDATORY)
                        .build()
                )
                .withOriginAttr(instance)
                .withNumber("#")
                .withOriginNumAttr(instance)
                .withHeading("Title heading...")
                .withSoftActionAttr(SoftActionType.ADD)
                .withIsSoftActionRoot(true)
                .withItemDepth(itemDepth);
        return builder.build();
    }

    public static TableOfContentItemVO createChapter(String id, int itemDepth, String instance) {
        TocItemVOBuilder builder = TocItemVOBuilder.getBuilder()
                .withId(id)
                .withTocItem(TocItemBuilder.getBuilder()
                        .withAknTag(AknTag.CHAPTER)
                        .withTemplate("<chapter xml:id=\"${id}\"><num>${num}</num><heading>${heading}</heading></chapter>")
                        .withItemNumber(OptionsType.MANDATORY)
                        .withItemHeading(OptionsType.MANDATORY)
                        .build()
                )
                .withOriginAttr(instance)
                .withNumber("#")
                .withOriginNumAttr(instance)
                .withHeading("Chapter heading...")
                .withSoftActionAttr(SoftActionType.ADD)
                .withIsSoftActionRoot(true)
                .withItemDepth(itemDepth);
        return builder.build();
    }

    public static TableOfContentItemVO createSection(String id, int itemDepth, String instance) {
        TocItemVOBuilder builder = TocItemVOBuilder.getBuilder()
                .withId(id)
                .withTocItem(TocItemBuilder.getBuilder()
                        .withAknTag(AknTag.SECTION)
                        .withTemplate("<section xml:id=\"${id}\"><num>${num}</num><heading>${heading}</heading></section>")
                        .withItemNumber(OptionsType.MANDATORY)
                        .withItemHeading(OptionsType.MANDATORY)
                        .build()
                )
                .withOriginAttr(instance)
                .withNumber("#")
                .withOriginNumAttr(instance)
                .withHeading("Section heading...")
                .withSoftActionAttr(SoftActionType.ADD)
                .withIsSoftActionRoot(true)
                .withItemDepth(itemDepth);
        return builder.build();
    }

    public static TableOfContentItemVO createArticle(String id, int itemDepth, String instance) {
        TocItemVOBuilder builder = TocItemVOBuilder.getBuilder()
                .withId(id)
                .withTocItem(TocItemBuilder.getBuilder()
                        .withAknTag(AknTag.ARTICLE)
                        .withTemplate("<article xml:id=\"${id}\"><num leos:editable=\"false\">${num}</num><heading>${heading}</heading><paragraph xml:id=\"${id}-par1\"><num>1.</num><content><p>${default.content.text}</p></content></paragraph></article>")
                        .withItemNumber(OptionsType.MANDATORY)
                        .withItemHeading(OptionsType.OPTIONAL)
                        .build()
                )
                .withOriginAttr(instance)
                .withNumber("#")
                .withOriginNumAttr(instance)
                .withHeading("Article heading...")
                .withSoftActionAttr(SoftActionType.ADD)
                .withIsSoftActionRoot(true)
                .withItemDepth(itemDepth)
                .withAffected(true);
        return builder.build();
    }

    public static TableOfContentItemVO createParagraph(String id, int itemDepth, String instance) {
        TocItemVOBuilder builder = TocItemVOBuilder.getBuilder()
                .withId(id)
                .withTocItem(TocItemBuilder.getBuilder()
                        .withAknTag(AknTag.PARAGRAPH)
                        .withItemNumber(OptionsType.OPTIONAL)
                        .withItemHeading(OptionsType.NONE)
                        .build()
                )
                .withOriginAttr(instance)
                .withNumber("#")
                .withOriginNumAttr(instance)
                .withSoftActionAttr(SoftActionType.ADD)
                .withIsSoftActionRoot(true)
                .withItemDepth(itemDepth)
                .withAffected(true);
        return builder.build();
    }

    public static TableOfContentItemVO createSubParagraph(String id, int itemDepth, String instance) {
        TocItemVOBuilder builder = TocItemVOBuilder.getBuilder()
                .withId(id)
                .withTocItem(TocItemBuilder.getBuilder()
                        .withAknTag(AknTag.SUBPARAGRAPH)
                        .withItemNumber(OptionsType.NONE)
                        .withItemHeading(OptionsType.NONE)
                        .build()
                )
                .withOriginAttr(instance)
                .withOriginNumAttr(instance)
                .withSoftActionAttr(SoftActionType.ADD)
                .withIsSoftActionRoot(true)
                .withItemDepth(itemDepth);
        return builder.build();
    }

    public static TableOfContentItemVO createPoint(String id, int itemDepth, String instance) {
        TocItemVOBuilder builder = TocItemVOBuilder.getBuilder()
                .withId(id)
                .withTocItem(TocItemBuilder.getBuilder()
                        .withAknTag(AknTag.POINT)
                        .withItemNumber(OptionsType.MANDATORY)
                        .withItemHeading(OptionsType.NONE)
                        .build()
                )
                .withOriginAttr(instance)
                .withNumber("#")
                .withOriginNumAttr(instance)
                .withSoftActionAttr(SoftActionType.ADD)
                .withIsSoftActionRoot(true)
                .withItemDepth(itemDepth);
        return builder.build();
    }

    public static TableOfContentItemVO createLevel(String id, int itemDepth, String instance) {
        TocItemVOBuilder builder = TocItemVOBuilder.getBuilder()
                .withId(id)
                .withTocItem(TocItemBuilder.getBuilder()
                        .withAknTag(AknTag.LEVEL)
                        .withItemNumber(OptionsType.MANDATORY)
                        .withAutoNumbering(true)
                        .withItemHeading(OptionsType.OPTIONAL)
                        .withMaxDepth("7")
                        .withNumberingType(NumberingType.ARABIC_POSTFIX_DEPTH)
                        .build()
                )
                .withOriginAttr(instance)
                .withNumber("#")
                .withOriginNumAttr(instance)
                .withSoftActionAttr(SoftActionType.ADD)
                .withIsSoftActionRoot(true)
                .withItemDepth(itemDepth);
        return builder.build();
    }

    public static TableOfContentItemVO createMoveToElement(TableOfContentItemVO originalElement) {
        TocItemVOBuilder builder = TocItemVOBuilder.getBuilder()
                .withId(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + originalElement.getId())
                .withTocItem(originalElement.getTocItem())
                .withParentItem(originalElement.getParentItem())
                .withNumTagIndex((Node) originalElement.getNumTagIndex())
                .withOriginAttr(originalElement.getOriginAttr())
                .withNumber(originalElement.getNumber())
                .withVtdIndex((Node) originalElement.getVtdIndex())
                .withOriginNumAttr(originalElement.getOriginNumAttr())
                .withSoftActionAttr(SoftActionType.MOVE_TO)
                .withIsSoftActionRoot(true)
                .withSoftMoveTo(originalElement.getId());
        return builder.build();
    }

    public static TableOfContentItemVO createMoveToPoint(TableOfContentItemVO originalElement) {
        TocItemVOBuilder builder = TocItemVOBuilder.getBuilder()
                .withId(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + originalElement.getId())
                .withTocItem(originalElement.getTocItem())
                .withParentItem(originalElement.getParentItem())
                .withNumTagIndex((Node) originalElement.getNumTagIndex())
                .withOriginAttr(originalElement.getOriginAttr())
                .withNumber(originalElement.getNumber())
                .withVtdIndex((Node) originalElement.getVtdIndex())
                .withContent(originalElement.getContent())
                .withOriginNumAttr(originalElement.getOriginNumAttr())
                .withHeading(originalElement.getHeading())
                .withChildItems(originalElement.getChildItems()) //copy children
                .withSoftActionAttr(SoftActionType.MOVE_TO)
                .withIsSoftActionRoot(true)
                .withSoftMoveTo(originalElement.getId())
                .withItemDepth(originalElement.getItemDepth());
        return builder.build();
    }

    public static TableOfContentItemVO restorePointToPreviousPosition(TableOfContentItemVO moveFrom, TableOfContentItemVO moveTo) {
        moveFrom.setSoftActionRoot(null);
        moveFrom.setSoftActionAttr(null);
        moveFrom.setSoftMoveFrom(null);
        moveFrom.setSoftUserAttr(null);
        moveFrom.setSoftDateAttr(null);
        moveFrom.setRestored(true);
        moveFrom.setOriginAttr(EC);
        moveFrom.setNumber(moveTo.getNumber());
        moveFrom.setOriginNumAttr(EC);
        return moveFrom;
    }

    public static TableOfContentItemVO createMoveFromElement(TableOfContentItemVO originalElement, String instance) {
        TocItemVOBuilder builder = TocItemVOBuilder.getBuilder()
                .withId(originalElement.getId())
                .withTocItem(originalElement.getTocItem())
                .withParentItem(originalElement.getParentItem())
                .withNumTagIndex((Node) originalElement.getNumTagIndex())
                .withOriginAttr(EC)
                .withNumber("#")
                .withVtdIndex((Node) originalElement.getVtdIndex())
                .withContent(originalElement.getContent())
                .withOriginNumAttr(instance)
                .withHeading(originalElement.getHeading())
                .withChildItems(originalElement.getChildItems()) //copy children
                .withSoftActionAttr(SoftActionType.MOVE_FROM)
                .withIsSoftActionRoot(true)
                .withSoftMoveFrom(SOFT_MOVE_PLACEHOLDER_ID_PREFIX + originalElement.getId())
                .withItemDepth(originalElement.getItemDepth());
        return builder.build();
    }

    public static TableOfContentItemVO getElementById(List<TableOfContentItemVO> tocList, String id) {
        List<TableOfContentItemVO> allToc = tocList.stream()
                .flatMap(l -> l.flattened())
                .collect(Collectors.toList());

        return allToc.stream()
                //.filter(toc -> toc.getTocItem().getAknTag().value().equals(tagName))
                .filter(toc -> toc.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Didn't find element with id " + id));
    }

}
