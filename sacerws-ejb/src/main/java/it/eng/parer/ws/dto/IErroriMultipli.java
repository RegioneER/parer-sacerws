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
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.dto;

import java.util.List;

import it.eng.parer.ws.versamento.dto.VoceDiErrore;

/**
 *
 * @author fioravanti_f
 */
public interface IErroriMultipli {

    void aggiungErroreFatale(String errCode, String errMessage);

    VoceDiErrore calcolaErrorePrincipale();

    List<VoceDiErrore> getErroriTrovati();

    boolean isTrovatiErrori();

    boolean isTrovatiWarning();

    VoceDiErrore addError(String descElemento, String errCode, String errMessage);

    VoceDiErrore addWarning(String descElemento, String errCode, String errMessage);

    void listErrAddError(String descElemento, String errCode, Object... params);

    void listErrAddError(String descElemento, String errCode);

    void listErrAddWarning(String descElemento, String errCode, Object... params);

    void listErrAddWarning(String descElemento, String errCode);

    Object produciEsitoErroriSec();

    Object produciEsitoWarningSec();

}
