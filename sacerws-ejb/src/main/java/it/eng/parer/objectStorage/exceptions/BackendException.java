/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna <p/> This program is free software: you can
 * redistribute it and/or modify it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version. <p/> This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details. <p/> You should
 * have received a copy of the GNU Affero General Public License along with this program. If not,
 * see <https://www.gnu.org/licenses/>.
 */

package it.eng.parer.objectStorage.exceptions;

import java.text.MessageFormat;

public class BackendException extends Exception {

    private static final long serialVersionUID = -2311721715615582399L;

    private final String message;
    private final Throwable cause;

    private BackendException(BackendExceptionBuilder builder) {
        super(builder.message, builder.cause);
        this.message = builder.message;
        this.cause = builder.cause;
    }

    public static BackendExceptionBuilder builder() {
        return new BackendExceptionBuilder();
    }

    public String message() {
        return message;
    }

    public Throwable cause() {
        return cause;
    }

    public static class BackendExceptionBuilder {

        private String message;
        private Throwable cause;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public BackendExceptionBuilder message(String message) {
            this.setMessage(message);
            return this;
        }

        public BackendExceptionBuilder message(String messageToFormat, Object... args) {
            this.setMessage(MessageFormat.format(messageToFormat, args));
            return this;
        }

        public Throwable getCause() {
            return cause;
        }

        public void setCause(Throwable cause) {
            this.cause = cause;
        }

        public BackendExceptionBuilder cause(Throwable cause) {
            this.setCause(cause);
            return this;
        }

        public BackendException build() {
            return new BackendException(this);
        }
    }
}
