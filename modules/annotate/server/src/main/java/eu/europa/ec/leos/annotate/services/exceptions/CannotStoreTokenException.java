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
package eu.europa.ec.leos.annotate.services.exceptions;

/**
 * Exception that is thrown when the storing of a user's access/refresh tokens fails
 * The inner exception gives more details
 */
public class CannotStoreTokenException extends Exception {

    private static final long serialVersionUID = 1L;

    public CannotStoreTokenException(final String msg, final Throwable exc) {
        super(msg, exc);
    }
    
    public CannotStoreTokenException(final String msg) {
        super(msg);
    }
}
