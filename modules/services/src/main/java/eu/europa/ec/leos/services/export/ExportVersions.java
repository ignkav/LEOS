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
package eu.europa.ec.leos.services.export;

import eu.europa.ec.leos.domain.cmis.document.XmlDocument;

public class ExportVersions<T extends XmlDocument> {
    
    private T original;
    private T intermediate;
    private T current;
    
    public ExportVersions(T original, T intermediate, T current) {
        this.original = original;
        this.intermediate = intermediate;
        this.current = current;
    }

    public ExportVersions(T original, T current) {
        this.original = original;
        this.intermediate = null;
        this.current = current;
    }

    public T getOriginal() {
        return original;
    }

    public T getIntermediate() {
        return intermediate;
    }

    public T getCurrent() {
        return current;
    }
    
    public Class getVersionClass(){
        return current.getClass();
    }

    public int getVersionsPresent() {
        int count = 0;
        if (original != null) {
            count++;
        }
        if (intermediate != null) {
            count++;
        }
        if (current != null) {
            count++;
        }
        return count;
    }
    
}
