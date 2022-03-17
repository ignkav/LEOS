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

public abstract class NumberingConfigProcessorAbstract implements NumberingConfigProcessor {

    protected int value;
    protected String numberToShow;
    protected String prefix;
    protected String suffix;

    protected boolean isComplex;

    public NumberingConfigProcessorAbstract() {
        numberToShow = getActualNumberToShow();
    }

    @Override
    public String getNextNumberToShow() {
        value++;
        numberToShow = getActualNumberToShow();
        return numberToShow;
    }

    @Override
    public void setComplex(boolean isComplex) {
        this.isComplex = isComplex;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(getImplName() + "[")
                .append("value = ").append(value)
                .append(", numberToShow = ").append(numberToShow)
                .append(", prefix = ").append(prefix)
                .append(", suffix = ").append(suffix)
                .append(", isComplex = ").append(isComplex)
                .append("]");

        return sb.toString();
    }

    protected abstract String getImplName();
}
