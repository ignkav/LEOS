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

import eu.europa.ec.leos.vo.toc.NumberingType;
import eu.europa.ec.leos.vo.toc.TocItem;
import eu.europa.ec.leos.vo.toc.TocItemUtils;

import java.util.List;

import static java.lang.String.join;
import static java.util.Collections.nCopies;

public abstract class ElementNumberingAbstract implements ElementNumberingHelper {

    protected boolean isDefaultEditable = false;

    @Override
    public void setImportArticleDefaultProperties() {
        this.isDefaultEditable = true;
    }

    @Override
    public void resetImportArticleDefaultProperties() {
        this.isDefaultEditable = false;
    }

    protected String getElementNumeral(long number, NumberingType numberingType) {
        switch (numberingType) {
            case ROMAN_LOWER:
                return getRomanNumeral(new Long(number).intValue());
            case ROMAN_UPPER:
                return getRomanNumeral(new Long(number).intValue()).toUpperCase();
            case ALPHA:
                if (number > 0) {
                    char alphaNumber = (char) (number + 96);
                    return calculateAlphaNumber(alphaNumber);
                } else {
                    return "#";
                }
            case ARABIC:
            case ARABIC_POSTFIX:
            case ARABIC_PARENTHESIS:
                return new Long(number).toString();
            default:
                return new Long(number).toString();
        }
    }

    protected String getPointNumber(int number, NumberingType numberingType) {
        String pointNum = "#";
        switch (numberingType) {
            case ROMAN_LOWER:
                pointNum = getRomanNumeral(number);
                break;
            case ALPHA:
                if (number > 0) {
                    char alphaNumber = (char) (number + 96);
                    pointNum = calculateAlphaNumber(alphaNumber);
                } else {
                    return "#";
                }
                break;
            case INDENT:
                pointNum = "-";
                break;
            case ARABIC:
            case ARABIC_PARENTHESIS:
            case ARABIC_POSTFIX:
                pointNum = "" + number;
                break;
            default:
                break;
        }
        return pointNum;
    }

    protected String getRomanNumeral(int number) {
        String pointNum;
        pointNum = join("", nCopies(number, "i"))
                .replace("iiiii", "v")
                .replace("iiii", "iv")
                .replace("vv", "x")
                .replace("viv", "ix")
                .replace("xxxxx", "l")
                .replace("xxxx", "xl")
                .replace("ll", "c")
                .replace("lxl", "xc")
                .replace("ccccc", "d")
                .replace("cccc", "cd")
                .replace("dd", "m")
                .replace("dcd", "cm");
        return pointNum;
    }

    protected boolean isAutoNumberingEnabled(List<TocItem> tocItems, String elementName) {
        Boolean isAutoNumEnabled = TocItemUtils.getTocItemByName(tocItems, elementName).isAutoNumbering();
        return isAutoNumEnabled == null || isAutoNumEnabled;
    }

    protected String calculateAlphaNumber(char alphaNumber) {
        char num = (char) (alphaNumber - 96);
        if (alphaNumber <= 'z') {
            return "" + alphaNumber;
        } else {
            int rest = num % 26;
            int div = num / 26;
            String number = "";
            for (int i = 0; i < div; i++) {
                number += "z";
            }
            number += (rest > 0) ? ((char) (rest + 96)) : "";
            return number;
        }
    }
}
