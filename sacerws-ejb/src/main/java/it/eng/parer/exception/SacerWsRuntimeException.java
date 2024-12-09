/*
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Copyright (C) 2023 Regione Emilia-Romagna
 * <p/>
 * This program is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Affero General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.exception;

import it.eng.parer.exception.ParerErrorCategory.SacerWsErrorCategory;

/**
 *
 * @author sinatti_s
 */
public class SacerWsRuntimeException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 5015771412184277789L;

    private final SacerWsErrorCategory category;

    public SacerWsRuntimeException() {
        super();
        this.category = SacerWsErrorCategory.INTERNAL_ERROR; // default
    }

    public SacerWsRuntimeException(SacerWsErrorCategory category) {
        super();
        this.category = category;
    }

    public SacerWsRuntimeException(String message, Throwable throwable, SacerWsErrorCategory category) {
        super(message, throwable);
        this.category = category;
    }

    public SacerWsRuntimeException(Throwable throwable, SacerWsErrorCategory category) {
        super(throwable);
        this.category = category;
    }

    public SacerWsRuntimeException(String message, SacerWsErrorCategory category) {
        super(message);
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
