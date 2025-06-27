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

package it.eng.parer.ws.versamentoUpd.ext;

import java.util.Map;
import java.util.Set;

import it.eng.parer.ws.versamentoUpd.utils.UpdCostanti.AggiornamentoEffettuato;

public interface IAggiornamentiWsMultipli {

    //
    Set<AggiornamentoEffettuato> getAggiornamentiUnitaDocumentaria();

    //
    Map<String, Set<AggiornamentoEffettuato>> getAggiornamentiDocPrincipale();

    Set<AggiornamentoEffettuato> getAggiornamentiDocPrincipale(String key);

    //
    Map<String, Set<AggiornamentoEffettuato>> getAggiornamentiCompDocPrincipale();

    Set<AggiornamentoEffettuato> getAggiornamentiCompDocPrincipale(String key);

    //
    Map<String, Set<AggiornamentoEffettuato>> getAggiornamentiAllegati();

    Set<AggiornamentoEffettuato> getAggiornamentiAllegato(String key);

    Map<String, Set<AggiornamentoEffettuato>> getAggiornamentiCompAllegati();

    Set<AggiornamentoEffettuato> getAggiornamentiCompAllegato(String key);

    //
    Map<String, Set<AggiornamentoEffettuato>> getAggiornamentiAnnotazioni();

    Set<AggiornamentoEffettuato> getAggiornamentiAnnotazione(String key);

    Map<String, Set<AggiornamentoEffettuato>> getAggiornamentiCompAnnotazioni();

    Set<AggiornamentoEffettuato> getAggiornamentiCompAnnotazione(String key);

    //
    Map<String, Set<AggiornamentoEffettuato>> getAggiornamentiAnnessi();

    Set<AggiornamentoEffettuato> getAggiornamentiAnnesso(String key);

    Map<String, Set<AggiornamentoEffettuato>> getAggiornamentiCompAnnessi();

    Set<AggiornamentoEffettuato> getAggiornamentiCompAnnesso(String key);

    // add
    void addAggiornamentoUnitaDocumentaria(AggiornamentoEffettuato aggiornamento);

    void addAggiornamentoDocPrincipale(AggiornamentoEffettuato aggiornamento, String key);

    void addAggiornamentoCompDocPrincipale(AggiornamentoEffettuato aggiornamento, String key);

    void addAggiornamentoAllegati(AggiornamentoEffettuato aggiornamento, String key);

    void addAggiornamentoCompAllegati(AggiornamentoEffettuato aggiornamento, String key);

    void addAggiornamentoAnnessi(AggiornamentoEffettuato aggiornamento, String key);

    void addAggiornamentoCompAnnessi(AggiornamentoEffettuato aggiornamento, String key);

    void addAggiornamentoAnnotazioni(AggiornamentoEffettuato aggiornamento, String key);

    void addAggiornamentoCompAnnotazioni(AggiornamentoEffettuato aggiornamento, String key);

}
