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
package eu.europa.ec.leos.services.support.xml.ref;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEVEL;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.POINT;

/**
 * Simple way for building the label.
 * 1) Build the Queue with all incriminated words.
 * 2) Print the queues elements.
 *
 * Example if choosing Level 1, 2 and 3 the queue will have the following structure:
 * [0]: <ref xml:id="" href="body_level_1" documentref="annex_ck88ujxkp0000l8490kba4tif">3</ref>
 * [1]: and
 * [2]: <ref xml:id="" href="body_level_1" documentref="annex_ck88ujxkp0000l8490kba4tif">2</ref>
 * [3]: ,
 * [4]: <ref xml:id="" href="body_level_1" documentref="annex_ck88ujxkp0000l8490kba4tif">1</ref>
 * [5]: Point
 */
@Component
public class LabelLevelElementsOnly extends LabelHandler {

    private static final List<String> NODES_TO_CONSIDER = Arrays.asList(LEVEL);

    @Override
    public void process(List<TreeNode> refs, List<TreeNode> mrefCommonNodes, TreeNode sourceNode, StringBuffer label, Locale locale, boolean withAnchor) {
        Deque<String> sb = new ArrayDeque<>();
        final String refType = POINT;

        if (refs.size() == 1 && mrefCommonNodes.contains(refs.get(0))) {
            sb.addLast(THIS_REF + " " + refType);
        } else {
            //collect all the ref anchors
            for (int i = 0; i < refs.size(); i++) {
                if (i != 0 && i == refs.size() - 1) {
                    sb.push(" and ");
                } else if (i > 0) {
                    sb.push(", ");
                }
                sb.push(createAnchor(refs.get(i), locale, withAnchor));
            }
            sb.addLast(refType + " ");
        }

        while (sb.size() > 0){
            label.append(sb.removeLast());
        }
    }

    @Override
    public boolean canProcess(List<TreeNode> refs) {
        boolean canProcess = refs.stream()
                .allMatch(ref -> NODES_TO_CONSIDER.contains(ref.getType()));
        return canProcess;
    }

    @Override
    public void addPreffix(StringBuffer label, String docType, List<TreeNode> refs) {
        if (!StringUtils.isEmpty(docType)) {
            label.append(docType);
            label.append(", ");
        }
    }

    @Override
    public int getOrder() {
        return 5;
    }
}
