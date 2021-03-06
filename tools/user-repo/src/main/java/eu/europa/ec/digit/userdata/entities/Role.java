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
package eu.europa.ec.digit.userdata.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "LEOS_ROLE")
public class Role {
    
    @Id
    @Column(name = "ROLE_NAME", nullable = false, insertable = false, updatable = false)
    private String role;

    @Column(name = "ROLE_DESC", nullable = false, insertable = false, updatable = false)
    private String roleDesc;

    public Role() {
    }

    public Role(String role, String roleDesc) {
        this.role = role;
        this.roleDesc = roleDesc;
    }

    public String getRole() {
        return role;
    }

    public String getRoleDesc() {
        return roleDesc;
    }
}
