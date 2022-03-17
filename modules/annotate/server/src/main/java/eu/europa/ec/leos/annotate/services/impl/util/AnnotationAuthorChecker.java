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
package eu.europa.ec.leos.annotate.services.impl.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import eu.europa.ec.leos.annotate.model.entity.Annotation;
import eu.europa.ec.leos.annotate.model.entity.User;

public final class AnnotationAuthorChecker {

    private static final Logger LOG = LoggerFactory.getLogger(AnnotationAuthorChecker.class);

    private AnnotationAuthorChecker() {
        // Prevent instantiation as all methods are static.
    }

    /**
     * check if a given annotation belongs to a user
     * 
     * @param annot
     *        the annotation to be checked
     * @param userlogin
     *        login of the user to be checked
     *        
     * @return true if user created the annotation
     */
    public static boolean isAnnotationOfUser(final Annotation annot, final User user) {

        Assert.notNull(annot, "Required annotation missing");
        Assert.notNull(user, "Required user missing");

        try {
            return annot.getUser().getId().equals(user.getId());
        } catch (Exception e) {
            LOG.error("Error checking if annotation belongs to user", e);
        }

        return false;
    }

    public static boolean isContributorOfAnnotation(final Annotation annot, final User user) {

        Assert.notNull(annot, "Required annotation missing");
        Assert.notNull(user, "Required user missing");

        return !annot.isShared() && AnnotationAuthorChecker.isAnnotationOfUser(annot, user);
    }

}
