/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.dto;

import it.eng.parer.ws.versamento.dto.VoceDiErrore;
import java.util.List;

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
