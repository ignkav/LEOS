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

import org.junit.Assert;
import org.junit.Test;

import eu.europa.ec.leos.annotate.model.entity.Annotation;
import eu.europa.ec.leos.annotate.model.entity.User;

public class AnnotationAuthorCheckerTest {

    @Test
    public void testIsAnnotationOfUser() {

        final User user = new User("a");

        final Annotation annot = new Annotation();
        annot.setUser(user);

        Assert.assertTrue(AnnotationAuthorChecker.isAnnotationOfUser(annot, user));
    }

    @Test
    public void testIsAnnotationOfUser_UndefinedUser() {

        final User user = new User("a");

        final Annotation annot = new Annotation();

        Assert.assertFalse(AnnotationAuthorChecker.isAnnotationOfUser(annot, user));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsAnnotationOfUser_NullAsAnnotation() {

        final User user = new User("a");

        AnnotationAuthorChecker.isAnnotationOfUser(null, user);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsAnnotationOfUser_NullAsUser() {

        final User user = new User("a");

        final Annotation annot = new Annotation();
        annot.setUser(user);

        AnnotationAuthorChecker.isAnnotationOfUser(annot, null);
    }

    @Test
    public void testIsContributorOfAnnotation() {

        final User user = new User("a");

        final Annotation annot = new Annotation();
        annot.setShared(false);
        annot.setUser(user);

        Assert.assertTrue(AnnotationAuthorChecker.isContributorOfAnnotation(annot, user));
    }

    @Test
    public void testIsContributorOfAnnotation_SharedAnnotation() {

        final User user = new User("a");

        final Annotation annot = new Annotation();
        annot.setShared(true);
        annot.setUser(user);

        Assert.assertFalse(AnnotationAuthorChecker.isContributorOfAnnotation(annot, user));
    }

    @Test
    public void testIsContributorOfAnnotation_UndefinedUser() {

        final User user = new User("a");

        final Annotation annot = new Annotation();
        annot.setShared(false);

        Assert.assertFalse(AnnotationAuthorChecker.isContributorOfAnnotation(annot, user));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsContributorOfAnnotation_NullAsAnnotation() {

        final User user = new User("a");

        AnnotationAuthorChecker.isContributorOfAnnotation(null, user);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIsContributorOfAnnotation_NullAsUser() {

        final Annotation annot = new Annotation();
        annot.setShared(false);

        AnnotationAuthorChecker.isContributorOfAnnotation(annot, null);
    }

}
