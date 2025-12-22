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

/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package it.eng.parer.exception;

import it.eng.parer.exception.ParerErrorCategory.SacerWsErrorCategory;

public class SacerWsException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -354822225586846363L;

    private final SacerWsErrorCategory category;

    public SacerWsException() {
        super();
        this.category = SacerWsErrorCategory.INTERNAL_ERROR; // default
    }

    public SacerWsException(SacerWsErrorCategory category) {
        super();
        this.category = category;
    }

    public SacerWsException(String message, Throwable cause, SacerWsErrorCategory category) {
        super(message, cause);
        this.category = category;
    }

    public SacerWsException(String message, SacerWsErrorCategory category) {
        super(message);
        this.category = category;
    }

    public SacerWsException(Throwable cause, SacerWsErrorCategory category) {
        super(cause);
        this.category = category;
    }

    public SacerWsErrorCategory getCategory() {
        return category;
    }

    @Override
    public String getLocalizedMessage() {
        return "[" + getCategory().toString() + "]" + "  " + super.getLocalizedMessage();
    }

    @Override
    public String getMessage() {
        return "[" + getCategory().toString() + "]" + "  " + super.getMessage();
    }

}
