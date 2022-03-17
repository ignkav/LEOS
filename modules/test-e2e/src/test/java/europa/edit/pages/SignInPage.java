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
package europa.edit.pages;

import lombok.experimental.UtilityClass;
import org.openqa.selenium.By;

@UtilityClass
public class SignInPage {
    public static final By USER_NAME = By.id("username");
    public static final By NEXT_BUTTON = By.name("whoamiSubmit");
    public static final By ECAS_SUCCESSFUL_LOGIN_TEXT = By.xpath("//h1[text()='Successful login']");
    public static final By ECAS_LOG_OUT_BUTTON = By.xpath("//*[@id='content']//*[text()='Logout']");
    public static final By ECAS_LOGGED_OUT_MESSAGE = By.xpath("//*[contains(text(),'You have logged out of EU Login')]");
    public static final By ECAS_SIGN_IN_WITH_DIFFERENT_USER = By.xpath("//*[text()='Sign in with a different e-mail address?']");
    public static final By ECAS_SIGN_IN_USER_TEXT = By.xpath("//*[text()='Enter your e-mail address or unique identifier']");
}
