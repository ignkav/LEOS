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
package eu.europa.ec.leos.services.numbering.processor;

import eu.europa.ec.leos.domain.common.InstanceType;
import eu.europa.ec.leos.i18n.MessageHelper;
import eu.europa.ec.leos.instance.Instance;
import eu.europa.ec.leos.services.numbering.LevelVO;
import eu.europa.ec.leos.services.numbering.config.NumberingConfigProcessor;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Arrays;

import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_DEPTH_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEOS_ORIGINAL_DEPTH_ATTR;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEVEL;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.LEVEL_MAX_DEPTH;
import static eu.europa.ec.leos.services.support.xml.XmlHelper.NUM;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.getAttributeValueAsInteger;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.getChildContent;
import static eu.europa.ec.leos.services.support.xml.XmlUtils.getId;

@Component
@Instance(instances = {InstanceType.COMMISSION, InstanceType.OS})
public class NumberProcessorLevel extends NumberProcessorAbstract {

    private static final Logger LOG = LoggerFactory.getLogger(NumberProcessorLevel.class);

    @Autowired
    public NumberProcessorLevel(MessageHelper messageHelper, NumberProcessorHandler numberProcessorHandler) {
        super(messageHelper, numberProcessorHandler);
    }

    @Override
    public boolean canRenumber(String elementName, Node element, boolean renumberChildren) {
        if (LEVEL.equals(elementName)) {
            return true;
        }
        return false;
    }

    @Override
    public void renumber(String elementName, Document document, Node node, NumberingConfigProcessor numberProcessor) {
        LOG.trace("Will number element '{}' inside nodeName '{}', nodeId '{}' ", elementName, node.getNodeName(), getId(node));
        NodeList children = document.getElementsByTagName(elementName);
        LOG.debug("Going to renumber element " + elementName + ", using numberProcessor: " + numberProcessor);

        String numLabel;
        LevelVO lastLevelVo = null;
        for (int i = 0; i < children.getLength(); i++) {
            node = children.item(i);
            if (!numberProcessorHandler.skipAutoRenumbering(node)) {
                LevelVO prevLevelVo = lastLevelVo;

                //2.set current level
                LevelVO currLevelVo = getLevelVo(node);

                //3. Update current level vo based on prev old depth - To adjust depth for indent & outdent
                currLevelVo = updateCurrentLevelVo(prevLevelVo, currLevelVo);

                //4.get level num of current level
                numLabel = numberProcessor.getPrefix() + getLevelNumeral(prevLevelVo, currLevelVo, numberProcessor.getNextNumberToShow()) + numberProcessor.getSuffix();

                //5.update previous level by current level
                lastLevelVo = currLevelVo;
                lastLevelVo.setLevelNum(numLabel);

                buildNumElement(document, node, numLabel);
                LOG.debug("{} '{}' numbered to '{}'", node.getNodeName(), getId(node), numLabel);
            }
        }
    }

    private LevelVO getLevelVo(Node node) {
        final Integer depth = getAttributeValueAsInteger(node, LEOS_DEPTH_ATTR);
        final Integer oldDepth = getAttributeValueAsInteger(node, LEOS_ORIGINAL_DEPTH_ATTR);
        final String num = getChildContent(node, NUM);

        LevelVO levelVo = new LevelVO();
        levelVo.setLevelDepth(depth);
        levelVo.setOldDepth(oldDepth);
        levelVo.setLevelNum(num);
        return levelVo;
    }

    protected LevelVO updateCurrentLevelVo(LevelVO prevLevelVo, LevelVO currLevelVo) {
        if (prevLevelVo != null && prevLevelVo.getOldDepth() > 0) {
            int oldDepth = prevLevelVo.getOldDepth();

            boolean toIndent;
            if (prevLevelVo.isModified()) {
                toIndent = currLevelVo.getLevelDepth() > oldDepth;
            } else {
                toIndent = currLevelVo.getLevelDepth() >= oldDepth;
            }

            if (toIndent) {
                currLevelVo.setOldDepth(currLevelVo.getLevelDepth());
                int prevDepth = prevLevelVo.getLevelDepth();
                currLevelVo.setLevelDepth(prevDepth < LEVEL_MAX_DEPTH ? (prevDepth + 1) : prevDepth);
            }
        } else if (currLevelVo.getOldDepth() > 0) {
            currLevelVo.setModified(true);
        }
        return currLevelVo;
    }

    protected String getLevelNumeral(LevelVO prevLevelVo, LevelVO currLevelVo, String levelNum) {
        if (prevLevelVo != null) {
            int prevLevelDepth = prevLevelVo.getLevelDepth();
            String prevLevelNum = prevLevelVo.getLevelNum();
            int currLevelDepth = currLevelVo.getLevelDepth();
            if (prevLevelDepth == currLevelDepth) {
                levelNum = getNextLevelNum(prevLevelDepth, prevLevelNum);
            } else if (currLevelDepth > prevLevelDepth) {
                levelNum = prevLevelNum.concat("1");
            } else if (currLevelDepth < prevLevelDepth) {
                levelNum = getNextNumForDepth(currLevelDepth, prevLevelNum);
            }
        }
        return levelNum;
    }

    protected String getNextLevelNum(int levelDepth, String levelNum) {
        String[] numArr = getNextNum(levelDepth, levelNum);
        return String.join(".", numArr);
    }

    protected String getNextNumForDepth(int levelDepth, String levelNum) {
        String[] numArr = getNextNum(levelDepth, levelNum);
        String[] copyArr = Arrays.copyOfRange(numArr, 0, levelDepth);
        return String.join(".", copyArr);
    }

    protected String[] getNextNum(int levelDepth, String levelNum) {
        String[] numArr = StringUtils.split(levelNum, ".");
        String numStr = numArr[levelDepth - 1];
        int currNum = Integer.parseInt(numStr) + 1;
        numArr[levelDepth - 1] = Integer.toString(currNum);
        return numArr;
    }
}

