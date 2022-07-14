/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.eng.parer.ws.versamento.dto;

public interface IProfiloEntity {

    /**
     * Logica che si è possibile esndere aggiungendo eventuali altri id di profili censiti. (e.g.
     * getIdRecXsdProfiloNormativo != null and getIdRecXsdProfiloGenerale != null and ....etc.)
     * 
     * @return true se presente almeno un profilo
     */
    public default boolean hasXsdProfile() {
        return getIdRecUsoXsdProfiloNormativo() != null;
    }

    /* id "tipizzati" per i profili esistenti */
    public Long getIdRecUsoXsdProfiloNormativo();

    public void setIdRecUsoXsdProfiloNormativo(Long idRecUsoXsdProfiloNormativo);

    /* xml canonicalizzati del profilo generato ai controlli */
    public String getDatiC14NProfNormXml();

    public void setDatiC14NProfNormXml(String datiC14NProfNormXml);

}
