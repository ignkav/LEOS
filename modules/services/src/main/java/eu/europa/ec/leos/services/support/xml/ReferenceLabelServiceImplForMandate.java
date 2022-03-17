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

import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.domain.common.Result;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.services.support.xml.ref.Ref;
import org.springframework.stereotype.Service;

//@Service
//@Instance(instances = {InstanceType.COUNCIL})
public class ReferenceLabelServiceImplForMandate extends ReferenceLabelServiceImpl {

    /**
     * Generates the soft move label. Example: MOVED from Article 1(1), point (a)
     *
     * @return: returns the label if ref is valid or an error code if not.
     */
    @Override
    public Result<String> generateSoftMoveLabel(Ref ref, String sourceRefId, byte[] sourceBytes, String attr,
                                                String sourceDocumentRef) {
        return super.generateSoftMoveLabel(ref, sourceRefId, sourceBytes, attr, sourceDocumentRef);
    }
}
