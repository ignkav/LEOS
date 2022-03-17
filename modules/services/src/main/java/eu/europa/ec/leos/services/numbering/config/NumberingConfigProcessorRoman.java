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
package eu.europa.ec.leos.services.numbering.config;

import static java.lang.Math.abs;
import static java.lang.String.join;
import static java.util.Collections.nCopies;

public class NumberingConfigProcessorRoman extends NumberingConfigProcessorAbstract implements NumberingConfigProcessor {

    private boolean isUpperCase;

    public NumberingConfigProcessorRoman(boolean isUpperCase, String prefix, String suffix) {
        this.isUpperCase = isUpperCase;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public NumberingConfigProcessorRoman() {
        this(false, "", "");
    }

    @Override
    public String getActualNumberToShow() {
        String val;
        if (value < 0) {
            val = "-" + getRomanUpper(abs(value));
        } else {
            val = getRomanUpper(value);
        }
        return val;
    }

    public String getRomanUpper(int val) {
        return isUpperCase ? getRoman(val).toUpperCase() : getRoman(val);
    }

    private String getRoman(int val) {
        String pointNum = join("", nCopies(val, "i"))
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

    @Override
    protected String getImplName() {
        return this.getClass().getSimpleName();
    }

}
