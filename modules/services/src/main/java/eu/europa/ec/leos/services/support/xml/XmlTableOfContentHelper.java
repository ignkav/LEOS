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
package eu.europa.ec.leos.services.support.xml;

import eu.europa.ec.leos.domain.common.TocMode;
import eu.europa.ec.leos.vo.toc.TableOfContentItemVO;

import java.util.List;

import static eu.europa.ec.leos.services.support.xml.XmlHelper.PARAGRAPH;

public interface XmlTableOfContentHelper {

    static String getTagValueFromTocItemVo(TableOfContentItemVO tableOfContentItemVO) {
        return tableOfContentItemVO.getTocItem().getAknTag().value();
    }

    static Boolean checkIfParagraphNumberingIsToggled(TableOfContentItemVO tableOfContentItemVO) {
        if (PARAGRAPH.equals(tableOfContentItemVO.getTocItem().getAknTag().value())
                && tableOfContentItemVO.getParentItem().isNumberingToggled() != null) {
            return tableOfContentItemVO.getParentItem().isNumberingToggled();
        }
        return null;
    }

    List<TableOfContentItemVO> buildTableOfContent(String startingNode, byte[] xmlContent, TocMode mode);

}
