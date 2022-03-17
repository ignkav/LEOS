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

public class NumberingConfigProcessorAlpha extends NumberingConfigProcessorAbstract implements NumberingConfigProcessor {

    public NumberingConfigProcessorAlpha(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public NumberingConfigProcessorAlpha() {
        this("", "");
    }

    @Override
    public String getActualNumberToShow() {
        String val;
        if (value < 0) {
            val = "-" + getAlphaNumber(abs(value));
        } else {
            val = getAlphaNumber(value);
        }
        return val;
    }

    protected String getAlphaNumber(int val) {
        if (val > 0 && val < 27) {
            return String.valueOf((char) (val + 96));
        } else if (val > 0) {
            int rest = val % 26;
            int div = val / 26;
            String num = "";
            for (int i = 0; i < div; i++) {
                num += "z";
            }
            num += (rest > 0) ? ((char) (rest + 96)) : "";
            return num;
        } else {
            return "";
        }
    }

    @Override
    protected String getImplName() {
        return this.getClass().getSimpleName();
    }

}
