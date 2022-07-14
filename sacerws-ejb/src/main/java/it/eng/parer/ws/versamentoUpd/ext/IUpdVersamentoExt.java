/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamentoUpd.ext;

import java.io.Serializable;

import it.eng.parer.ws.dto.IRestWSBase;
import it.eng.parer.ws.versamentoUpd.dto.StrutturaUpdVers;
import it.eng.spagoLite.security.User;

/**
 *
 * @author sinatti_s
 */

public interface IUpdVersamentoExt extends Serializable, IRestWSBase {

    StrutturaUpdVers getStrutturaUpdVers();

    void setStrutturaUpdVers(StrutturaUpdVers strutturaUpdVers);

    boolean isSimulaScrittura();

    void setSimulaScrittura(boolean simulaScrittura);

    // necessario a gestire l'EJB come stateless
    User getUtente();

    void setUtente(User utente);
}
