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
package it.eng.parer.firma.exception;

import it.eng.parer.firma.xml.VerificaFirmaWrapper;

/**
 *
 * @author sinatti_s
 */
public class VerificaFirmaWrapperGenericException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -4578580893175157354L;

    private final transient VerificaFirmaWrapper wrapper;

    public VerificaFirmaWrapperGenericException(final VerificaFirmaWrapper wrapper) {
        super();
        this.wrapper = wrapper;
    }

    public VerificaFirmaWrapperGenericException(Throwable ex, final VerificaFirmaWrapper wrapper) {
        super(ex);
        this.wrapper = wrapper;
    }

    public VerificaFirmaWrapperGenericException(String message, final VerificaFirmaWrapper wrapper) {
        super(message);
        this.wrapper = wrapper;
    }

    public VerificaFirmaWrapper getVerificaFirmaWrapper() {
        return wrapper;
    }

    @Override
    public String getMessage() {
        return "Servizio di verifica firma " + wrapper.getAdditionalInfo().getServiceCode() + " [versione="
                + wrapper.getServiceVersion() + ", libreria=" + wrapper.getLibraryVersion()
                + "] errore durante elaborazione report verifica " + super.getMessage();
    }

}
