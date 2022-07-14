/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamento.dto;

import it.eng.parer.ws.dto.IRestWSBase;
import it.eng.spagoLite.security.User;
import java.io.Serializable;

/**
 *
 * @author Fioravanti_F
 */
public interface IVersamentoExt extends Serializable, IRestWSBase {

    StrutturaVersamento getStrutturaComponenti();

    void setStrutturaComponenti(StrutturaVersamento strutturaComponenti);

    public boolean isSimulaScrittura();

    public void setSimulaScrittura(boolean simulaScrittura);

    // necessario a gestire l'EJB come stateless
    public User getUtente();

    public void setUtente(User utente);
}
