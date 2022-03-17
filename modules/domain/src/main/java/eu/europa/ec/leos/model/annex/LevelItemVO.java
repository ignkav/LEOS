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
package eu.europa.ec.leos.model.annex;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LevelItemVO implements Serializable {

    private static final long serialVersionUID = 3551047180046009042L;
    
    private String id;
    private String levelNum;
    private int levelDepth;
    private String origin;
    
    private List<LevelItemVO> children = new ArrayList<LevelItemVO>();
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    
    public String getLevelNum() {
        return levelNum;
    }
    
    public void setLevelNum(String levelNum) {
        this.levelNum = levelNum;
    }
    
    public int getLevelDepth() {
        return levelDepth;
    }
    
    public void setLevelDepth(int levelDepth) {
        this.levelDepth = levelDepth;
    }

    public String getOrigin() { return origin; }

    public void setOrigin(String origin) { this.origin = origin; }
    
    public List<LevelItemVO> getChildren() {
        return children;
    }
    
    public void addChildLevelItemVO(LevelItemVO levelItemVO) {
        children.add(levelItemVO);
    }
    
    @Override
    public String toString() {
        return "LevelItemVO [id=" + id + ", levelNum=" + levelNum + ", levelDepth=" + levelDepth + ", children=" + children + "]";
    }
}
