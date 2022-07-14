/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamento.dto;

import it.eng.parer.ws.dto.RispostaControlli;
import java.util.HashMap;

/**
 *
 * @author Fioravanti_F
 */
public class RispostaControlliAttSpec extends RispostaControlli {

    private long idRecXsdDatiSpec;
    private HashMap<String, DatoSpecifico> datiSpecifici;

    public long getIdRecXsdDatiSpec() {
        return idRecXsdDatiSpec;
    }

    public void setIdRecXsdDatiSpec(long idRecXsdDatiSpec) {
        this.idRecXsdDatiSpec = idRecXsdDatiSpec;
    }

    public HashMap<String, DatoSpecifico> getDatiSpecifici() {
        return datiSpecifici;
    }

    public void setDatiSpecifici(HashMap<String, DatoSpecifico> datiSpecifici) {
        this.datiSpecifici = datiSpecifici;
    }
}
