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

import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;

import static eu.europa.ec.leos.services.support.xml.XmlHelper.EC;

public class TocVOCreateProposalUtils {

    public static TableOfContentItemVO createPart(String id, int itemDepth) {
        return TocVOCreateUtils.createPart(id, itemDepth, EC);
    }

    public static TableOfContentItemVO createTitle(String id, int itemDepth) {
        return TocVOCreateUtils.createTitle(id, itemDepth, EC);
    }

    public static TableOfContentItemVO createChapter(String id, int itemDepth) {
        return TocVOCreateUtils.createChapter(id, itemDepth, EC);
    }

    public static TableOfContentItemVO createSection(String id, int itemDepth) {
        return TocVOCreateUtils.createSection(id, itemDepth, EC);
    }

    public static TableOfContentItemVO createArticle(String id, int itemDepth) {
        return TocVOCreateUtils.createArticle(id, itemDepth, EC);
    }

    public static TableOfContentItemVO createLevel(String id, int itemDepth) {
        return TocVOCreateUtils.createLevel(id, itemDepth, EC);
    }

    public static TableOfContentItemVO createMoveToElement(TableOfContentItemVO originalElement) {
        return TocVOCreateUtils.createMoveToElement(originalElement);
    }

    public static TableOfContentItemVO createMoveFromElement(TableOfContentItemVO originalElement) {
        return TocVOCreateUtils.createMoveFromElement(originalElement, EC);
    }

}
